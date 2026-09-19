package com.team12kotlin.juggle.ui.dto

import java.time.LocalDate

data class Task(
    val id: String,
    val title: String,
    val member: String? = null,
    val isImportant: Boolean = false,
    val dueLabel: String = "Tomorrow - 12 hours left",
    val tag: String? = null,
    val dueDate: LocalDate? = null
)