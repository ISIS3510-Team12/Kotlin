package com.team12kotlin.juggle.ui.profile.settings

import androidx.lifecycle.ViewModel
import com.team12kotlin.juggle.data.Dependencies
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SettingsUiState(
    val soundAndVibrationEnabled: Boolean = true
)

class SettingsViewModel : ViewModel() {

    private val authRepository = Dependencies.authRepository

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun onSoundAndVibrationChange(enabled: Boolean) {
        _uiState.update { it.copy(soundAndVibrationEnabled = enabled) }
    }

    fun onThemeClick() {
        // TODO: theme selection is not designed yet, the app follows the system appearance
    }

    fun onSignOut(onSignedOut: () -> Unit) {
        authRepository.signOut()
        onSignedOut()
    }
}
