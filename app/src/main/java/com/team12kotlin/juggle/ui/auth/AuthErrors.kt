package com.team12kotlin.juggle.ui.auth

import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.firebase.auth.FirebaseAuthException
import retrofit2.HttpException


internal fun Throwable.toAuthErrorMessage(): String = when (this) {
    is FirebaseAuthException -> when (errorCode) {
        "ERROR_INVALID_EMAIL" -> "Invalid email address."
        "ERROR_WRONG_PASSWORD", "ERROR_INVALID_CREDENTIAL" -> "Incorrect email or password."
        "ERROR_USER_NOT_FOUND" -> "No account found with that email."
        "ERROR_EMAIL_ALREADY_IN_USE" -> "An account with that email already exists."
        "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" ->
            "That email already has a password account. Sign in with your password."
        "ERROR_WEAK_PASSWORD" -> "Password is too weak."
        "ERROR_USER_DISABLED" -> "This account has been disabled."
        "ERROR_TOO_MANY_REQUESTS" -> "Too many attempts. Please try again later."
        "ERROR_NETWORK_REQUEST_FAILED" -> "Network error. Check your connection and try again."
        else -> message ?: "Authentication failed."
    }

    is GetCredentialCancellationException -> "Google sign in was cancelled."
    is NoCredentialException -> "No Google account found on this device."
    is GetCredentialException -> "Google sign in failed. Please try again."

    is HttpException -> when (code()) {
        400 -> "A profile for this account already exists."
        401 -> "Your session is invalid. Please sign in again."
        403 -> "No profile found for this account."
        else -> "Something went wrong (${code()})."
    }

    else -> message ?: "Something went wrong."
}
