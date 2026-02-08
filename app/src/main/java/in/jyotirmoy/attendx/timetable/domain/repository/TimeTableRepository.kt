package `in`.jyotirmoy.attendx.timetable.domain.repository

import `in`.jyotirmoy.attendx.timetable.data.model.TimeTableScheduleEntity
import `in`.jyotirmoy.attendx.timetable.data.model.TimeTableScheduleWithSubject
import kotlinx.coroutines.flow.Flow

interface TimeTableRepository {
    fun getClassesForDay(day: Int): Flow<List<TimeTableScheduleEntity>>
    
    fun getClassesForDayWithSubject(day: Int): Flow<List<TimeTableScheduleWithSubject>>
    
    fun getAllClasses(): Flow<List<TimeTableScheduleEntity>>
    
    fun getAllClassesWithSubject(): Flow<List<TimeTableScheduleWithSubject>>
    
    suspend fun insertClass(schedule: TimeTableScheduleEntity): Long
    
    suspend fun updateClass(schedule: TimeTableScheduleEntity)
    
    suspend fun deleteClass(id: Int)
    
    suspend fun checkTimeClash(day: Int, start: Long, end: Long, excludeId: Int = -1): List<TimeTableScheduleEntity>

    fun getSchedulesForSubject(subjectId: Int): Flow<List<TimeTableScheduleEntity>>

    suspend fun deleteSchedulesForSubject(subjectId: Int)

    suspend fun deleteClassesByIds(ids: List<Int>)
}
