package `in`.jyotirmoy.attendx.timetable

import `in`.jyotirmoy.attendx.core.data.database.SubjectDao
import `in`.jyotirmoy.attendx.core.data.model.SubjectEntity
import `in`.jyotirmoy.attendx.timetable.data.dao.TimeTableDao
import `in`.jyotirmoy.attendx.timetable.data.model.TimeTableScheduleEntity
import `in`.jyotirmoy.attendx.timetable.data.model.TimeTableScheduleWithSubject
import `in`.jyotirmoy.attendx.timetable.data.model.community.TemplateClassEntry
import `in`.jyotirmoy.attendx.timetable.data.model.community.TemplateSubjectEntry
import `in`.jyotirmoy.attendx.timetable.domain.usecase.TemplateMerger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Guards the bug that made re-importing a template destructive: because
 * TimeTableScheduleEntity.id is autoGenerate it is always 0 on insert, so
 * OnConflictStrategy.REPLACE never fires and every import appended a fresh copy
 * of the whole timetable.
 */
class TemplateMergerTest {

    private val classes = listOf(
        TemplateClassEntry(dayOfWeek = 1, startTime = 600, endTime = 660, subject = "Maths"),
        TemplateClassEntry(dayOfWeek = 1, startTime = 660, endTime = 720, subject = "Physics"),
        TemplateClassEntry(dayOfWeek = 3, startTime = 540, endTime = 600, subject = "Maths")
    )

    private val subjects = listOf(
        TemplateSubjectEntry(name = "Maths", code = "MA101"),
        TemplateSubjectEntry(name = "Physics", code = "PH101")
    )

    @Test
    fun `importing twice does not duplicate classes or subjects`() = runBlocking {
        val db = FakeDb()
        val merger = TemplateMerger(db.timeTableDao, db.subjectDao)

        merger.merge(subjects, classes)
        val afterFirst = db.schedules.size to db.subjects.size

        merger.merge(subjects, classes)

        assertEquals("second import must be a no-op", afterFirst, db.schedules.size to db.subjects.size)
        assertEquals(3, db.schedules.size)
        assertEquals(2, db.subjects.size)
    }

    @Test
    fun `a genuinely new class from a later version is added`() = runBlocking {
        val db = FakeDb()
        val merger = TemplateMerger(db.timeTableDao, db.subjectDao)
        merger.merge(subjects, classes)

        val added = TemplateClassEntry(
            dayOfWeek = 5, startTime = 780, endTime = 840, subject = "Chemistry"
        )
        merger.merge(subjects, classes + added)

        assertEquals(4, db.schedules.size)
        assertEquals(3, db.subjects.size)
    }

    @Test
    fun `subject matching ignores case`() = runBlocking {
        val db = FakeDb()
        db.subjects += SubjectEntity(id = 1, subject = "maths")
        val merger = TemplateMerger(db.timeTableDao, db.subjectDao)

        merger.merge(listOf(TemplateSubjectEntry(name = "Maths")), emptyList())

        assertEquals("must not create a second casing of the same subject", 1, db.subjects.size)
    }

    @Test
    fun `missing subject metadata is filled in but existing values are kept`() = runBlocking {
        val db = FakeDb()
        db.subjects += SubjectEntity(
            id = 1, subject = "Maths", subjectCode = "MY-OWN-CODE", histogramLabel = null
        )
        val merger = TemplateMerger(db.timeTableDao, db.subjectDao)

        merger.merge(
            listOf(TemplateSubjectEntry(name = "Maths", code = "MA101", histogramLabel = "M")),
            emptyList()
        )

        val stored = db.subjects.single()
        assertEquals("the user's own code must win", "MY-OWN-CODE", stored.subjectCode)
        assertEquals("the missing label should be filled in", "M", stored.histogramLabel)
    }

    @Test
    fun `a class at the same slot for a different subject is not treated as duplicate`() = runBlocking {
        val db = FakeDb()
        val merger = TemplateMerger(db.timeTableDao, db.subjectDao)

        merger.merge(
            emptyList(),
            listOf(TemplateClassEntry(dayOfWeek = 2, startTime = 600, endTime = 660, subject = "Maths"))
        )
        merger.merge(
            emptyList(),
            listOf(TemplateClassEntry(dayOfWeek = 2, startTime = 600, endTime = 660, subject = "Physics"))
        )

        assertEquals(2, db.schedules.size)
    }

