package com.team12kotlin.juggle.ui.tasks.create

data class CreateTaskDraft(
    val selectedGroupId: Int? = null,
    val title: String = "",
    val taskType: String? = null,
    val selectedProjectId: Int? = null,
    val selectedMemberIds: Set<String> = emptySet(),
    val deadline: String = "",
    val time: String = "",
    val isPriority: Boolean = false,
    val needsHelp: Boolean = false,
    val notes: String = "",
    val evidenceBytes: ByteArray? = null,
    val evidenceMimeType: String? = null,
    val evidenceDirty: Boolean = false,
    val selectedRelatedTaskIds: Set<Int> = emptySet(),
    val relatedQuery: String = ""
)
