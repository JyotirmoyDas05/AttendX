package `in`.jyotirmoy.attendx.core.data.repository

import `in`.jyotirmoy.attendx.core.data.database.CourseMaterialDao
import `in`.jyotirmoy.attendx.core.data.model.toDomain
import `in`.jyotirmoy.attendx.core.data.model.toEntity
import `in`.jyotirmoy.attendx.core.domain.model.CourseMaterial
import `in`.jyotirmoy.attendx.core.domain.repository.CourseMaterialRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CourseMaterialRepositoryImpl @Inject constructor(
    private val materialDao: CourseMaterialDao
) : CourseMaterialRepository {

    override fun getMaterialsForSubject(subjectId: Int): Flow<List<CourseMaterial>> {
        return materialDao.getMaterialsForSubject(subjectId).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun insertMaterial(material: CourseMaterial): Long {
        return materialDao.insertMaterial(material.toEntity())
    }

    override suspend fun deleteMaterial(material: CourseMaterial) {
        materialDao.deleteMaterial(material.toEntity())
    }

    override suspend fun deleteMaterialById(materialId: Int) {
        materialDao.deleteMaterialById(materialId)
    }
}
