package com.team12kotlin.juggle.ui.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TaskDetailSessionRequest(
    @SerialName("task_id")
    val taskId: Int,
    @SerialName("opened_at")
    val openedAt: String,
    @SerialName("closed_at")
    val closedAt: String,
    @SerialName("progress_updated")
    val progressUpdated: Boolean
)
