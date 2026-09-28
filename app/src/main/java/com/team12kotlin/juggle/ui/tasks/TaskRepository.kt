package com.team12kotlin.juggle.ui.tasks

import com.team12kotlin.juggle.ui.dto.Reminder
import com.team12kotlin.juggle.ui.dto.Task
import com.team12kotlin.juggle.ui.dto.TaskStatus
import com.team12kotlin.juggle.ui.dto.User

// This is a mock
object TaskRepository {

    private val diego = User(firstName = "Diego", email = "diego@juggle.app", major = "CS")
    private val manuela = User(firstName = "Manuela", email = "manuela@juggle.app", major = "CS")
    private val shaiel = User(firstName = "Shaiel", email = "shaiel@juggle.app", major = "CS")

    val personalTasks: List<Task> = listOf(
        Task(
            id = "p1",
            title = "Finish the figma",
            description = "Create the figma for small class exercise and upcoming MS for sprint 2",
            taskType = "Design",
            status = TaskStatus.IN_PROGRESS,
            isPriority = true,
            isImportant = true,
            deadline = "September 5, 2026",
            reminder = Reminder(label = "1 day before", enabled = true),
            members = listOf(diego, manuela, shaiel),
            relatedTasks = listOf(Task(id = "p4", title = "Finish Something bruh"),Task(id = "p4", title = "Finish Something bruh"),)
        ),
        Task(
            id = "p2",
            title = "Terminar",
            description = "Wrap up the pending items for this week.",
            taskType = "Coding",
            status = TaskStatus.NOT_STARTED,
            deadline = "September 8, 2026",
            members = listOf(diego)
        ),
        Task(
            id = "p3",
            title = "Work",
            description = "General work item.",
            taskType = "Coding",
            status = TaskStatus.NOT_STARTED,
            members = listOf(diego)
        ),
        Task(id = "p4", title = "Finish Something bruh"),
        Task(id = "p5", title = "Terminar"),
        Task(id = "p6", title = "Work")
    )

    val groupTasks: List<Task> = listOf(
        Task(
            id = "g1",
            title = "Finish Something bruh",
            description = "Group deliverable for sprint 2.",
            taskType = "Design",
            status = TaskStatus.IN_PROGRESS,
            isPriority = true,
            isImportant = true,
            deadline = "September 5, 2026",
            reminder = Reminder(label = "1 day before", enabled = true),
            member = "Diego",
            members = listOf(diego, manuela, shaiel)
        ),
        Task(
            id = "g2",
            title = "Terminar",
            description = "Finish the remaining group tasks.",
            taskType = "Coding",
            status = TaskStatus.NOT_STARTED,
            deadline = "September 9, 2026",
            member = "Manuela",
            members = listOf(manuela)
        ),
        Task(
            id = "g3",
            title = "Work",
            description = "Group work item.",
            taskType = "Research",
            status = TaskStatus.NOT_STARTED,
            member = "Shaiel",
            members = listOf(shaiel)
        )
    )

    private val tasksById: Map<String, Task> =
        (personalTasks + groupTasks).associateBy { it.id }

    fun findById(id: String): Task? = tasksById[id]
}
