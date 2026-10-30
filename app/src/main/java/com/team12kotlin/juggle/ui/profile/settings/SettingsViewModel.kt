package com.team12kotlin.juggle.ui.profile.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import com.team12kotlin.juggle.data.Dependencies
import com.team12kotlin.juggle.reminders.ScheduledReminderScheduler
import com.team12kotlin.juggle.reminders.ScheduledReminderStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SettingsUiState(
    val soundAndVibrationEnabled: Boolean = true,
    val dailyRemindersEnabled: Boolean = true,
    val dailyReminderHour: Int = 18,
    val dailyReminderMinute: Int = 0,
)

class SettingsViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val authRepository = Dependencies.authRepository
    private val reminderStore = ScheduledReminderStore(application.applicationContext)

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            dailyRemindersEnabled = reminderStore.enabled,
            dailyReminderHour = reminderStore.hour,
            dailyReminderMinute = reminderStore.minute
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun onSoundAndVibrationChange(enabled: Boolean) {
        _uiState.update { it.copy(soundAndVibrationEnabled = enabled) }
    }

    fun onDailyRemindersChange(enabled: Boolean) {
        reminderStore.enabled = enabled

        if (enabled) {
            ScheduledReminderScheduler.schedule(
                context = getApplication<Application>().applicationContext,
                hour = reminderStore.hour,
                minute = reminderStore.minute
            )
        } else {
            ScheduledReminderScheduler.cancel(
                getApplication<Application>().applicationContext
            )
        }

        _uiState.update { it.copy(dailyRemindersEnabled = enabled) }
    }

    fun onDailyReminderTimeChange(hour: Int, minute: Int) {
        reminderStore.hour = hour
        reminderStore.minute = minute

        if (reminderStore.enabled) {
            ScheduledReminderScheduler.schedule(
                context = getApplication<Application>().applicationContext,
                hour = hour,
                minute = minute
            )
        }

        _uiState.update {
            it.copy(dailyReminderHour = hour, dailyReminderMinute = minute)
        }
    }

    fun onThemeClick() {
        // TODO: theme selection is not designed yet, the app follows the system appearance
    }

    fun onSignOut(onSignedOut: () -> Unit) {
        authRepository.signOut()
        onSignedOut()
    }
}
