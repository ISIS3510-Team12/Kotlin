package com.team12kotlin.juggle.ui.profile.information

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team12kotlin.juggle.data.Dependencies
import com.team12kotlin.juggle.ui.utils.toUserMessage
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
    val errorMessage: String? = null,
    val loaded: Boolean = false
)

class ProfileInformationViewModel : ViewModel() {

    private val userRepository = Dependencies.userRepository

    private val _uiState = MutableStateFlow(ProfileInformationUiState(isLoading = true))
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
                            errorMessage = null,
                            loaded = true
                        )
                    }
                }
                .onFailure { error ->
                    val cached = Dependencies.authRepository.currentUser
                    val name = cached?.displayName.orEmpty().trim()
                    _uiState.update {
                        it.copy(
                            firstName = it.firstName.ifBlank { name.substringBefore(' ') },
                            lastName = it.lastName.ifBlank { name.substringAfter(' ', "") },
                            email = it.email.ifBlank { cached?.email.orEmpty() },
                            errorMessage = error.toUserMessage(),
                            isLoading = false
                        )
                    }
                }
        }
    }
}
