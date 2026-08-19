package `in`.jyotirmoy.attendx.tools.data.repository

import `in`.jyotirmoy.attendx.tools.data.dao.TopicDao
import `in`.jyotirmoy.attendx.tools.data.model.Topic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface TopicRepository {
    fun getAllTopics(): Flow<List<Topic>>
    suspend fun getTopicById(id: Long): Topic?
    suspend fun saveTopic(topic: Topic): Long
    suspend fun deleteTopic(topic: Topic)
    suspend fun ensureDefaultTopic(): Topic
}

@Singleton
class AppTopicRepository @Inject constructor(
    private val topicDao: TopicDao
) : TopicRepository {

    override fun getAllTopics(): Flow<List<Topic>> {
        return topicDao.getAllTopics().map { list ->
            if (list.isEmpty()) {
                val defaultTopic = Topic(id = 1, name = "Default", colorHex = "#A8C7FA", shapeIndex = 0)
                topicDao.insertTopic(defaultTopic)
                listOf(defaultTopic)
            } else list
        }
    }

    override suspend fun getTopicById(id: Long): Topic? = topicDao.getTopicById(id)

    override suspend fun saveTopic(topic: Topic): Long = topicDao.insertTopic(topic)

    override suspend fun deleteTopic(topic: Topic) = topicDao.deleteTopic(topic)

    override suspend fun ensureDefaultTopic(): Topic {
        val topic = topicDao.getTopicById(1)
        return if (topic != null) topic
        else {
            val defaultTopic = Topic(id = 1, name = "Default", colorHex = "#A8C7FA", shapeIndex = 0)
            topicDao.insertTopic(defaultTopic)
            defaultTopic
        }
    }
}
