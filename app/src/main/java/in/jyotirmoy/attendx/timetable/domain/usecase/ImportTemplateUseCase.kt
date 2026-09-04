package `in`.jyotirmoy.attendx.timetable.domain.usecase

import androidx.room.withTransaction
import `in`.jyotirmoy.attendx.core.data.database.SubjectDatabase
import `in`.jyotirmoy.attendx.settings.data.local.SettingsKeys
import `in`.jyotirmoy.attendx.settings.domain.repository.SettingsRepository
import `in`.jyotirmoy.attendx.timetable.data.model.community.TemplateClassEntry
import `in`.jyotirmoy.attendx.timetable.data.model.community.TemplateSubjectEntry
import `in`.jyotirmoy.attendx.timetable.domain.repository.TemplateRepository
import javax.inject.Inject

/**
 * Imports selected parts of a community template into the local timetable.
 *
 * Idempotent: importing the same template twice, or re-importing after the author
 * publishes a new version, must not duplicate anything. See [TemplateMerger] for
 * the rules and why they have to be explicit.
 *
 * Nothing is ever deleted. A class the author removed upstream is left alone,
 * because without per-row provenance it is indistinguishable from one the user
 * added themselves.
 */
class ImportTemplateUseCase @Inject constructor(
    private val repository: TemplateRepository,
    private val settingsRepository: SettingsRepository,
    private val database: SubjectDatabase,
    private val merger: TemplateMerger
) {
    suspend operator fun invoke(
        templateId: String,
        templateVersion: Int,
        subjectsToImport: List<TemplateSubjectEntry>,
        classesToImport: List<TemplateClassEntry>
    ): Result<Unit> {
        return try {
            // One transaction: a failure part-way cannot leave a half-import behind.
            database.withTransaction {
                merger.merge(subjectsToImport, classesToImport)
            }

            // Remember where this came from so a later version by the same author
            // can be offered. Nothing else links a local timetable to its origin.
            settingsRepository.setString(SettingsKeys.IMPORTED_TEMPLATE_ID, templateId)
            settingsRepository.setInt(SettingsKeys.IMPORTED_TEMPLATE_VERSION, templateVersion)
            settingsRepository.setInt(SettingsKeys.IMPORTED_TEMPLATE_DISMISSED_VERSION, 0)

            // Analytics, outside the transaction — a network failure here must not
            // roll back a successful import.
            try {
                repository.incrementDownloads(templateId)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
