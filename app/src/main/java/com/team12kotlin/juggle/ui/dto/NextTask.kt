package com.team12kotlin.juggle.ui.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NextTask(
    @SerialName("task_id")
    val taskId: Int = 0,
    val title: String = "",
    @SerialName("group_id")
    val groupId: Int = 0,
    @SerialName("group_name")
    val groupName: String = "",
    @SerialName("due_at")
    val dueAt: String? = null,
    @SerialName("reason_code")
    val reasonCode: String = "",
    @SerialName("reason_text")
    val reasonText: String = ""
) {
    val reason: NextTaskReason
        get() = NextTaskReason.fromCode(reasonCode)
}

@Serializable
data class NextTaskResponse(
    val task: NextTask? = null,
    val message: String? = null
)

enum class NextTaskReason {
    OVERDUE,
    DUE_TODAY,
    DUE_SOON,
    PRIORITY,
    NEEDS_HELP,
    NEXT_DEADLINE;

    companion object {
        fun fromCode(code: String): NextTaskReason = when (code) {
            "overdue" -> OVERDUE
            "due_today" -> DUE_TODAY
            "due_soon" -> DUE_SOON
            "priority" -> PRIORITY
            "needs_help" -> NEEDS_HELP
            else -> NEXT_DEADLINE
        }
    }
}

data class NextTaskFlag(
    val reason: NextTaskReason,
    val text: String
)

fun NextTask.toFlag(): NextTaskFlag? =
    if (reasonText.isBlank()) null else NextTaskFlag(reason = reason, text = reasonText)

fun NextTask.accessibilityLabel(): String =
    if (reasonText.isBlank()) "Next task: $title" else "Next task: $title. $reasonText"
