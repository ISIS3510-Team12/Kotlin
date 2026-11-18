package com.team12kotlin.juggle.ui.groups.create

data class CreateGroupDraft(
    val name: String = "",
    val description: String = "",
    val query: String = "",
    val selectedIds: Set<String> = emptySet(),
)
