package `in`.jyotirmoy.attendx.core.data.service

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnonymousAuthService @Inject constructor() {
    private val auth = FirebaseAuth.getInstance()

    suspend fun getOrCreateUid(): String {
        val current = auth.currentUser
        if (current != null) return current.uid
        return auth.signInAnonymously().await().user!!.uid
    }
}
