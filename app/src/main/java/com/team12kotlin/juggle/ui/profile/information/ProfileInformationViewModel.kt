package com.team12kotlin.juggle.ui.profile.information

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team12kotlin.juggle.data.Dependencies
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileInformationUiState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ProfileInformationViewModel : ViewModel() {

    private val userRepository = Dependencies.userRepository

    private val _uiState = MutableStateFlow(ProfileInformationUiState())
    val uiState: StateFlow<ProfileInformationUiState> = _uiState.asStateFlow()

    init {
        loadUser()
    }

    fun loadUser() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching { userRepository.getCurrentUser() }
                .onSuccess { user ->
                    _uiState.update {
                        it.copy(
                            firstName = user.firstName,
                            lastName = user.lastName,
                            email = user.email,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = error.message, isLoading = false) }
                }
        }
    }

    // TODO: persist the edited names once the backend exposes a user update endpoint
    fun onFirstNameChange(value: String) {
        _uiState.update { it.copy(firstName = value) }
    }

    fun onLastNameChange(value: String) {
        _uiState.update { it.copy(lastName = value) }
    }
}
