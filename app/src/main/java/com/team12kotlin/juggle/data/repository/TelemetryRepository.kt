package com.team12kotlin.juggle.data.repository

import com.team12kotlin.juggle.data.remote.JuggleApi
import com.team12kotlin.juggle.ui.dto.TaskDetailSessionRequest

class TelemetryRepository(
    private val api: JuggleApi
) {
    suspend fun registerTaskDetailSession(request: TaskDetailSessionRequest) {
        api.registerTaskDetailSession(request)
    }
}