    @Test
    fun `subject with no prior row is created by a class entry alone`() = runBlocking {
        val db = FakeDb()
        val merger = TemplateMerger(db.timeTableDao, db.subjectDao)

        merger.merge(
            emptyList(),
            listOf(TemplateClassEntry(dayOfWeek = 4, startTime = 600, endTime = 660, subject = "Biology"))
        )

        assertEquals("Biology", db.subjects.single().subject)
        assertNull(db.subjects.single().subjectCode)
    }
}

/** In-memory stand-ins for the two DAOs the merger touches. */
private class FakeDb {
    val subjects = mutableListOf<SubjectEntity>()
    val schedules = mutableListOf<TimeTableScheduleEntity>()
    private var nextSubjectId = 1
    private var nextScheduleId = 1

    val subjectDao = object : SubjectDao {
        override suspend fun getSubjectIdByName(name: String): Int? =
            subjects.firstOrNull { it.subject.equals(name, ignoreCase = true) }?.id

        override suspend fun getSubjectEntityById(id: Int): SubjectEntity? =
            subjects.firstOrNull { it.id == id }

        override suspend fun insertSubject(subject: SubjectEntity): Long {
            val id = if (subject.id == 0) nextSubjectId++ else subject.id
            subjects.removeAll { it.id == id }
            subjects += subject.copy(id = id)
            return id.toLong()
        }

        override suspend fun updateSubject(
            subjectId: Int, newName: String, newCode: String?, histogramLabel: String?
        ) {
            val index = subjects.indexOfFirst { it.id == subjectId }
            if (index >= 0) {
                subjects[index] = subjects[index].copy(
                    subject = newName, subjectCode = newCode, histogramLabel = histogramLabel
                )
            }
        }

        override fun getAllSubjects(): Flow<List<SubjectEntity>> = flowOf(subjects)
        override fun getSubjectById(id: Int): Flow<SubjectEntity> = flowOf(subjects.first { it.id == id })
        override suspend fun insertAllSubjects(subjects: List<SubjectEntity>) = Unit
        override suspend fun deleteSubject(subjectId: Int) { subjects.removeAll { it.id == subjectId } }
        override suspend fun deleteAllSubjects() = subjects.clear()
        override fun getSubjectCount(): Flow<Int> = flowOf(subjects.size)
        override suspend fun getAllSubjectsOnce(): List<SubjectEntity> = subjects
        override fun isSubjectExists(subject: String): Flow<Boolean> =
            flowOf(subjects.any { it.subject == subject })
        override suspend fun updateSavedMonthYear(subjectId: Int, month: Int, year: Int) = Unit
        override suspend fun updateTargetPercentage(subjectId: Int, targetPercentage: Float) = Unit
    }

    val timeTableDao = object : TimeTableDao {
        override suspend fun checkTimeClash(
            day: Int, start: Long, end: Long, excludeId: Int
        ): List<TimeTableScheduleEntity> = schedules.filter {
            it.dayOfWeek == day && it.isActive && it.id != excludeId &&
                it.startTime < end && it.endTime > start
        }

        override suspend fun insertClass(schedule: TimeTableScheduleEntity): Long {
            val id = nextScheduleId++
            schedules += schedule.copy(id = id)
            return id.toLong()
        }

        override fun getClassesForDayWithSubject(day: Int): Flow<List<TimeTableScheduleWithSubject>> =
            flowOf(emptyList())
        override fun getAllClassesWithSubject(): Flow<List<TimeTableScheduleWithSubject>> = flowOf(emptyList())
        override fun getClassesForDay(day: Int): Flow<List<TimeTableScheduleEntity>> = flowOf(emptyList())
        override fun getAllClasses(): Flow<List<TimeTableScheduleEntity>> = flowOf(schedules)
        override suspend fun updateClass(schedule: TimeTableScheduleEntity) = Unit
        override suspend fun deleteClass(id: Int) { schedules.removeAll { it.id == id } }
        override fun getSchedulesForSubject(subjectId: Int): Flow<List<TimeTableScheduleEntity>> =
            flowOf(schedules.filter { it.subjectId == subjectId })
        override suspend fun deleteSchedulesForSubject(subjectId: Int) {
            schedules.removeAll { it.subjectId == subjectId }
        }
        override suspend fun deleteClassesByIds(ids: List<Int>) { schedules.removeAll { it.id in ids } }
    }
}
