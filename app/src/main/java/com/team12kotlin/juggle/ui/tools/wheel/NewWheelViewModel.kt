package com.team12kotlin.juggle.ui.tools.wheel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team12kotlin.juggle.data.Dependencies
import com.team12kotlin.juggle.data.repository.WheelRepository
import com.team12kotlin.juggle.ui.dto.MAX_WHEEL_OPTIONS
import com.team12kotlin.juggle.ui.dto.MIN_WHEEL_OPTIONS
import com.team12kotlin.juggle.ui.utils.WheelFieldLimits
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NewWheelUiState(
    val name: String = "",
    val options: List<String> = emptyList(),
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val saved: Boolean = false
) {
    val canAddOption: Boolean
        get() = options.size < MAX_WHEEL_OPTIONS

    val canSave: Boolean
        get() = name.isNotBlank() && options.size >= MIN_WHEEL_OPTIONS && !isSaving
}

class NewWheelViewModel(
    private val repository: WheelRepository = Dependencies.wheelRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewWheelUiState())
    val uiState: StateFlow<NewWheelUiState> = _uiState.asStateFlow()

    fun onNameChange(name: String) {
        _uiState.update {
            it.copy(name = name.take(WheelFieldLimits.NAME_MAX_LENGTH), errorMessage = null)
        }
    }

    fun onAddOption(text: String) {
        val option = text.trim().take(WheelFieldLimits.OPTION_MAX_LENGTH)
        if (option.isEmpty()) return
        _uiState.update {
            if (it.canAddOption) it.copy(options = it.options + option, errorMessage = null)
            else it.copy(errorMessage = "A wheel can have up to $MAX_WHEEL_OPTIONS options.")
        }
    }

    fun onRemoveOption(index: Int) {
        _uiState.update {
            it.copy(options = it.options.filterIndexed { i, _ -> i != index }, errorMessage = null)
        }
    }

    /** Clears what the user entered. */
    fun onResetForm() {
        if (_uiState.value.isSaving) return
        _uiState.update { NewWheelUiState() }
    }

    fun onSave() {
        val state = _uiState.value
        if (!state.canSave) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching { repository.createWheel(state.name, state.options) }
                .onSuccess { _uiState.update { it.copy(isSaving = false, saved = true) } }
                .onFailure { error ->
                    _uiState.update { it.copy(isSaving = false, errorMessage = error.toWheelMessage()) }
                }
        }
    }
}
