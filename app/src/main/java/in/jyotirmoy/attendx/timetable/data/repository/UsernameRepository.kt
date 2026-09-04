package `in`.jyotirmoy.attendx.timetable.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import `in`.jyotirmoy.attendx.core.data.service.AnonymousAuthService
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Username reservations backed by `usernames/{lowercased}`.
 *
 * Uniqueness needs no server: a Firestore create on a fixed document id fails
 * atomically when that id already exists, so the name itself is the lock. The
 * rules permit create but never update, which is why a rename is a batched
 * create-then-delete rather than an edit.
 */
@Singleton
class UsernameRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authService: AnonymousAuthService
) {
    private val usernames = firestore.collection("usernames")

    sealed interface ClaimResult {
        data class Claimed(val name: String) : ClaimResult
        data object Taken : ClaimResult
        data class Failed(val error: Throwable) : ClaimResult
    }

    suspend fun isAvailable(name: String): Result<Boolean> = runCatching {
        withTimeout(TIMEOUT_MS) {
            authService.getOrCreateUid()
            !usernames.document(name.lowercase()).get().await().exists()
        }
    }

    /**
     * Reserves [name], releasing [previous] if the user is renaming. Firestore
     * batches are atomic, so a rename cannot leave the user holding neither name.
     */
    suspend fun claim(name: String, previous: String? = null): ClaimResult {
        return try {
            withTimeout(TIMEOUT_MS) {
                val uid = authService.getOrCreateUid()
                val docId = name.lowercase()

                // Re-saving the same name would be an update, which the rules
                // forbid outright — it would come back as "taken" by the user's
                // own reservation.
                if (previous != null && previous.lowercase() == docId) {
                    authService.setDisplayName(name)
                    return@withTimeout ClaimResult.Claimed(name)
                }

                val batch = firestore.batch()
                // The rules allow create but not update, so this write fails with
                // PERMISSION_DENIED when the name is already reserved — that
                // rejection *is* the uniqueness check.
                batch.set(usernames.document(docId), mapOf("uid" to uid))
                previous?.lowercase()
                    ?.takeIf { it != docId }
                    ?.let { batch.delete(usernames.document(it)) }
                batch.commit().await()

                // Firebase Auth is the canonical store, so the name follows the
                // account to a new device instead of living only on this one.
                authService.setDisplayName(name)
                ClaimResult.Claimed(name)
            }
        } catch (e: Exception) {
            if (e.isPermissionDenied()) ClaimResult.Taken else ClaimResult.Failed(e)
        }
    }

    private fun Exception.isPermissionDenied(): Boolean =
        (this as? com.google.firebase.firestore.FirebaseFirestoreException)?.code ==
            com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED

    private companion object {
        const val TIMEOUT_MS = 15_000L
    }
}
