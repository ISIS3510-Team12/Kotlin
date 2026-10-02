package com.team12kotlin.juggle.ui.profile.settings.location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team12kotlin.juggle.data.Dependencies
import com.team12kotlin.juggle.data.repository.LocationRepository
import com.team12kotlin.juggle.ui.dto.MAX_NOTIFY_WITHIN_METERS
import com.team12kotlin.juggle.ui.dto.UserLocation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val DEFAULT_RADIUS_METERS = 500
private const val MAX_RADIUS_DIGITS = 6

data class LocationRemindersUiState(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val radiusText: String = DEFAULT_RADIUS_METERS.toString(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val saved: Boolean = false
) {
    val hasLocation: Boolean
        get() = latitude != null && longitude != null

    private val radiusMeters: Int?
        get() = radiusText.toIntOrNull()

    /** Shown under the radius field; null when the typed radius is valid. */
    val radiusError: String?
        get() = when {
            radiusText.isEmpty() -> "Enter a radius in meters"
            radiusMeters == null -> "Enter a valid number"
            radiusMeters!! < 1 -> "The radius must be at least 1 m"
            radiusMeters!! > MAX_NOTIFY_WITHIN_METERS -> "The maximum radius is 1 km (1000 m)"
            else -> null
        }

    val canSave: Boolean
        get() = hasLocation && radiusError == null && !isLoading
}

class LocationRemindersViewModel(
    private val locationRepository: LocationRepository = Dependencies.locationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LocationRemindersUiState())
    val uiState: StateFlow<LocationRemindersUiState> = _uiState.asStateFlow()

    init {
        loadLocation()
    }

    private fun loadLocation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { locationRepository.getLocation() }
                .onSuccess { location ->
                    _uiState.update {
                        if (location == null) {
                            it.copy(isLoading = false)
                        } else {
                            it.copy(
                                latitude = location.latitude,
                                longitude = location.longitude,
                                radiusText = location.notifyWithin.toString(),
                                isLoading = false
                            )
                        }
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
        }
    }

    fun onChooseOnMapClick() {
        // TODO: open the Google Maps picker (next step); it will call onLocationPicked
    }

    fun onLocationPicked(latitude: Double, longitude: Double) {
        _uiState.update { it.copy(latitude = latitude, longitude = longitude) }
    }

    fun onRadiusChange(text: String) {
        // Digits only; an over-limit value is kept so the field can warn instead of silently changing it.
        val digits = text.filter { it.isDigit() }.take(MAX_RADIUS_DIGITS)
        _uiState.update { it.copy(radiusText = digits) }
    }

    fun onSaveLocation() {
        val state = _uiState.value
        if (!state.canSave) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                locationRepository.saveLocation(
                    UserLocation(
                        latitude = state.latitude!!,
                        longitude = state.longitude!!,
                        notifyWithin = state.radiusText.toInt()
                    )
                )
            }
                .onSuccess { _uiState.update { it.copy(isLoading = false, saved = true) } }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
        }
    }
}
