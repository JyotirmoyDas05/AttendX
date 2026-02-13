package `in`.jyotirmoy.attendx.timetable.domain.repository

import `in`.jyotirmoy.attendx.timetable.data.model.community.CommunityTemplate
import kotlinx.coroutines.flow.Flow

interface TemplateRepository {
    suspend fun uploadTemplate(template: CommunityTemplate): Result<String>
    fun searchTemplates(query: String, department: String?, semester: Int?): Flow<List<CommunityTemplate>>
    suspend fun getTemplateDetails(templateId: String): Result<CommunityTemplate>
    suspend fun incrementDownloads(templateId: String)
}
