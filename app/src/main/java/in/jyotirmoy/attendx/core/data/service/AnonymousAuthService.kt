package `in`.jyotirmoy.attendx.core.data.service

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/** Outcome of attempting to attach a Google account to the anonymous one. */
sealed interface LinkResult {
    /** The anonymous uid was upgraded in place; everything it owns stays owned. */
    data class Linked(val uid: String) : LinkResult

    /**
     * This Google account already belongs to a different Firebase uid — usually
     * because the user linked it on another device. Signing into that account is
     * what they want, but it abandons the current anonymous uid along with
     * anything published from this install that was never linked. Needs an
     * explicit confirmation, so it is surfaced rather than resolved here.
     */
    data object AlreadyLinkedElsewhere : LinkResult

    data class Failed(val error: Throwable) : LinkResult
}

@Singleton
class AnonymousAuthService @Inject constructor() {
    private val auth = FirebaseAuth.getInstance()

    suspend fun getOrCreateUid(): String {
        val current = auth.currentUser
        if (current != null) return current.uid
        return auth.signInAnonymously().await().user?.uid
            ?: throw IllegalStateException("Firebase Anonymous Auth returned null user")
    }

    fun currentUid(): String? = auth.currentUser?.uid

    fun isLinked(): Boolean = auth.currentUser?.isAnonymous == false

    fun displayName(): String? = auth.currentUser?.displayName?.takeIf { it.isNotBlank() }

    /** Emits on sign-in, sign-out, link and profile refresh. */
    fun authState(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    /**
     * Attaches a Google credential to the *existing* anonymous user. The uid is
     * preserved, which is the whole point: templates already published from this
     * install keep matching authorId, so nothing needs migrating.
     */
    suspend fun linkWithGoogle(idToken: String): LinkResult {
        return try {
            val user = auth.currentUser ?: run {
                getOrCreateUid()
                auth.currentUser
            } ?: return LinkResult.Failed(IllegalStateException("No Firebase user to link"))

            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val linked = user.linkWithCredential(credential).await().user
            LinkResult.Linked(linked?.uid ?: user.uid)
        } catch (e: FirebaseAuthUserCollisionException) {
            LinkResult.AlreadyLinkedElsewhere
        } catch (e: Exception) {
            LinkResult.Failed(e)
        }
    }

    /**
     * Abandons the current anonymous uid and signs into the pre-existing account.
     * Only call after the user has confirmed the consequence spelled out on
     * [LinkResult.AlreadyLinkedElsewhere].
     */
    suspend fun signInWithGoogleDiscardingAnonymous(idToken: String): LinkResult {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val user = auth.signInWithCredential(credential).await().user
            LinkResult.Linked(user?.uid ?: return LinkResult.Failed(
                IllegalStateException("Google sign-in returned null user")
            ))
        } catch (e: Exception) {
            LinkResult.Failed(e)
        }
    }

    /** Canonical store for the author's chosen username; survives reinstall. */
    suspend fun setDisplayName(name: String) {
        auth.currentUser?.updateProfile(
            userProfileChangeRequest { displayName = name }
        )?.await()
    }
}
