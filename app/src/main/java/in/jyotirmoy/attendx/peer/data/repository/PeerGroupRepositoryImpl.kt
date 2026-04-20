package `in`.jyotirmoy.attendx.peer.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import `in`.jyotirmoy.attendx.core.data.service.AnonymousAuthService
import `in`.jyotirmoy.attendx.peer.data.model.PeerGroup
import `in`.jyotirmoy.attendx.peer.domain.repository.PeerGroupRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val GROUPS_COLLECTION = "peer_groups"

@Singleton
class PeerGroupRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authService: AnonymousAuthService // ensures anon sign-in before all Firestore ops
) : PeerGroupRepository {

    private val collection = firestore.collection(GROUPS_COLLECTION)

    /**
     * Real-time search. Calls getOrCreateUid() first so the anonymous auth token is
     * attached before the Firestore listener opens — prevents PERMISSION_DENIED.
     */
    override fun searchGroups(query: String): Flow<List<PeerGroup>> = callbackFlow {
        try {
            authService.getOrCreateUid()
        } catch (_: Exception) {
            trySend(emptyList())
            awaitClose {}
            return@callbackFlow
        }

        val firestoreQuery: Query = collection
            .orderBy("memberCount", Query.Direction.DESCENDING)
            .limit(50)

        val subscription = firestoreQuery.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val groups = snapshot.toObjects(PeerGroup::class.java)
                val filtered = if (query.isBlank()) groups else groups.filter {
                    it.college.contains(query, ignoreCase = true) ||
                    it.department.contains(query, ignoreCase = true) ||
                    it.displayName.contains(query, ignoreCase = true)
                }
                trySend(filtered)
            }
        }
        awaitClose { subscription.remove() }
    }

    /** Creates a canonical group. Ensures sign-in before writing. */
    override suspend fun createGroup(
        college: String,
        department: String,
        semester: Int
    ): Result<PeerGroup> {
        return try {
            authService.getOrCreateUid()
            val displayName = "${college.trim()} | ${department.trim()} | Semester $semester"
            val docRef = collection.document()
            val group = PeerGroup(
                id = docRef.id,
                displayName = displayName,
                college = college.trim(),
                department = department.trim(),
                semester = semester,
                memberCount = 0
            )
            docRef.set(group).await()
            Result.success(group)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getGroup(groupId: String): Result<PeerGroup> {
        return try {
            authService.getOrCreateUid()
            val snapshot = collection.document(groupId).get().await()
            val group = snapshot.toObject(PeerGroup::class.java)
            if (group != null) Result.success(group)
            else Result.failure(Exception("Group not found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
