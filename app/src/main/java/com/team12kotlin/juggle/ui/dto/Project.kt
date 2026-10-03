package com.team12kotlin.juggle.ui.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Project(
    val id: Int = 0,
    val name: String,
    val description: String = "",
    val deadline: String? = null,
    @SerialName("group_id")
    val groupId: Int = 0
)

@Serializable
data class ProjectCreateRequest(
    val name: String,
    val description: String = "",
    val deadline: String,
    @SerialName("group_id")
    val groupId: Int
)

@Serializable
data class ProjectDeadlinePrediction(
    @SerialName("project_id")
    val projectId: Int = 0,
    @SerialName("project_name")
    val projectName: String = "",
    val deadline: String? = null,
    @SerialName("days_left")
    val daysLeft: Double = 0.0,
    @SerialName("total_tasks")
    val totalTasks: Int = 0,
    @SerialName("completed_tasks")
    val completedTasks: Int = 0,
    @SerialName("remaining_tasks")
    val remainingTasks: Int = 0,
    @SerialName("pace_tasks_per_day")
    val paceTasksPerDay: Double = 0.0,
    @SerialName("predicted_completion_date")
    val predictedCompletionDate: String? = null,
    @SerialName("will_meet_deadline")
    val willMeetDeadline: Boolean = true,
)
