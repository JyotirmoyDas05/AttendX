package `in`.jyotirmoy.attendx.timetable.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import `in`.jyotirmoy.attendx.timetable.data.model.TimeTableScheduleEntity
import `in`.jyotirmoy.attendx.timetable.data.model.TimeTableScheduleWithSubject
import kotlinx.coroutines.flow.Flow

@Dao
interface TimeTableDao {

    @Transaction
    @Query("""
        SELECT * FROM class_schedule 
        WHERE dayOfWeek = :day 
        AND isActive = 1 
        ORDER BY startTime ASC
    """)
    fun getClassesForDayWithSubject(day: Int): Flow<List<TimeTableScheduleWithSubject>>

    @Transaction
    @Query("SELECT * FROM class_schedule WHERE isActive = 1")
    fun getAllClassesWithSubject(): Flow<List<TimeTableScheduleWithSubject>>

    @Query("""
        SELECT * FROM class_schedule 
        WHERE dayOfWeek = :day 
        AND isActive = 1 
        ORDER BY startTime ASC
    """)
    fun getClassesForDay(day: Int): Flow<List<TimeTableScheduleEntity>>

    @Query("SELECT * FROM class_schedule WHERE isActive = 1")
    fun getAllClasses(): Flow<List<TimeTableScheduleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClass(schedule: TimeTableScheduleEntity): Long

    @Update
    suspend fun updateClass(schedule: TimeTableScheduleEntity)

    @Query("DELETE FROM class_schedule WHERE id = :id")
    suspend fun deleteClass(id: Int)
    
    @Query("""
        SELECT * FROM class_schedule 
        WHERE dayOfWeek = :day 
        AND isActive = 1
        AND id != :excludeId
        AND (
            (startTime < :end AND endTime > :start)
        )
    """)
    suspend fun checkTimeClash(day: Int, start: Long, end: Long, excludeId: Int = -1): List<TimeTableScheduleEntity>

    @Query("SELECT * FROM class_schedule WHERE subjectId = :subjectId AND isActive = 1 ORDER BY dayOfWeek ASC, startTime ASC")
    fun getSchedulesForSubject(subjectId: Int): Flow<List<TimeTableScheduleEntity>>

    @Query("DELETE FROM class_schedule WHERE subjectId = :subjectId")
    suspend fun deleteSchedulesForSubject(subjectId: Int)

    @Query("DELETE FROM class_schedule WHERE id IN (:ids)")
    suspend fun deleteClassesByIds(ids: List<Int>)
}
