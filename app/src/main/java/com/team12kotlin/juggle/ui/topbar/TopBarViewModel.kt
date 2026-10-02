package com.team12kotlin.juggle.ui.topbar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team12kotlin.juggle.data.Dependencies
import com.team12kotlin.juggle.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TopBarUiState(
    val userInitial: String = "",
    val showGroupIcon: Boolean = true
)

class TopBarViewModel(
    private val userRepository: UserRepository = Dependencies.userRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TopBarUiState())
    val uiState: StateFlow<TopBarUiState> = _uiState.asStateFlow()

    init {
        fetchUserInitial()
    }

    private fun fetchUserInitial() {
        viewModelScope.launch {
            try {
                val user = userRepository.getCurrentUser()
                val initial = user.firstName.firstOrNull()?.uppercaseChar()?.toString()
                    ?: user.displayName.firstOrNull()?.uppercaseChar()?.toString()
                    ?: "?"
                _uiState.update { it.copy(userInitial = initial) }
            } catch (e: Exception) {
                // Mantener el valor vacío si falla
            }
        }
    }

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
