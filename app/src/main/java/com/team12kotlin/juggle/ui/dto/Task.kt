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
    val label: String,
    val enabled: Boolean = false
)

@Serializable
data class Task(
    val id: String,
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
    val ownerId: String? = null,
    @SerialName("project_id")
    val projectId: String? = null,
    val members: List<User> = emptyList(),
    val reminder: Reminder? = null,
    val relatedTasks: List<Task> = emptyList(),
    val member: String? = null,
    val isImportant: Boolean = false,
    val dueLabel: String = "Tomorrow - 12 hours left"
)
