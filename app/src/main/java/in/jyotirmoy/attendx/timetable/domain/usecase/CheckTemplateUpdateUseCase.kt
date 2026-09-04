package `in`.jyotirmoy.attendx.timetable.domain.usecase

import `in`.jyotirmoy.attendx.settings.data.local.SettingsKeys
import `in`.jyotirmoy.attendx.settings.domain.repository.SettingsRepository
import `in`.jyotirmoy.attendx.timetable.data.model.community.TemplateClassEntry
import `in`.jyotirmoy.attendx.timetable.data.model.community.TemplateSubjectEntry
import `in`.jyotirmoy.attendx.timetable.domain.repository.TemplateRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * A newer version of the template this timetable came from.
 *
 * [incoming] and [localOnly] are a diff of the published template against the
 * *current* local timetable, not against what was originally imported. That is
 * deliberate: it describes what applying the update would actually do, and it
 * needs no stored snapshot. The trade-off is that [localOnly] mixes the author's
 * removals with the user's own additions, which is why nothing is ever deleted
 * and the dialog describes it neutrally.
 */
data class TemplateUpdateInfo(
    val templateId: String,
    val templateName: String,
    val version: Int,
    val incoming: List<TemplateClassEntry>,
    val localOnly: List<TemplateClassEntry>,
    val newSubjects: List<TemplateSubjectEntry>
)

class CheckTemplateUpdateUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val templateRepository: TemplateRepository,
    private val getExportableDataUseCase: GetExportableDataUseCase
) {
    suspend operator fun invoke(force: Boolean = false): TemplateUpdateInfo? {
        val templateId = settingsRepository.getString(SettingsKeys.IMPORTED_TEMPLATE_ID).first()
        if (templateId.isBlank()) return null

        val now = System.currentTimeMillis()
        if (!force && now - lastCheck() < CHECK_INTERVAL_MS) return null

        val remote = templateRepository.getTemplateDetails(templateId).getOrNull()
            ?: return null // offline or deleted — try again next launch

        val importedVersion = settingsRepository
            .getInt(SettingsKeys.IMPORTED_TEMPLATE_VERSION).first()
        val dismissedVersion = settingsRepository
            .getInt(SettingsKeys.IMPORTED_TEMPLATE_DISMISSED_VERSION).first()

        if (remote.version <= importedVersion || remote.version <= dismissedVersion) {
            return noUpdate(now)
        }

        val (localSubjects, localClasses) = getExportableDataUseCase()
        val diff = diffTemplate(remote.classes, remote.subjects, localClasses, localSubjects)
        val (incoming, localOnly, newSubjects) = diff

        if (incoming.isEmpty() && localOnly.isEmpty() && newSubjects.isEmpty()) {
            // The author bumped the version without changing anything that affects
            // this timetable (a renamed college, say). Absorb it silently.
            settingsRepository.setInt(SettingsKeys.IMPORTED_TEMPLATE_VERSION, remote.version)
            return noUpdate(now)
        }

        // The check timestamp is deliberately NOT advanced here: leaving it stale
        // means an update the user neither applied nor dismissed is offered again
        // on the next launch rather than disappearing for a day.
        return TemplateUpdateInfo(
            templateId = templateId,
            templateName = remote.name,
            version = remote.version,
            incoming = incoming,
            localOnly = localOnly,
            newSubjects = newSubjects
        )
    }

    /** Records that a check happened and found nothing to offer. */
    private suspend fun noUpdate(now: Long): TemplateUpdateInfo? {
        settingsRepository.setString(SettingsKeys.LAST_TEMPLATE_CHECK, now.toString())
        return null
    }

    private suspend fun lastCheck(): Long =
        settingsRepository.getString(SettingsKeys.LAST_TEMPLATE_CHECK).first().toLongOrNull() ?: 0L

    private companion object {
        const val CHECK_INTERVAL_MS = 24L * 60 * 60 * 1000
    }
}

/** The three lists that make up a template diff. */
data class TemplateDiff(
    val incoming: List<TemplateClassEntry>,
    val localOnly: List<TemplateClassEntry>,
    val newSubjects: List<TemplateSubjectEntry>
)

/**
 * Pure set-difference of a published template against the local timetable.
 *
 * TemplateClassEntry is a data class, so structural equality does the work — but
 * that means a changed time or room shows up as one incoming plus one localOnly
 * rather than as an edit. Callers must sort deterministically, because
 * getAllClassesWithSubject() has no ORDER BY.
 */
internal fun diffTemplate(
    remoteClasses: List<TemplateClassEntry>,
    remoteSubjects: List<TemplateSubjectEntry>,
    localClasses: List<TemplateClassEntry>,
    localSubjects: List<TemplateSubjectEntry>
): TemplateDiff {
    val bySlot = compareBy<TemplateClassEntry>({ it.dayOfWeek }, { it.startTime })
    val localNames = localSubjects.mapTo(mutableSetOf()) { it.name.lowercase() }

    return TemplateDiff(
        incoming = remoteClasses.filterNot { it in localClasses }.sortedWith(bySlot),
        localOnly = localClasses.filterNot { it in remoteClasses }.sortedWith(bySlot),
        newSubjects = remoteSubjects.filterNot { it.name.lowercase() in localNames }
    )
}
