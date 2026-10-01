package com.team12kotlin.juggle.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class HealthResponse(
    val status: String = "",
    val version: String = ""
)
