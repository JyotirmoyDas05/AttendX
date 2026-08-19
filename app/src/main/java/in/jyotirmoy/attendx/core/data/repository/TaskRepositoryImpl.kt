package `in`.jyotirmoy.attendx.core.data.repository

import `in`.jyotirmoy.attendx.core.data.database.SubjectTaskDao
import `in`.jyotirmoy.attendx.core.data.model.toDomain
import `in`.jyotirmoy.attendx.core.data.model.toEntity
import `in`.jyotirmoy.attendx.core.domain.model.SubjectTask
import `in`.jyotirmoy.attendx.core.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val taskDao: SubjectTaskDao
) : TaskRepository {

    override fun getTasksForSubject(subjectId: Int): Flow<List<SubjectTask>> {
        return taskDao.getTasksForSubject(subjectId).map { list -> list.map { it.toDomain() } }
    }

    override fun getAllPendingTasks(): Flow<List<SubjectTask>> {
        return taskDao.getAllPendingTasks().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getTaskById(taskId: Int): SubjectTask? {
        return taskDao.getTaskById(taskId)?.toDomain()
    }

    override suspend fun insertTask(task: SubjectTask): Long {
        return taskDao.insertTask(task.toEntity())
    }

    override suspend fun updateTask(task: SubjectTask) {
        taskDao.updateTask(task.toEntity())
    }

    override suspend fun updateTaskCompletion(taskId: Int, isCompleted: Boolean) {
        taskDao.updateTaskCompletion(taskId, isCompleted)
    }

    override suspend fun deleteTask(task: SubjectTask) {
        taskDao.deleteTask(task.toEntity())
    }

    override suspend fun deleteTaskById(taskId: Int) {
        taskDao.deleteTaskById(taskId)
    }
}
