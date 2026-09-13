package com.team12kotlin.juggle.ui.navbar

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class NavbarUiState(
    val selectedDestination: NavigationDestination = NavigationDestination.Tasks,
    val destinations: List<NavigationDestination> = NavigationDestination.entries
)

class NavbarViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(NavbarUiState())
    val uiState: StateFlow<NavbarUiState> = _uiState.asStateFlow()

    fun onDestinationSelected(destination: NavigationDestination) {
        _uiState.update { it.copy(selectedDestination = destination) }
    }
}
