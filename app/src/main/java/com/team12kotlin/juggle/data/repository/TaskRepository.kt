package com.team12kotlin.juggle.data.repository

import com.team12kotlin.juggle.data.remote.JuggleApi
import com.team12kotlin.juggle.ui.dto.Task
import com.team12kotlin.juggle.ui.dto.TaskCreateRequest
import com.team12kotlin.juggle.ui.dto.TaskStatus
import com.team12kotlin.juggle.ui.dto.TaskUpdateRequest

class TaskRepository(
    private val api: JuggleApi
) {
    suspend fun getTasks(): List<Task> = api.getTasks()

    suspend fun getTask(taskId: Int): Task = api.getTask(taskId)

    suspend fun createTask(request: TaskCreateRequest): Task = api.createTask(request)

    suspend fun updateTask(taskId: Int, request: TaskUpdateRequest): Task =
        api.updateTask(taskId, request)

    suspend fun changeStatus(taskId: Int, status: TaskStatus): Task =
        api.changeTaskStatus(taskId, status.serializedName)

    suspend fun deleteTask(taskId: Int) {
        api.deleteTask(taskId)
    }
}
