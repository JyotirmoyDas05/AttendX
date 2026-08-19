package `in`.jyotirmoy.attendx.tools.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import `in`.jyotirmoy.attendx.tools.data.model.Topic
import kotlinx.coroutines.flow.Flow

@Dao
interface TopicDao {
    @Query("SELECT * FROM pomodoro_topics ORDER BY id ASC")
    fun getAllTopics(): Flow<List<Topic>>

    @Query("SELECT * FROM pomodoro_topics WHERE id = :id LIMIT 1")
    suspend fun getTopicById(id: Long): Topic?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopic(topic: Topic): Long

    @Update
    suspend fun updateTopic(topic: Topic)

    @Delete
    suspend fun deleteTopic(topic: Topic)

    @Query("DELETE FROM pomodoro_topics")
    suspend fun clearTopics()
}
