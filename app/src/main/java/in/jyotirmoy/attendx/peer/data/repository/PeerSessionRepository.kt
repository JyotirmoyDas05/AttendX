package `in`.jyotirmoy.attendx.peer.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import `in`.jyotirmoy.attendx.core.data.service.AnonymousAuthService
import `in`.jyotirmoy.attendx.peer.data.model.PeerSessionDoc
import `in`.jyotirmoy.attendx.peer.data.model.PeerSubjectPayload
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

import kotlinx.coroutines.withTimeout

private const val SESSIONS_COLLECTION = "peer_sessions"
private const val TIMEOUT_MS = 15_000L

@Singleton
class PeerSessionRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authService: AnonymousAuthService
) {
    private val collection = firestore.collection(SESSIONS_COLLECTION)

    /**
     * Host creates a new session, writes their subject data.
     * Returns the generated sessionId on success.
     */
    suspend fun createSession(subjects: List<PeerSubjectPayload>): Result<String> {
        return try {
            withTimeout(TIMEOUT_MS) {
                val uid = authService.getOrCreateUid()
                val docRef = collection.document()
                val doc = hashMapOf(
                    "sessionId"   to docRef.id,
                    "status"      to PeerSessionDoc.STATUS_WAITING,
                    "hostAnonUid" to uid,
                    "guestAnonUid" to null,
                    "hostData"    to subjects.map { it.toMap() },
                    "guestData"   to emptyList<Map<String, Any>>(),
                    "createdAt"   to Timestamp.now()
                )
                docRef.set(doc).await()
                Result.success(docRef.id)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Guest joins an existing WAITING session by writing their subject data.
     * Returns the full session doc (with hostData) on success.
     */
    suspend fun joinSession(
        sessionId: String,
        subjects: List<PeerSubjectPayload>
    ): Result<PeerSessionDoc> {
        return try {
            withTimeout(TIMEOUT_MS) {
                val uid = authService.getOrCreateUid()
                val docRef = collection.document(sessionId)
                val snapshot = docRef.get().await()

                if (!snapshot.exists()) return@withTimeout Result.failure(Exception("Session not found"))

                val status = snapshot.getString("status") ?: PeerSessionDoc.STATUS_EXPIRED
                if (status != PeerSessionDoc.STATUS_WAITING) {
                    return@withTimeout Result.failure(Exception("Session is no longer available ($status)"))
                }

                docRef.update(
                    mapOf(
                        "guestAnonUid" to uid,
                        "guestData"    to subjects.map { it.toMap() },
                        "status"       to PeerSessionDoc.STATUS_PAIRED
                    )
                ).await()

                // Re-fetch to get hostData for the guest
                val updated = docRef.get().await()
                Result.success(updated.toSessionDoc())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Host listens for the session doc to change to PAIRED.
     * Emits null if the document doesn't exist or there's an error.
     */
    fun observeSession(sessionId: String): Flow<PeerSessionDoc?> = callbackFlow {
        var registration: ListenerRegistration? = null
        try {
            authService.getOrCreateUid()
        } catch (e: Exception) {
            trySend(null)
            awaitClose {}
            return@callbackFlow
        }

        registration = collection.document(sessionId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                trySend(snapshot.toSessionDoc())
            }

        awaitClose { registration?.remove() }
    }

    /**
     * Mark session as EXPIRED — called when host cancels or TTL fires on client.
     */
    suspend fun expireSession(sessionId: String) {
        try {
            collection.document(sessionId)
                .update("status", PeerSessionDoc.STATUS_EXPIRED)
                .await()
        } catch (_: Exception) {}
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    @Suppress("UNCHECKED_CAST")
    private fun com.google.firebase.firestore.DocumentSnapshot.toSessionDoc(): PeerSessionDoc {
        val hostRaw  = get("hostData")  as? List<Map<String, Any>> ?: emptyList()
        val guestRaw = get("guestData") as? List<Map<String, Any>> ?: emptyList()
        return PeerSessionDoc(
            sessionId    = getString("sessionId") ?: id,
            status       = getString("status") ?: PeerSessionDoc.STATUS_EXPIRED,
            hostAnonUid  = getString("hostAnonUid") ?: "",
            guestAnonUid = getString("guestAnonUid"),
            hostData     = hostRaw.map { it.toPayload() },
            guestData    = guestRaw.map { it.toPayload() },
            createdAt    = getTimestamp("createdAt")
        )
    }

    private fun PeerSubjectPayload.toMap(): Map<String, Any?> = mapOf(
        "subjectCode"           to subjectCode,
        "normalizedName"        to normalizedName,
        "originalName"          to originalName,
        "attendancePercentage"  to attendancePercentage
    )

    private fun Map<String, Any>.toPayload() = PeerSubjectPayload(
        subjectCode          = get("subjectCode") as? String,
        normalizedName       = get("normalizedName") as? String ?: "",
        originalName         = get("originalName") as? String ?: "",
        attendancePercentage = (get("attendancePercentage") as? Number)?.toFloat() ?: 0f
    )
}
