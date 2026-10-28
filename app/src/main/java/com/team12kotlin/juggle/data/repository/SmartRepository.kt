package com.team12kotlin.juggle.data.repository

import com.team12kotlin.juggle.data.remote.JuggleApi
import com.team12kotlin.juggle.ui.dto.NextTaskResponse

class SmartRepository(
    private val api: JuggleApi
) {
    suspend fun getNextTask(): NextTaskResponse = api.getNextTask()
}
