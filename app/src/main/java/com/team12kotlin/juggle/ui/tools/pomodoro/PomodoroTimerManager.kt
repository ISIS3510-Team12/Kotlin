package com.team12kotlin.juggle.ui.tools.pomodoro

import android.content.Context
import android.os.SystemClock
import com.team12kotlin.juggle.data.local.PomodoroSettingsStore
import com.team12kotlin.juggle.data.local.cache_structs.PomodoroSessionCache
import com.team12kotlin.juggle.services.PomodoroService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.ceil

class PomodoroTimerManager(
    private val appContext: Context,
    private val settings: PomodoroSettingsStore,
    private val cache: PomodoroSessionCache
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var timerJob: Job? = null
    private var endAtMillis = 0L

    private val _uiState = MutableStateFlow(
        PomodoroUiState(
            focusMinutes = settings.focusMinutes,
            breakMinutes = settings.breakMinutes,
            remainingSeconds = settings.focusMinutes *60,
            totalSeconds = settings.focusMinutes *60,
            completedSessions = cache.getCompletedSessions()
        )
    )
    val uiState: StateFlow<PomodoroUiState> = _uiState.asStateFlow()

    fun toggleTimer() {
        if (_uiState.value.isRunning) pause()
        else if (_uiState.value.phase == PomodoroPhase.FINISHED) {
            reset()
            start()
        }
        else start()
    }

    private fun start() {
        timerJob?.cancel()
        _uiState.update { it.copy(isRunning = true) }
        PomodoroService.start(appContext)          // después de marcar isRunning

        endAtMillis = SystemClock.elapsedRealtime() + _uiState.value.remainingSeconds * 1000L

        timerJob = scope.launch {
            while (isActive && _uiState.value.isRunning) {
                delay(250)
                val remaining = endAtMillis - SystemClock.elapsedRealtime()
                if (remaining > 0) {
                    val secs = ceil(remaining / 1000.0).toInt()
                    _uiState.update { it.copy(remainingSeconds = secs) }
                } else {
                    advancePhase(keepRunning = true)
                    val s = _uiState.value
                    if (!s.isRunning) break
                    endAtMillis = SystemClock.elapsedRealtime() + s.remainingSeconds * 1000L
                }
            }
        }
    }

    private fun pause() {
        timerJob?.cancel()
        _uiState.update { it.copy(isRunning = false) }
    }

    fun reset() {
        timerJob?.cancel()
        val uiState = _uiState.value
        if (uiState.phase == PomodoroPhase.FINISHED ||
            (!uiState.isRunning && uiState.totalSeconds == uiState.remainingSeconds)
        ) {
            cache.putCompletedSessions(0)
            _uiState.update { it.copy(
                phase = PomodoroPhase.FOCUS,
                remainingSeconds = it.focusMinutes * 60,
                totalSeconds = it.focusMinutes * 60,
                isRunning = false,
                completedSessions = 0
            ) }
        } else {
            _uiState.update {
                it.copy(remainingSeconds = it.totalSeconds, isRunning = false)
            }
        }
    }

    fun updateFocusMinutes(minutes: Int) {
        val value = minutes.coerceIn(1, 99)
        settings.focusMinutes = value
        _uiState.update {
            if (it.phase == PomodoroPhase.FOCUS && !it.isRunning)
                it.copy(focusMinutes = value, remainingSeconds = value * 60, totalSeconds = value *60)
            else it.copy(focusMinutes = value)
        }
    }

    fun updateBreakMinutes(minutes: Int) {
        val value = minutes.coerceIn(1, 99)
        settings.breakMinutes = value
        _uiState.update {
            if (it.phase == PomodoroPhase.BREAK && !it.isRunning)
                it.copy(breakMinutes = value, remainingSeconds = value * 60, totalSeconds = value *60)
            else it.copy(breakMinutes = value)
        }
    }

    fun onNextPhase() {
        val wasRunning = _uiState.value.isRunning
        timerJob?.cancel()
        advancePhase(keepRunning = wasRunning)
        if (_uiState.value.isRunning) start()
    }

    private fun advancePhase(keepRunning: Boolean) {
        val state = _uiState.value

        val completed = when (state.phase) {
            PomodoroPhase.FOCUS -> state.completedSessions + 1
            PomodoroPhase.FINISHED -> 0
            PomodoroPhase.BREAK -> state.completedSessions
        }
        cache.putCompletedSessions(completed)

        val next = state.nextPhase
        val seconds = state.nextPhaseMinutes * 60

        _uiState.update {
            it.copy(
                phase = next,
                remainingSeconds = seconds,
                totalSeconds = seconds,
                isRunning = keepRunning && next != PomodoroPhase.FINISHED,
                completedSessions = completed
            )
        }
    }
}