package com.team12kotlin.juggle.data.repository

import com.team12kotlin.juggle.data.remote.JuggleApi
import com.team12kotlin.juggle.ui.dto.Reminder
import com.team12kotlin.juggle.ui.dto.ReminderRequest
import com.team12kotlin.juggle.ui.dto.ReminderUpdateRequest
import com.team12kotlin.juggle.ui.dto.Task
import com.team12kotlin.juggle.ui.dto.TaskCreateRequest
import com.team12kotlin.juggle.ui.dto.TaskStatus
import com.team12kotlin.juggle.ui.dto.TaskUpdateRequest
import com.team12kotlin.juggle.ui.dto.TimeBlock
import com.team12kotlin.juggle.ui.dto.TimeBlockRequest

class TaskRepository(
    private val api: JuggleApi
) {
    suspend fun getTasks(): List<Task> = api.getTasks()

    suspend fun getOwnTasks(groupId: Int): List<Task> = api.getOwnTasks(groupId)

    suspend fun getGroupTasks(groupId: Int): List<Task> = api.getGroupTasks(groupId)

    suspend fun getAllTasks(
        dueWithinDays: Int? = null,
        mine: Boolean = false,
        priority: Boolean = false
    ): List<Task> = api.getAllTasks(dueWithinDays, mine, priority)

    suspend fun getTask(taskId: Int): Task = api.getTask(taskId)

    suspend fun createTask(request: TaskCreateRequest): Task = api.createTask(request)

    suspend fun updateTask(taskId: Int, request: TaskUpdateRequest): Task =
        api.updateTask(taskId, request)

    suspend fun changeStatus(taskId: Int, status: TaskStatus): Task =
        api.changeTaskStatus(taskId, status.serializedName)

    suspend fun deleteTask(taskId: Int) {
        api.deleteTask(taskId)
    }

    // --- Reminders -------------------------------------------------------

    suspend fun getReminders(taskId: Int): List<Reminder> = api.getReminders(taskId)

    suspend fun createReminder(taskId: Int, request: ReminderRequest): Reminder =
        api.createReminder(taskId, request)

    suspend fun setReminderEnabled(taskId: Int, reminderId: Int, enabled: Boolean): Reminder =
        api.updateReminder(taskId, reminderId, ReminderUpdateRequest(enabled = enabled))

    // --- Time blocks -----------------------------------------------------

    suspend fun getTimeBlocks(taskId: Int): List<TimeBlock> = api.getTimeBlocks(taskId)

    suspend fun createTimeBlock(taskId: Int, request: TimeBlockRequest): TimeBlock =
        api.createTimeBlock(taskId, request)
}
