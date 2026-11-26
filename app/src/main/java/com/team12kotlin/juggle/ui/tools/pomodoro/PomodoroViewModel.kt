package com.team12kotlin.juggle.ui.tools.pomodoro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team12kotlin.juggle.data.Dependencies
import com.team12kotlin.juggle.data.local.PomodoroSettingsStore
import com.team12kotlin.juggle.data.local.cache_structs.PomodoroSessionCache
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class PomodoroPhase {FOCUS, BREAK, FINISHED}

data class PomodoroUiState(
    val phase: PomodoroPhase = PomodoroPhase.FOCUS,
    val focusMinutes: Int = 25,
    val breakMinutes: Int = 5,
    val remainingSeconds: Int = 25*60,
    val totalSeconds: Int = 25*60,
    val isRunning: Boolean = false,
    val completedSessions: Int = 0,
) {
    val progress: Float get() = 1f -remainingSeconds /totalSeconds.toFloat()

    val nextPhase: PomodoroPhase
        get() = when (phase) {
            PomodoroPhase.FOCUS ->
                if ((completedSessions + 1) % 4 == 0) PomodoroPhase.FINISHED else PomodoroPhase.BREAK
            PomodoroPhase.BREAK, PomodoroPhase.FINISHED -> PomodoroPhase.FOCUS
        }

    val nextPhaseMinutes: Int
        get() = when (nextPhase) {
            PomodoroPhase.FOCUS -> focusMinutes
            PomodoroPhase.BREAK -> breakMinutes
            PomodoroPhase.FINISHED -> 0
        }
}

class PomodoroViewModel(
    private val settings: PomodoroSettingsStore = Dependencies.pomodoroSettingsStore,
    private val cache: PomodoroSessionCache = Dependencies.pomodoroSessionCache
): ViewModel() {
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

    private var timerJob: Job? = null

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
        timerJob = viewModelScope.launch {
            while (isActive && _uiState.value.isRunning) {
                delay(1_000)
                val remaining = _uiState.value.remainingSeconds - 1
                if (remaining > 0) {
                    _uiState.update { it.copy(remainingSeconds = remaining) }
                } else {
                    // Termina la fase y arranca la siguiente sin detenerse
                    advancePhase(keepRunning = true)
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