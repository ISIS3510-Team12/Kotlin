package com.team12kotlin.juggle.ui.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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

private val NOTIFICATION_DATE: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a")

fun TaskNotification.toNotification(): Notification {
    val action = when (eventType) {
        "created" -> "Created"
        "updated" -> "Edited"
        "completed" -> "Completed"
        "deleted" -> "Deleted"
        else -> "Updated"
    }
    val type = when (eventType) {
        "created" -> "create"
        "updated" -> "edit"
        "completed" -> "complete"
        "deleted" -> "delete"
        else -> "edit"
    }
    return Notification(
        id = id,
        title = "$action: $taskTitle",
        date = formatNotificationDate(occurredAt),
        origin = groupName.orEmpty(),
        type = type
    )
}

private fun formatNotificationDate(value: String): String {
    return try {
        LocalDateTime.parse(value).format(NOTIFICATION_DATE)
    } catch (_: Exception) {
        value
    }
}
