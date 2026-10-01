package com.team12kotlin.juggle.data.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.team12kotlin.juggle.BuildConfig

// Taken from https://firebase.google.com/docs/auth/android/google-signin
suspend fun requestGoogleIdToken(context: Context): String {
    val credentialManager = CredentialManager.create(context)

    val result = try {
        credentialManager.getCredential(context, googleIdRequest(authorizedAccountsOnly = true))
    } catch (_: NoCredentialException) {
        credentialManager.getCredential(context, googleIdRequest(authorizedAccountsOnly = false))
    }

    val credential = result.credential
    if (credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        error("Unexpected credential type: ${credential.type}")
    }
    return GoogleIdTokenCredential.createFrom(credential.data).idToken
}

private fun googleIdRequest(authorizedAccountsOnly: Boolean): GetCredentialRequest =
    GetCredentialRequest.Builder()
        .addCredentialOption(
            GetGoogleIdOption.Builder()
                .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                .setFilterByAuthorizedAccounts(authorizedAccountsOnly)
                .build()
        )
        .build()
