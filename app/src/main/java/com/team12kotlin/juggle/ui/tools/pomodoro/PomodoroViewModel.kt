package com.team12kotlin.juggle.ui.tools.pomodoro

import androidx.lifecycle.ViewModel
import com.team12kotlin.juggle.data.Dependencies
import kotlinx.coroutines.flow.StateFlow

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
    private val manager: PomodoroTimerManager = Dependencies.PomodoroTimerManager
): ViewModel() {
    val uiState: StateFlow<PomodoroUiState> = manager.uiState

    fun toggleTimer() = manager.toggleTimer()
    fun reset() = manager.reset()
    fun onNextPhase() = manager.onNextPhase()
    fun updateFocusMinutes(minutes: Int) = manager.updateFocusMinutes(minutes)
    fun updateBreakMinutes(minutes: Int) = manager.updateBreakMinutes(minutes)
}