package `in`.jyotirmoy.attendx.core.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import `in`.jyotirmoy.attendx.core.data.model.SubjectTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectTaskDao {

    @Query("SELECT * FROM subject_tasks WHERE subjectId = :subjectId ORDER BY isCompleted ASC, dueDate ASC, createdAt DESC")
    fun getTasksForSubject(subjectId: Int): Flow<List<SubjectTaskEntity>>

    @Query("SELECT * FROM subject_tasks WHERE isCompleted = 0 AND isNote = 0 ORDER BY dueDate ASC, createdAt DESC")
    fun getAllPendingTasks(): Flow<List<SubjectTaskEntity>>

    @Query("SELECT * FROM subject_tasks WHERE isCompleted = 0 AND isNote = 0 AND reminderMinutes IS NOT NULL AND dueDate IS NOT NULL")
    suspend fun getPendingTasksWithReminders(): List<SubjectTaskEntity>

    @Query("SELECT * FROM subject_tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Int): SubjectTaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: SubjectTaskEntity): Long

    @Update
    suspend fun updateTask(task: SubjectTaskEntity)

    @Query("UPDATE subject_tasks SET isCompleted = :isCompleted WHERE id = :taskId")
    suspend fun updateTaskCompletion(taskId: Int, isCompleted: Boolean)

    @Delete
    suspend fun deleteTask(task: SubjectTaskEntity)

    @Query("DELETE FROM subject_tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: Int)
}
