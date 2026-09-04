package `in`.jyotirmoy.attendx.timetable.domain.usecase

import `in`.jyotirmoy.attendx.core.data.database.SubjectDao
import `in`.jyotirmoy.attendx.core.data.model.SubjectEntity
import `in`.jyotirmoy.attendx.timetable.data.dao.TimeTableDao
import `in`.jyotirmoy.attendx.timetable.data.model.TimeTableScheduleEntity
import `in`.jyotirmoy.attendx.timetable.data.model.community.TemplateClassEntry
import `in`.jyotirmoy.attendx.timetable.data.model.community.TemplateSubjectEntry
import javax.inject.Inject

/**
 * Merges template content into the local tables.
 *
 * Separate from [ImportTemplateUseCase] purely so the merge rules can be driven
 * by fake DAOs in a plain JUnit test — the use case itself needs a real
 * RoomDatabase for withTransaction, which would drag in Robolectric.
 */
class TemplateMerger @Inject constructor(
    private val timeTableDao: TimeTableDao,
    private val subjectDao: SubjectDao
) {
    suspend fun merge(
        subjects: List<TemplateSubjectEntry>,
        classes: List<TemplateClassEntry>
    ) {
        subjects.forEach { upsertSubject(it) }
        classes.forEach { insertClassIfAbsent(it) }
    }

    /**
     * Creates the subject, or fills in metadata the local copy is missing.
     * Existing non-blank values are never overwritten — the user's own edits win
     * over the template's.
     */
    private suspend fun upsertSubject(entry: TemplateSubjectEntry) {
        val existingId = subjectDao.getSubjectIdByName(entry.name)

        if (existingId == null) {
            subjectDao.insertSubject(
                SubjectEntity(
                    subject = entry.name,
                    subjectCode = entry.code,
                    histogramLabel = entry.histogramLabel,
                    targetPercentage = DEFAULT_TARGET
                )
            )
            return
        }

        val existing = subjectDao.getSubjectEntityById(existingId) ?: return
        val code = existing.subjectCode?.takeIf { it.isNotBlank() } ?: entry.code
        val label = existing.histogramLabel?.takeIf { it.isNotBlank() } ?: entry.histogramLabel

        if (code != existing.subjectCode || label != existing.histogramLabel) {
            // existing.subject, not entry.name: keep whatever the user renamed it to.
            subjectDao.updateSubject(existingId, existing.subject, code, label)
        }
    }

    private suspend fun insertClassIfAbsent(entry: TemplateClassEntry) {
        val subjectId = subjectDao.getSubjectIdByName(entry.subject)
            ?: subjectDao.insertSubject(
                SubjectEntity(subject = entry.subject, targetPercentage = DEFAULT_TARGET)
            ).toInt()

        // TimeTableScheduleEntity.id is autoGenerate, so it is always 0 on insert
        // and OnConflictStrategy.REPLACE never fires. Without this explicit check
        // a second import duplicates the entire timetable.
        //
        // A duplicate is always a time overlap, so the existing clash query finds
        // the candidates and an exact-slot comparison confirms them.
        val alreadyPresent = timeTableDao
            .checkTimeClash(entry.dayOfWeek, entry.startTime, entry.endTime)
            .any {
                it.subjectId == subjectId &&
                    it.startTime == entry.startTime &&
                    it.endTime == entry.endTime
            }
        if (alreadyPresent) return

        timeTableDao.insertClass(
            TimeTableScheduleEntity(
                subjectId = subjectId,
                dayOfWeek = entry.dayOfWeek,
                startTime = entry.startTime,
                endTime = entry.endTime,
                room = entry.room,
                classType = entry.type
            )
        )
    }

    private companion object {
        const val DEFAULT_TARGET = 75.0f
    }
}
