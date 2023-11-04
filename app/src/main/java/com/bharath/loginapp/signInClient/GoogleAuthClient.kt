package com.bharath.loginapp.signInClient

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import com.bharath.loginapp.BuildConfig
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

class GoogleAuthClient(
    private val context: Context,
    private val oneTapClient: SignInClient,

    ) {
    private val auth = Firebase.auth

    suspend fun signIn(): IntentSender? {
        val result = try {

            oneTapClient.beginSignIn(
                buildSignInRequest()
            ).await()
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
            null
        }
        return result?.pendingIntent?.intentSender
    }

    data class SignInResult(
        val data: UserData?,
        val errorMessage: String?,
    )

    data class UserData(
        val userId: String = "",
        val userName: String = "",
        val profilePictureUrl: String? = "",
        val email: String? = "",
    )

    suspend fun signInWithInten(intent: Intent): SignInResult {
        val credentails = oneTapClient.getSignInCredentialFromIntent(intent)
        val googleIdToken = credentails.googleIdToken

        val googleCredentials = GoogleAuthProvider.getCredential(googleIdToken, null)
        return try {
            val user = auth.signInWithCredential(googleCredentials).await().user
            SignInResult(
                data = user?.run {
                    UserData(
                        userId = uid,
                        userName = displayName.toString(),
                        profilePictureUrl = photoUrl.toString(),
                        email
                    )
                },
                errorMessage = null
            )

        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
            SignInResult(
                data = null,
                errorMessage = "Login Session Cancelled"
            )

        }

    }

    private fun buildSignInRequest(): BeginSignInRequest {

        return BeginSignInRequest.Builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.Builder()
                    .setSupported(true)
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(BuildConfig.Google_Client_Id)
                    .build()
            )
            .build()
    }


    fun signOut() {
        auth.signOut()
    }
}