package `in`.jyotirmoy.attendx.core.data.service

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import javax.inject.Inject
import javax.inject.Singleton

sealed interface GoogleIdTokenResult {
    data class Success(val idToken: String) : GoogleIdTokenResult
    data object Cancelled : GoogleIdTokenResult
    /** Google sign-in has not been set up in the Firebase console yet. */
    data object NotConfigured : GoogleIdTokenResult
    data class Failed(val error: Throwable) : GoogleIdTokenResult
}

/**
 * Fetches a Google ID token via Credential Manager (the replacement for the
 * retired GoogleSignInClient).
 *
 * Requires, in the Firebase console: Google enabled as a sign-in provider, the
 * SHA-1 of both the release and debug keystores registered, and a refreshed
 * google-services.json. Until that is done the generated web client id does not
 * exist and this reports [GoogleIdTokenResult.NotConfigured] rather than
 * crashing — which is also why the id is resolved by name at runtime instead of
 * as R.string.default_web_client_id, a reference that would not compile today.
 */
@Singleton
class GoogleSignInHelper @Inject constructor() {

    suspend fun getIdToken(context: Context): GoogleIdTokenResult {
        val webClientId = webClientId(context)
            ?: return GoogleIdTokenResult.NotConfigured

        return try {
            val option = GetGoogleIdOption.Builder()
                // false so the user can pick any account, not only ones already
                // authorized for this app — this is a first-time link.
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(option)
                .build()

            val credential = CredentialManager.create(context)
                .getCredential(context, request)
                .credential

            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                GoogleIdTokenResult.Success(
                    GoogleIdTokenCredential.createFrom(credential.data).idToken
                )
            } else {
                GoogleIdTokenResult.Failed(
                    IllegalStateException("Unexpected credential type: ${credential.type}")
                )
            }
        } catch (e: GetCredentialCancellationException) {
            GoogleIdTokenResult.Cancelled
        } catch (e: Exception) {
            GoogleIdTokenResult.Failed(e)
        }
    }

    @Suppress("DiscouragedApi")
    private fun webClientId(context: Context): String? {
        val id = context.resources.getIdentifier(
            "default_web_client_id", "string", context.packageName
        )
        return if (id == 0) null else context.getString(id).takeIf { it.isNotBlank() }
    }
}
