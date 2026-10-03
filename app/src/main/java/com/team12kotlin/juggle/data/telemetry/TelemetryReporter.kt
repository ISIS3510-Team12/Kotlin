package com.team12kotlin.juggle.data.telemetry

import com.team12kotlin.juggle.data.Dependencies
import com.team12kotlin.juggle.ui.dto.ScreenLoadRequest
import com.team12kotlin.juggle.ui.dto.TaskDetailSessionRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

object TelemetryReporter {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun reportScreenLoad(screen: String, loadTimeMs: Long) {
        if (Dependencies.authRepository.currentUser == null) return
        scope.launch {
            runCatching {
                Dependencies.telemetryRepository.registerScreenLoad(
                    ScreenLoadRequest(screen = screen, loadTimeMs = loadTimeMs.toDouble())
                )
            }
        }
    }

    fun reportTaskDetailSession(
        taskId: Int,
        openedAt: LocalDateTime,
        closedAt: LocalDateTime,
        progressUpdated: Boolean
    ) {
        if (Dependencies.authRepository.currentUser == null) return
        scope.launch {
            runCatching {
                Dependencies.telemetryRepository.registerTaskDetailSession(
                    TaskDetailSessionRequest(
                        taskId = taskId,
                        openedAt = openedAt.toApiDateTime(),
                        closedAt = closedAt.toApiDateTime(),
                        progressUpdated = progressUpdated
                    )
                )
            }
        }
    }
}

fun LocalDateTime.toApiDateTime(): String = truncatedTo(ChronoUnit.MILLIS).toString()
