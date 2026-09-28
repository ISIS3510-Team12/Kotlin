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
    IN_PROGRESS
}

@Serializable
data class Task(
    val id: String,
    val title: String,
//    @SerialName("task_type")
//    val taskType: String,
//    val status: TaskStatus,
//    @SerialName("is_priority")
//    val isPriority: Boolean = false,
//    @SerialName("needs_help")
//    val needsHelp: Boolean = false,
//    val deadline: String? = null,
    val member: String? = null,
    val isImportant: Boolean = false,
    val dueLabel: String = "Tomorrow - 12 hours left"
)