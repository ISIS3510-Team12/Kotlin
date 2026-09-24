package com.team12kotlin.juggle.ui.auth

import android.util.Patterns

object AuthValidation {

    const val MIN_PASSWORD_LENGTH = 8

    /** Returns an error if value is blank, otherwise null. */
    fun validateRequired(value: String, fieldName: String): String? =
        if (value.isBlank()) {
            "$fieldName is required"
        } else null

    /** Returns an error if value is blank or not a well-formed email address. */
    fun validateEmail(value: String): String? = when {
        value.isBlank() -> "Email is required"
        !Patterns.EMAIL_ADDRESS.matcher(value).matches() -> "Enter a valid email address"
        else -> null
    }

    /** Returns an error if value is blank or shorter than 8 characters. */
    fun validatePassword(value: String): String? = when {
        value.isBlank() -> "Password is required"
        value.length < MIN_PASSWORD_LENGTH ->
            "Password must be at least $MIN_PASSWORD_LENGTH characters"
        else -> null
    }

    /** Returns an error if confirm does not match [password]. */
    fun validatePasswordMatch(password: String, confirm: String): String? =
        if (password != confirm) "Passwords do not match" else null
}
