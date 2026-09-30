package com.team12kotlin.juggle.data.remote

import com.team12kotlin.juggle.ui.dto.Group
import com.team12kotlin.juggle.ui.dto.Project
import com.team12kotlin.juggle.ui.dto.Reminder
import com.team12kotlin.juggle.ui.dto.ReminderRequest
import com.team12kotlin.juggle.ui.dto.ReminderUpdateRequest
import com.team12kotlin.juggle.ui.dto.Task
import com.team12kotlin.juggle.ui.dto.TaskCreateRequest
import com.team12kotlin.juggle.ui.dto.TaskUpdateRequest
import com.team12kotlin.juggle.ui.dto.TimeBlock
import com.team12kotlin.juggle.ui.dto.TimeBlockRequest
import com.team12kotlin.juggle.ui.dto.User
import com.team12kotlin.juggle.ui.dto.UserCreateRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface JuggleApi {

    @GET("/health")
    suspend fun health(): HealthResponse

    @GET("/tasks")
    suspend fun getTasks(): List<Task>

    @GET("/tasks/own/{group_id}")
    suspend fun getOwnTasks(@Path("group_id") groupId: Int): List<Task>

    @GET("/tasks/group/{group_id}")
    suspend fun getGroupTasks(@Path("group_id") groupId: Int): List<Task>

    @GET("/tasks/all")
    suspend fun getAllTasks(): List<Task>

    @GET("/tasks/{task_id}")
    suspend fun getTask(@Path("task_id") taskId: Int): Task

    @POST("/tasks")
    suspend fun createTask(@Body body: TaskCreateRequest): Task

    @PATCH("/tasks/{task_id}")
    suspend fun updateTask(
        @Path("task_id") taskId: Int,
        @Body body: TaskUpdateRequest
    ): Task

    @PATCH("/tasks/{task_id}/status")
    suspend fun changeTaskStatus(
        @Path("task_id") taskId: Int,
        @Query("status") status: String
    ): Task

    @DELETE("/tasks/{task_id}")
    suspend fun deleteTask(@Path("task_id") taskId: Int)

    @GET("/tasks/{task_id}/reminders")
    suspend fun getReminders(@Path("task_id") taskId: Int): List<Reminder>

    @POST("/tasks/{task_id}/reminders")
    suspend fun createReminder(
        @Path("task_id") taskId: Int,
        @Body body: ReminderRequest
    ): Reminder

    @PATCH("/tasks/{task_id}/reminders/{reminder_id}")
    suspend fun updateReminder(
        @Path("task_id") taskId: Int,
        @Path("reminder_id") reminderId: Int,
        @Body body: ReminderUpdateRequest
    ): Reminder

    @GET("/tasks/{task_id}/time-blocks")
    suspend fun getTimeBlocks(@Path("task_id") taskId: Int): List<TimeBlock>

    @POST("/tasks/{task_id}/time-blocks")
    suspend fun createTimeBlock(
        @Path("task_id") taskId: Int,
        @Body body: TimeBlockRequest
    ): TimeBlock

    @GET("/groups")
    suspend fun getGroups(): List<Group>

    @GET("/groups/{group_id}")
    suspend fun getGroup(@Path("group_id") groupId: Int): Group

    @GET("/projects/group/{group_id}")
    suspend fun getProjects(@Path("group_id") groupId: Int): List<Project>

    @GET("/users/current_user")
    suspend fun getCurrentUser(): User

    @POST("/users/create_user")
    suspend fun createUser(@Body body: UserCreateRequest): User
}
