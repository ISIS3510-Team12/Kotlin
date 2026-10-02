package com.team12kotlin.juggle.ui.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TaskNotification(
    val id: Int = 0,
    @SerialName("task_id")
    val taskId: Int = 0,
    @SerialName("task_title")
    val taskTitle: String = "",
    @SerialName("group_id")
    val groupId: Int? = null,
    @SerialName("group_name")
    val groupName: String? = null,
    @SerialName("event_type")
    val eventType: String = "",
    @SerialName("occurred_at")
    val occurredAt: String = "",
    @SerialName("author_id")
    val authorId: String = ""
)
