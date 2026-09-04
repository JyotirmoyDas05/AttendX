package `in`.jyotirmoy.attendx.timetable.domain.repository

import `in`.jyotirmoy.attendx.timetable.data.model.community.CommunityTemplate
import kotlinx.coroutines.flow.Flow

interface TemplateRepository {
    suspend fun uploadTemplate(template: CommunityTemplate): Result<String>
    suspend fun updateTemplate(templateId: String, template: CommunityTemplate): Result<Unit>
    /**
     * Emits Result rather than closing the flow on error. AGENTS.md 1B forbids
     * close(error) in a callbackFlow snapshot listener, and closing would also
     * tear down the listener permanently on a transient failure.
     */
    fun searchTemplates(
        query: String,
        department: String?,
        semester: Int?
    ): Flow<Result<List<CommunityTemplate>>>

    /**
     * Templates authored by the current uid. Ownership lives server-side as
     * [CommunityTemplate.authorId], so once an anonymous account is linked to a
     * Google account these reappear on any device the user signs in on — no
     * local bookkeeping required.
     */
    fun getMyTemplates(): Flow<List<CommunityTemplate>>

    suspend fun getTemplateDetails(templateId: String): Result<CommunityTemplate>
    suspend fun incrementDownloads(templateId: String)
}
