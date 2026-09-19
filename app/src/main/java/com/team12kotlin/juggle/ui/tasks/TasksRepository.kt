package com.team12kotlin.juggle.ui.tasks

import com.team12kotlin.juggle.ui.dto.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate

/**
 * In-memory stand-in for a tasks data source, shared across screens (Tasks,
 * All Tasks, Calendar) so a task defined here shows up consistently everywhere
 * instead of being duplicated as separate per-screen mock data.
 */
object TasksRepository {

    private val today = LocalDate.now()

    private val _groupTasks = MutableStateFlow(
        listOf(
            Task(
                id = "shared-figma-review",
                title = "Finish the figma",
                member = "Diego",
                tag = "Needs Help.",
                dueLabel = "Tomorrow - 12 hours left",
                dueDate = today
            ),
            Task(
                id = "shared-review-pr",
                title = "Review pull request",
                member = "Manuela",
                dueLabel = "Tomorrow - 12 hours left",
                dueDate = today.plusDays(1)
            ),
            Task(
                id = "shared-sprint-slides",
                title = "Prepare sprint slides",
                member = "Diego",
                isImportant = true,
                dueLabel = "In 3 days",
                dueDate = today.plusDays(3)
            ),
            Task(
                id = "shared-ms4-report",
                title = "Submit MS4 report",
                member = "Shaiel",
                dueLabel = "2 days ago",
                dueDate = today.minusDays(2)
            )
        )
    )
    val groupTasks: StateFlow<List<Task>> = _groupTasks.asStateFlow()
}
