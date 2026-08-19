package `in`.jyotirmoy.attendx.core.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import `in`.jyotirmoy.attendx.core.data.model.CourseMaterialEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseMaterialDao {

    @Query("SELECT * FROM course_materials WHERE subjectId = :subjectId ORDER BY addedAt DESC")
    fun getMaterialsForSubject(subjectId: Int): Flow<List<CourseMaterialEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterial(material: CourseMaterialEntity): Long

    @Delete
    suspend fun deleteMaterial(material: CourseMaterialEntity)

    @Query("DELETE FROM course_materials WHERE id = :materialId")
    suspend fun deleteMaterialById(materialId: Int)
}
