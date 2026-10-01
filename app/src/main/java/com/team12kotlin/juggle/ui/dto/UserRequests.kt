package com.team12kotlin.juggle.ui.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserCreateRequest(
    @SerialName("first_name")
    val firstName: String,
    @SerialName("last_name")
    val lastName: String,
)
