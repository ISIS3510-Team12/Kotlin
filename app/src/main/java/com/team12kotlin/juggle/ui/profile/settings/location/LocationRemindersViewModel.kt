package com.team12kotlin.juggle.ui.profile.settings.location

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

val NOTIFY_RADIUS_OPTIONS_METERS = listOf(100, 250, 500, 1000)

fun formatRadius(meters: Int): String =
    if (meters >= 1000) "${meters / 1000} km" else "$meters m"

data class LocationRemindersUiState(
    val radiusMeters: Int = 500,
    val isRadiusDialogVisible: Boolean = false
)

class LocationRemindersViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LocationRemindersUiState())
    val uiState: StateFlow<LocationRemindersUiState> = _uiState.asStateFlow()

    fun onChooseOnMapClick() {
        // TODO: open a map picker once a maps provider is added to the project
    }

    fun onRadiusClick() {
        _uiState.update { it.copy(isRadiusDialogVisible = true) }
    }

    fun onRadiusDialogDismiss() {
        _uiState.update { it.copy(isRadiusDialogVisible = false) }
    }

    fun onRadiusSelected(meters: Int) {
        _uiState.update { it.copy(radiusMeters = meters, isRadiusDialogVisible = false) }
    }

    fun onSaveLocation() {
        // TODO: persist the saved place and radius once the backend supports location reminders
    }
}
