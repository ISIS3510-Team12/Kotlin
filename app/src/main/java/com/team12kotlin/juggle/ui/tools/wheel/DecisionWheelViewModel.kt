package com.team12kotlin.juggle.ui.tools.wheel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team12kotlin.juggle.data.Dependencies
import com.team12kotlin.juggle.data.repository.WheelRepository
import com.team12kotlin.juggle.ui.dto.Wheel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DecisionWheelUiState(
    val wheels: List<Wheel> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class DecisionWheelViewModel(
    private val repository: WheelRepository = Dependencies.wheelRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DecisionWheelUiState())
    val uiState: StateFlow<DecisionWheelUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadWheels()
    }

    /** Room keeps the list up to date, so a wheel created or deleted elsewhere shows up on its own. */
    fun loadWheels() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(errorMessage = null) }
            repository.wheels()
                .catch { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.toWheelMessage())
                    }
                }
                .collect { wheels ->
                    _uiState.update { it.copy(wheels = wheels, isLoading = false) }
                }
        }
    }
}
