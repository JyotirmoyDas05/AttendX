package `in`.jyotirmoy.attendx.timetable.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import `in`.jyotirmoy.attendx.core.data.service.AnonymousAuthService
import `in`.jyotirmoy.attendx.timetable.data.model.community.CommunityTemplate
import `in`.jyotirmoy.attendx.timetable.domain.repository.TemplateRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TemplateRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authService: AnonymousAuthService
) : TemplateRepository {

    private val templatesCollection = firestore.collection("templates")

    override suspend fun uploadTemplate(template: CommunityTemplate): Result<String> {
        return try {
            withTimeout(TIMEOUT_MS) {
                // Not swallowed: without auth the write is a guaranteed rule
                // rejection, so fail here with a comprehensible message rather
                // than as an opaque PERMISSION_DENIED.
                val uid = authService.getOrCreateUid()
                val docRef = templatesCollection.document()

                // Identity and counters are never taken from the caller. The rules
                // reject anything else anyway; mirroring them here fails fast and
                // keeps the two definitions honest.
                val sanitized = template.copy(
                    id = docRef.id,
                    authorId = uid,
                    likes = 0,
                    downloads = 0,
                    version = 1,
                    createdAt = null,
                    updatedAt = null
                )
                docRef.set(sanitized).await()
                Result.success(docRef.id)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateTemplate(
        templateId: String,
        template: CommunityTemplate
    ): Result<Unit> {
        return try {
            withTimeout(TIMEOUT_MS) {
                authService.getOrCreateUid()

                // A partial update(), not set(): it touches only the mutable keys,
                // which is what makes the rules' affectedKeys().hasOnly(...) check
                // cheap to satisfy. increment(1) on a missing field resolves to 1,
                // matching the rules' get('version', 0) + 1 default for templates
                // published before versioning existed.
                templatesCollection.document(templateId).update(
                    mapOf(
                        "name" to template.name,
                        "college" to template.college,
                        "department" to template.department,
                        "semester" to template.semester,
                        "section" to template.section,
                        "academicYear" to template.academicYear,
                        "classes" to template.classes,
                        "subjects" to template.subjects,
                        "authorName" to template.authorName,
                        "updatedAt" to FieldValue.serverTimestamp(),
                        "version" to FieldValue.increment(1)
                    )
                ).await()
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun searchTemplates(
        query: String,
        department: String?,
        semester: Int?
    ): Flow<Result<List<CommunityTemplate>>> = callbackFlow {
        try {
            authService.getOrCreateUid()
        } catch (_: Exception) {
            // Proceed even if auth fails temporarily; the listener below reports
            // any resulting permission error as Result.failure.
        }

        var firebaseQuery: Query = templatesCollection.orderBy("likes", Query.Direction.DESCENDING)

        if (department != null) {
            firebaseQuery = firebaseQuery.whereEqualTo("department", department)
        }
        if (semester != null) {
            firebaseQuery = firebaseQuery.whereEqualTo("semester", semester)
        }

        firebaseQuery = firebaseQuery.limit(50)

        val subscription = firebaseQuery.addSnapshotListener { snapshot, error ->
            // Never close(error) here — see AGENTS.md 1B. The failure travels
            // in-band so the listener stays registered and can recover.
            if (error != null) {
                trySend(Result.failure(error))
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
                trySend(Result.success(filtered))
            }
        }

        awaitClose { subscription.remove() }
    }

    override fun getMyTemplates(): Flow<List<CommunityTemplate>> = callbackFlow {
        val uid = try {
            authService.getOrCreateUid()
        } catch (e: Exception) {
            // Not signed in yet: report "nothing owned" rather than tearing the
            // flow down, so a later retry can still populate the card.
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        // Equality only — adding orderBy() here would require a composite index
        // and a Firebase console step, so the sort happens client-side instead.
        val subscription = templatesCollection
            .whereEqualTo("authorId", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val templates = snapshot?.toObjects(CommunityTemplate::class.java)
                    ?.sortedByDescending { it.updatedAt ?: it.createdAt ?: EPOCH }
                    ?: emptyList()
                trySend(templates)
            }

        awaitClose { subscription.remove() }
    }

    override suspend fun getTemplateDetails(templateId: String): Result<CommunityTemplate> {
        return try {
            withTimeout(TIMEOUT_MS) {
                try { authService.getOrCreateUid() } catch (_: Exception) {}
                val snapshot = templatesCollection.document(templateId).get().await()
                val template = snapshot.toObject(CommunityTemplate::class.java)
                if (template != null) {
                    Result.success(template)
                } else {
                    Result.failure(Exception("Template not found"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun incrementDownloads(templateId: String) {
        try {
            withTimeout(SHORT_TIMEOUT_MS) {
                try { authService.getOrCreateUid() } catch (_: Exception) {}
                templatesCollection.document(templateId)
                    .update("downloads", FieldValue.increment(1))
                    .await()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private companion object {
        const val TIMEOUT_MS = 15_000L
        const val SHORT_TIMEOUT_MS = 5_000L
        val EPOCH = Timestamp(0, 0)
    }
}
