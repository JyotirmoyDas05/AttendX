package `in`.jyotirmoy.attendx.core.domain.repository

import `in`.jyotirmoy.attendx.core.domain.model.CourseMaterial
import `in`.jyotirmoy.attendx.core.domain.model.SubjectTask
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getTasksForSubject(subjectId: Int): Flow<List<SubjectTask>>
    fun getAllPendingTasks(): Flow<List<SubjectTask>>
    suspend fun getTaskById(taskId: Int): SubjectTask?
    suspend fun insertTask(task: SubjectTask): Long
    suspend fun updateTask(task: SubjectTask)
    suspend fun updateTaskCompletion(taskId: Int, isCompleted: Boolean)
    suspend fun deleteTask(task: SubjectTask)
    suspend fun deleteTaskById(taskId: Int)
}

interface CourseMaterialRepository {
    fun getMaterialsForSubject(subjectId: Int): Flow<List<CourseMaterial>>
    suspend fun insertMaterial(material: CourseMaterial): Long
    suspend fun deleteMaterial(material: CourseMaterial)
    suspend fun deleteMaterialById(materialId: Int)
}
