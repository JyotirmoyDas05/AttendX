package `in`.jyotirmoy.attendx.timetable.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import `in`.jyotirmoy.attendx.timetable.data.model.community.CommunityTemplate
import `in`.jyotirmoy.attendx.timetable.domain.repository.TemplateRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TemplateRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : TemplateRepository {

    private val templatesCollection = firestore.collection("templates")

    override suspend fun uploadTemplate(template: CommunityTemplate): Result<String> {
        return try {
            val docRef = templatesCollection.document()
            val templateWithId = template.copy(id = docRef.id)
            docRef.set(templateWithId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun searchTemplates(
        query: String,
        department: String?,
        semester: Int?
    ): Flow<List<CommunityTemplate>> = callbackFlow {
        // Basic query implementation - can be enhanced with Algolia later if searching needs to be more robust
        var firebaseQuery: Query = templatesCollection.orderBy("likes", Query.Direction.DESCENDING)

        if (department != null) {
            firebaseQuery = firebaseQuery.whereEqualTo("department", department)
        }
        if (semester != null) {
            firebaseQuery = firebaseQuery.whereEqualTo("semester", semester)
        }

        // Note: Firestore text search is limited. 
        // For free tier, we'll rely on client-side filtering for the name query if the dataset is small,
        // or just basic equality checks if we want to be strict.
        // For now, let's fetch top 50 and filter locally for search query to save reads.
        
        firebaseQuery = firebaseQuery.limit(50)

        val subscription = firebaseQuery.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val templates = snapshot.toObjects(CommunityTemplate::class.java)
                val filtered = if (query.isNotBlank()) {
                    templates.filter { 
                        it.name.contains(query, ignoreCase = true) || 
                        it.college.contains(query, ignoreCase = true) 
                    }
                } else {
                    templates
                }
                trySend(filtered)
            }
        }

        awaitClose { subscription.remove() }
    }

    override suspend fun getTemplateDetails(templateId: String): Result<CommunityTemplate> {
        return try {
            val snapshot = templatesCollection.document(templateId).get().await()
            val template = snapshot.toObject(CommunityTemplate::class.java)
            if (template != null) {
                Result.success(template)
            } else {
                Result.failure(Exception("Template not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun incrementDownloads(templateId: String) {
        try {
             templatesCollection.document(templateId)
                 .update("downloads", com.google.firebase.firestore.FieldValue.increment(1))
                 .await()
        } catch (e: Exception) {
            // Log error but don't crash
            e.printStackTrace()
        }
    }
}
