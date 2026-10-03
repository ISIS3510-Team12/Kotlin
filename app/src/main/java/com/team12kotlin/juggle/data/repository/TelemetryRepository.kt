package com.team12kotlin.juggle.data.repository

import com.team12kotlin.juggle.data.remote.JuggleApi
import com.team12kotlin.juggle.ui.dto.ScreenLoadRequest

class TelemetryRepository(
    private val api: JuggleApi
) {
    suspend fun registerScreenLoad(screen: String, loadTimeMs: Double) {
        api.registerScreenLoad(ScreenLoadRequest(screen = screen, loadTimeMs = loadTimeMs))
    }
}
