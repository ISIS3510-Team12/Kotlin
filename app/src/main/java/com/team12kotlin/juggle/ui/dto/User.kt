package com.team12kotlin.juggle.ui.dto

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val firstName: String,
    val email: String,
    val major: String,
)
