package com.team12kotlin.juggle.data.remote

import com.team12kotlin.juggle.ui.dto.Group
import com.team12kotlin.juggle.ui.dto.GroupCreateRequest
import com.team12kotlin.juggle.ui.dto.GroupMemberAddRequest
import com.team12kotlin.juggle.ui.dto.GroupUpdateRequest
import com.team12kotlin.juggle.ui.dto.Project
import com.team12kotlin.juggle.ui.dto.ProjectCreateRequest
import com.team12kotlin.juggle.ui.dto.ProjectDeadlinePrediction
import com.team12kotlin.juggle.ui.dto.Reminder
import com.team12kotlin.juggle.ui.dto.ReminderRequest
import com.team12kotlin.juggle.ui.dto.ReminderUpdateRequest
import com.team12kotlin.juggle.ui.dto.ScreenLoadRequest
import com.team12kotlin.juggle.ui.dto.Task
import com.team12kotlin.juggle.ui.dto.TaskCreateRequest
import com.team12kotlin.juggle.ui.dto.TaskTodaySummary
import com.team12kotlin.juggle.ui.dto.TaskNotification
import com.team12kotlin.juggle.ui.dto.TaskUpdateRequest
import com.team12kotlin.juggle.ui.dto.TimeBlock
import com.team12kotlin.juggle.ui.dto.TimeBlockRequest
import com.team12kotlin.juggle.ui.dto.User
import com.team12kotlin.juggle.ui.dto.UserCreateRequest
import com.team12kotlin.juggle.ui.dto.UserLocation
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

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
    suspend fun getAllTasks(
        @Query("due_within_days") dueWithinDays: Int? = null,
        @Query("mine") mine: Boolean = false,
        @Query("priority") priority: Boolean = false,
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null
    ): List<Task>

    @GET("/tasks/today/summary")
    suspend fun getTodaySummary(
        @Query("start") start: String,
        @Query("end") end: String
    ): TaskTodaySummary

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

    @Multipart
    @PUT("/tasks/{task_id}/photo")
    suspend fun uploadTaskPhoto(
        @Path("task_id") taskId: Int,
        @Part file: MultipartBody.Part
    )

    @Streaming
    @GET("/tasks/{task_id}/photo")
    suspend fun getTaskPhoto(@Path("task_id") taskId: Int): ResponseBody

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

    @POST("/groups")
    suspend fun createGroup(@Body body: GroupCreateRequest): Group

    @PATCH("/groups/{group_id}")
    suspend fun updateGroup(
        @Path("group_id") groupId: Int,
        @Body body: GroupUpdateRequest
    ): Group

    @POST("/groups/{group_id}/members")
    suspend fun addGroupMember(
        @Path("group_id") groupId: Int,
        @Body body: GroupMemberAddRequest
    ): Group

    @DELETE("/groups/{group_id}/members/{member_user_id}")
    suspend fun removeGroupMember(
        @Path("group_id") groupId: Int,
        @Path("member_user_id") memberUserId: String
    )

    @DELETE("/groups/{group_id}/members/me")
    suspend fun leaveGroup(@Path("group_id") groupId: Int)

    @GET("/projects/group/{group_id}")
    suspend fun getProjects(@Path("group_id") groupId: Int): List<Project>

    @GET("/users/")
    suspend fun getUsers(): List<User>

    @GET("/projects/{project_id}")
    suspend fun getProject(@Path("project_id") projectId: Int): Project

    @POST("/projects")
    suspend fun createProject(@Body body: ProjectCreateRequest): Project

    @GET("/projects/{project_id}/deadline-prediction")
    suspend fun getDeadlinePrediction(
        @Path("project_id") projectId: Int
    ): ProjectDeadlinePrediction

    @GET("/users/current_user")
    suspend fun getCurrentUser(): User

    @GET("/users/me/location")
    suspend fun getLocation(): UserLocation

    @PUT("/users/me/location")
    suspend fun saveLocation(@Body body: UserLocation): UserLocation

    @DELETE("/users/me/location")
    suspend fun deleteLocation()

    @POST("/users/create_user")
    suspend fun createUser(@Body body: UserCreateRequest): User

    @GET("/notifications")
    suspend fun getNotifications(): List<TaskNotification>

    @POST("/telemetry/screen-load")
    suspend fun registerScreenLoad(@Body body: ScreenLoadRequest)
}
