package com.team12kotlin.juggle.data.remote

import com.team12kotlin.juggle.ui.dto.Group
import com.team12kotlin.juggle.ui.dto.Task
import com.team12kotlin.juggle.ui.dto.TaskCreateRequest
import com.team12kotlin.juggle.ui.dto.TaskUpdateRequest
import com.team12kotlin.juggle.ui.dto.User
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

    @GET("/groups")
    suspend fun getGroups(): List<Group>

    @GET("/groups/{group_id}")
    suspend fun getGroup(@Path("group_id") groupId: Int): Group

    @GET("/users/current_user")
    suspend fun getCurrentUser(): User
}
