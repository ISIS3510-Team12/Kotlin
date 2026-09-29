package com.team12kotlin.juggle.data.repository

import com.team12kotlin.juggle.data.remote.JuggleApi
import com.team12kotlin.juggle.ui.dto.User

class UserRepository(
    private val api: JuggleApi
) {
    suspend fun getCurrentUser(): User = api.getCurrentUser()
}
