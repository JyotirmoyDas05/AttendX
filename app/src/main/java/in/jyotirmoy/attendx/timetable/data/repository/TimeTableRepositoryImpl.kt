package `in`.jyotirmoy.attendx.timetable.data.repository

import `in`.jyotirmoy.attendx.timetable.data.dao.TimeTableDao
import `in`.jyotirmoy.attendx.timetable.data.model.TimeTableScheduleEntity
import `in`.jyotirmoy.attendx.timetable.data.model.TimeTableScheduleWithSubject
import `in`.jyotirmoy.attendx.timetable.domain.repository.TimeTableRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TimeTableRepositoryImpl @Inject constructor(
    private val dao: TimeTableDao
) : TimeTableRepository {
    
    override fun getClassesForDay(day: Int): Flow<List<TimeTableScheduleEntity>> {
        return dao.getClassesForDay(day)
    }

    override fun getClassesForDayWithSubject(day: Int): Flow<List<TimeTableScheduleWithSubject>> {
        return dao.getClassesForDayWithSubject(day)
    }

    override fun getAllClasses(): Flow<List<TimeTableScheduleEntity>> {
        return dao.getAllClasses()
    }

    override fun getAllClassesWithSubject(): Flow<List<TimeTableScheduleWithSubject>> {
        return dao.getAllClassesWithSubject()
    }

    override suspend fun insertClass(schedule: TimeTableScheduleEntity): Long {
        return dao.insertClass(schedule)
    }

    override suspend fun updateClass(schedule: TimeTableScheduleEntity) {
        dao.updateClass(schedule)
    }

    override suspend fun deleteClass(id: Int) {
        dao.deleteClass(id)
    }

    override suspend fun checkTimeClash(day: Int, start: Long, end: Long, excludeId: Int): List<TimeTableScheduleEntity> {
        return dao.checkTimeClash(day, start, end, excludeId)
    }
}
