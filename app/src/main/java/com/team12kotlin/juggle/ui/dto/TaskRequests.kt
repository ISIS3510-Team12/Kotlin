package com.team12kotlin.juggle.ui.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TaskCreateRequest(
    val title: String,
    @SerialName("task_type")
    val taskType: String,
    val description: String? = null,
    @SerialName("is_priority")
    val isPriority: Boolean = false,
    @SerialName("needs_help")
    val needsHelp: Boolean = false,
    val deadline: String,
    @SerialName("project_id")
    val projectId: Int? = null,
    @SerialName("assignee_ids")
    val assigneeIds: List<String> = emptyList(),
    @SerialName("related_task_ids")
    val relatedTaskIds: List<Int> = emptyList()
)

@Serializable
data class TaskUpdateRequest(
    val title: String? = null,
    @SerialName("task_type")
    val taskType: String? = null,
    val description: String? = null,
    @SerialName("is_priority")
    val isPriority: Boolean? = null,
    @SerialName("needs_help")
    val needsHelp: Boolean? = null,
    val deadline: String? = null,
    @SerialName("project_id")
    val projectId: Int? = null
)
