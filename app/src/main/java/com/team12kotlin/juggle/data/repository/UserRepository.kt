package com.team12kotlin.juggle.data.repository

import com.team12kotlin.juggle.data.local.ProfileCache
import com.team12kotlin.juggle.data.remote.JuggleApi
import com.team12kotlin.juggle.ui.dto.User
import com.team12kotlin.juggle.ui.dto.UserCreateRequest

class UserRepository(
    private val api: JuggleApi,
    private val cache: ProfileCache
) {
    suspend fun getCurrentUser(): User =
        try {
            api.getCurrentUser().also { cache.save(it) }
        } catch (e: java.io.IOException) {
            cache.read() ?: throw e
        }

    suspend fun getUsers(): List<User> = api.getUsers()

    suspend fun createUser(request: UserCreateRequest): User = api.createUser(request)
}
