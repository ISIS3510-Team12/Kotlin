package com.team12kotlin.juggle.ui.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class TaskStatus {
    @SerialName("completed")
    COMPLETED,

    @SerialName("not started")
    NOT_STARTED,

    @SerialName("in_progress")
    IN_PROGRESS;

    val label: String
        get() = when (this) {
            COMPLETED -> "Completed"
            NOT_STARTED -> "Not started"
            IN_PROGRESS -> "In progress"
        }

    val serializedName: String
        get() = when (this) {
            COMPLETED -> "completed"
            NOT_STARTED -> "not started"
            IN_PROGRESS -> "in_progress"
        }
}

@Serializable
data class Reminder(
    val id: Int = 0,
    val enabled: Boolean = false,
    @SerialName("scheduled_at")
    val scheduledAt: String? = null,
    @SerialName("sent_at")
    val sentAt: String? = null,
    @SerialName("acted_at")
    val actedAt: String? = null,
    val label: String = ""
)

@Serializable
data class RelatedTask(
    val id: Int = 0,
    val title: String = "",
    val status: TaskStatus = TaskStatus.NOT_STARTED,
    @SerialName("needs_help")
    val needsHelp: Boolean = false,
    val deadline: String? = null
)

@Serializable
data class TimeBlock(
    val id: Int = 0,
    @SerialName("start_at")
    val startAt: String = "",
    @SerialName("end_at")
    val endAt: String = "",
    @SerialName("task_id")
    val taskId: Int = 0
)

@Serializable
data class Task(
    val id: Int = 0,
    val title: String,
    @SerialName("task_type")
    val taskType: String = "",
    val status: TaskStatus = TaskStatus.NOT_STARTED,
    @SerialName("is_priority")
    val isPriority: Boolean = false,
    @SerialName("needs_help")
    val needsHelp: Boolean = false,
    val deadline: String? = null,
    val description: String = "",
    @SerialName("user_id")
    val ownerId: String = "",
    @SerialName("project_id")
    val projectId: Int? = null,
    @SerialName("group_id")
    val groupId: Int? = null,
    @SerialName("has_photo")
    val hasPhoto: Boolean = false,
    val assignees: List<User> = emptyList(),
    val reminders: List<Reminder> = emptyList(),
    @SerialName("related_tasks")
    val relatedTasks: List<RelatedTask> = emptyList()
)
