package com.team12kotlin.juggle.data.repository

import com.team12kotlin.juggle.data.remote.JuggleApi
import com.team12kotlin.juggle.ui.dto.User
import com.team12kotlin.juggle.ui.dto.UserCreateRequest

class UserRepository(
    private val api: JuggleApi
) {
    suspend fun getCurrentUser(): User = api.getCurrentUser()

    suspend fun getUsers(): List<User> = api.getUsers()

    suspend fun createUser(request: UserCreateRequest): User = api.createUser(request)
}
