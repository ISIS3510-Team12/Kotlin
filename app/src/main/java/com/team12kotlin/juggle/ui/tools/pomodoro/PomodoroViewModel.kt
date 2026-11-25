package com.team12kotlin.juggle.ui.tools.pomodoro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PomodoroPhase {FOCUS, SHORT_BREAK, LONG_BREAK}

data class PomodoroUiState(
    val phase: PomodoroPhase = PomodoroPhase.FOCUS,
    val focusMinutes: Int = 25,
    val shortBreakMinutes: Int = 5,
    val longBreakMinutes: Int = 10,
    val remainingSeconds: Int = 25*60,
    val totalSeconds: Int = 25*60,
    val isRunning: Boolean = false,
    val completedSessions: Int = 0,
) {
    val progress: Float get() = 1f -remainingSeconds /totalSeconds.toFloat()
}

class PomodoroViewModel: ViewModel() {
    private val _uiState = MutableStateFlow(PomodoroUiState())
    val uiState: StateFlow<PomodoroUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    fun toggleTimer() {
        if (_uiState.value.isRunning) pause() else start()
    }

    private fun start() {
        _uiState.update { it.copy(isRunning = true) }
        timerJob = viewModelScope.launch {
            while (_uiState.value.remainingSeconds > 0) {
                delay(1_000)
                _uiState.update { it.copy(remainingSeconds = it.remainingSeconds - 1) }
            }
            onNextPhase()
        }
    }

    private fun pause() {
        timerJob?.cancel()
        _uiState.update { it.copy(isRunning = false) }
    }

    fun reset() {
        timerJob?.cancel()
        _uiState.update {
            it.copy(remainingSeconds = it.totalSeconds, isRunning = false)
        }
    }

    fun onNextPhase() {
        val state = _uiState.value
        val (nextPhase, seconds) = when (state.phase) {
            PomodoroPhase.FOCUS -> {
                val done = state.completedSessions + 1
                if (done % 4 == 0) PomodoroPhase.LONG_BREAK to 15 * 60
                else PomodoroPhase.SHORT_BREAK to 5 * 60
            }
            else -> PomodoroPhase.FOCUS to 25 * 60
        }
        _uiState.update {
            it.copy(
                phase = nextPhase,
                remainingSeconds = seconds,
                totalSeconds = seconds,
                isRunning = false,
                completedSessions =
                    if (state.phase == PomodoroPhase.FOCUS) it.completedSessions + 1
                    else it.completedSessions
            )
        }
    }
}