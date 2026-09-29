package com.team12kotlin.juggle.ui.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    @SerialName("user_id")
    val userId: String,
    @SerialName("first_name")
    val firstName: String,
    @SerialName("last_name")
    val lastName: String = "",
    val email: String = "",
    @SerialName("auth_provider")
    val authProvider: String = "",
    @SerialName("last_active_at")
    val lastActiveAt: String? = null,
    val major: String = "",
) {
    val displayName: String
        get() = "$firstName $lastName".trim()
}
