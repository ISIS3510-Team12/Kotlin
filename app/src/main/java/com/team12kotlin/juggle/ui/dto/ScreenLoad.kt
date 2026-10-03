package com.team12kotlin.juggle.ui.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ScreenLoadRequest(
    val screen: String,
    @SerialName("load_time_ms")
    val loadTimeMs: Double
)
