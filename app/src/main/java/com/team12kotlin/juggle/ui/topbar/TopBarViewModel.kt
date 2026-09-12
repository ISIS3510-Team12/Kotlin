package com.team12kotlin.juggle.ui.topbar

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class TopBarUiState(
    val userInitial: String = "A",
    val showGroupIcon: Boolean = true
)

class TopBarViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TopBarUiState())
    val uiState: StateFlow<TopBarUiState> = _uiState.asStateFlow()

    fun setUserInitial(initial: String) {
        _uiState.update { it.copy(userInitial = initial) }
    }

    fun setShowGroupIcon(visible: Boolean) {
        _uiState.update { it.copy(showGroupIcon = visible) }
    }

    fun onMenuClick() {
        // TODO: open the groups overview
    }

    fun onProfileClick() {
        // TODO: navigate to the profile
    }
}
