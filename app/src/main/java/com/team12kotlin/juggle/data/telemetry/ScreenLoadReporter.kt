package com.team12kotlin.juggle.data.telemetry

import com.team12kotlin.juggle.data.Dependencies
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object ScreenLoadReporter {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun report(screen: String, loadTimeMs: Long) {
        if (Dependencies.authRepository.currentUser == null) return
        scope.launch {
            runCatching {
                Dependencies.telemetryRepository.registerScreenLoad(screen, loadTimeMs.toDouble())
            }
        }
    }
}
