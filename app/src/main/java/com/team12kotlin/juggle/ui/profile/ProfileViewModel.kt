package com.team12kotlin.juggle.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team12kotlin.juggle.data.Dependencies
import com.team12kotlin.juggle.ui.utils.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val displayName: String = "",
    val subtitle: String = "",
    val initial: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ProfileViewModel : ViewModel() {

    private val userRepository = Dependencies.userRepository

    private val _uiState = MutableStateFlow(ProfileUiState(isLoading = true))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

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
                            displayName = user.displayName,
                            subtitle = user.major,
                            initial = user.firstName.take(1).uppercase(),
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { error ->
                    val cached = Dependencies.authRepository.currentUser
                    _uiState.update {
                        it.copy(
                            displayName = it.displayName.ifBlank { cached?.displayName.orEmpty() },
                            initial = it.initial.ifBlank {
                                cached?.displayName?.trim()?.take(1)?.uppercase().orEmpty()
                            },
                            errorMessage = error.toUserMessage(),
                            isLoading = false
                        )
                    }
                }
        }
    }
}
