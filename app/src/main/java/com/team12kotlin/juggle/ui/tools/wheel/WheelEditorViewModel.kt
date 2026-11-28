package com.team12kotlin.juggle.ui.tools.wheel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team12kotlin.juggle.data.Dependencies
import com.team12kotlin.juggle.data.repository.WheelRepository
import com.team12kotlin.juggle.ui.dto.MAX_WHEEL_OPTIONS
import com.team12kotlin.juggle.ui.dto.MIN_WHEEL_OPTIONS
import com.team12kotlin.juggle.ui.dto.WheelOption
import com.team12kotlin.juggle.ui.utils.WheelFieldLimits
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

/** The winner is picked before the wheel moves; [id] tells two spins to the same option apart. */
data class SpinTarget(val id: Int, val index: Int, val optionCount: Int)

data class WheelEditorUiState(
    val wheelId: Int = 0,
    val name: String = "",
    val options: List<WheelOption> = emptyList(),
    val isLoading: Boolean = true,
    val spinTarget: SpinTarget? = null,
    val result: WheelOption? = null,
    val errorMessage: String? = null,
    val deleted: Boolean = false
) {
    val isSpinning: Boolean
        get() = spinTarget != null

    val canSpin: Boolean
        get() = options.size >= MIN_WHEEL_OPTIONS && !isSpinning && result == null

    val canAddOption: Boolean
        get() = options.size < MAX_WHEEL_OPTIONS
}

class WheelEditorViewModel(
    private val repository: WheelRepository = Dependencies.wheelRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WheelEditorUiState())
    val uiState: StateFlow<WheelEditorUiState> = _uiState.asStateFlow()

    private var loadedWheelId: Int? = null
    private var spinCount = 0

    fun load(wheelId: Int) {
        if (loadedWheelId == wheelId) return
        loadedWheelId = wheelId

        viewModelScope.launch {
            _uiState.update { it.copy(wheelId = wheelId, isLoading = true, errorMessage = null) }
            runCatching { repository.getWheel(wheelId) to repository.getOptions(wheelId) }
                .onSuccess { (wheel, options) ->
                    // A wheel that is gone (for example deleted from another session) has nothing to show.
                    if (wheel == null) {
                        _uiState.update { it.copy(isLoading = false, deleted = true) }
                    } else {
                        _uiState.update {
                            it.copy(name = wheel.name, options = options, isLoading = false)
                        }
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.toWheelMessage()) }
                }
        }
    }

    fun onSpin() {
        val state = _uiState.value
        if (!state.canSpin) return

        val index = Random.nextInt(state.options.size)
        _uiState.update {
            it.copy(spinTarget = SpinTarget(++spinCount, index, state.options.size))
        }
        // The history is saved while the wheel turns, so the animation never waits for the database.
        viewModelScope.launch {
            runCatching { repository.recordSpin(state.wheelId, state.options[index].text) }
        }
    }

    fun onSpinFinished() {
        _uiState.update { state ->
            val target = state.spinTarget ?: return@update state
            state.copy(spinTarget = null, result = state.options.getOrNull(target.index))
        }
    }

    fun onResultDismissed() {
        _uiState.update { it.copy(result = null) }
    }

    fun onSpinAgain() {
        _uiState.update { it.copy(result = null) }
        onSpin()
    }

    fun onRemoveResult() {
        val option = _uiState.value.result ?: return
        _uiState.update { it.copy(result = null) }
        deleteOption(option)
    }

    fun onAddOption(text: String) {
        val wheelId = _uiState.value.wheelId
        applyChange { repository.addOption(wheelId, text.take(WheelFieldLimits.OPTION_MAX_LENGTH)) }
    }

    fun onEditOption(option: WheelOption, text: String) {
        val wheelId = _uiState.value.wheelId
        applyChange { repository.editOption(wheelId, option.id, text.take(WheelFieldLimits.OPTION_MAX_LENGTH)) }
    }

    fun onDeleteOption(option: WheelOption) = deleteOption(option)

    fun onRename(name: String) {
        val wheelId = _uiState.value.wheelId
        applyChange {
            repository.renameWheel(wheelId, name.take(WheelFieldLimits.NAME_MAX_LENGTH))
            _uiState.update { it.copy(name = name.trim().take(WheelFieldLimits.NAME_MAX_LENGTH)) }
        }
    }

    fun onDeleteWheel() {
        val wheelId = _uiState.value.wheelId
        applyChange {
            repository.deleteWheel(wheelId)
            _uiState.update { it.copy(deleted = true) }
        }
    }

    fun onErrorShown() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun deleteOption(option: WheelOption) {
        val wheelId = _uiState.value.wheelId
        applyChange { repository.deleteOption(wheelId, option.id) }
    }

    /** Runs a change and then reloads the options, which are the source of truth. */
    private fun applyChange(change: suspend () -> Unit) {
        val wheelId = _uiState.value.wheelId
        viewModelScope.launch {
            runCatching {
                change()
                repository.getOptions(wheelId)
            }
                .onSuccess { options -> _uiState.update { it.copy(options = options) } }
                .onFailure { error -> _uiState.update { it.copy(errorMessage = error.toWheelMessage()) } }
        }
    }
}
