package com.team12kotlin.juggle.ui.auth

import com.google.firebase.auth.FirebaseUser
import com.team12kotlin.juggle.data.repository.AuthRepository
import com.team12kotlin.juggle.data.repository.UserRepository
import com.team12kotlin.juggle.ui.dto.UserCreateRequest
import retrofit2.HttpException

internal suspend fun registerBackendUser(
    authRepository: AuthRepository,
    userRepository: UserRepository,
    user: FirebaseUser
) {
    val (firstName, lastName) = user.displayNameParts()

    if (user.displayName.isNullOrBlank()) {
        authRepository.updateDisplayName("$firstName $lastName")
    }

    try {
        userRepository.createUser(
            UserCreateRequest(firstName = firstName, lastName = lastName)
        )
    } catch (error: HttpException) {
        if (error.code() != 400) throw error
    }
}

private fun FirebaseUser.displayNameParts(): Pair<String, String> {
    val name = displayName?.trim().orEmpty()
    if (name.isBlank()) {
        return email?.substringBefore('@').orEmpty() to ""
    }
    val parts = name.split(" ", limit = 2)
    return parts.first() to parts.getOrElse(1) { "" }
}
