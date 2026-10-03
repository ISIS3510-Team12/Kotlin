package com.team12kotlin.juggle.data.telemetry

import android.util.Log
import com.team12kotlin.juggle.data.repository.TelemetryRepository
import com.team12kotlin.juggle.ui.dto.TaskDetailSessionRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

private const val TAG = "Telemetry"

/** Sends analytics events without waiting for the caller, so they still go out after a screen is closed. */
class TelemetryReporter(
    private val repository: TelemetryRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun reportTaskDetailSession(
        taskId: Int,
        openedAt: LocalDateTime,
        closedAt: LocalDateTime,
        progressUpdated: Boolean
    ) {
        scope.launch {
            runCatching {
                repository.registerTaskDetailSession(
                    TaskDetailSessionRequest(
                        taskId = taskId,
                        openedAt = openedAt.toApiDateTime(),
                        closedAt = closedAt.toApiDateTime(),
                        progressUpdated = progressUpdated
                    )
                )
            }.onFailure { Log.d(TAG, "task detail session not sent: ${it.message}") }
        }
    }
}

/** Local date-time with millisecond precision, the format the back accepts. */
fun LocalDateTime.toApiDateTime(): String = truncatedTo(ChronoUnit.MILLIS).toString()
