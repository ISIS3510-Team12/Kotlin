package com.team12kotlin.juggle.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team12kotlin.juggle.data.Dependencies
import com.team12kotlin.juggle.data.repository.AuthRepository
import com.team12kotlin.juggle.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SignInUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val authError: String? = null,
    val isSubmitting: Boolean = false,
    val navigateToSuccess: Boolean = false
) {
    /** True when the form is not currently being submitted. */
    val canSubmit: Boolean
        get() = !isSubmitting
}

class SignInViewModel(
    private val authRepository: AuthRepository = Dependencies.authRepository,
    private val userRepository: UserRepository = Dependencies.userRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignInUiState())
    val uiState: StateFlow<SignInUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, emailError = null, authError = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null, authError = null) }
    }

    fun onSignInSubmit() {
        val state = _uiState.value
        if (state.isSubmitting) return

        val emailError = AuthValidation.validateEmail(state.email)
        val passwordError = AuthValidation.validateRequired(state.password, "Password")

        if (emailError != null || passwordError != null) {
            _uiState.update {
                it.copy(emailError = emailError, passwordError = passwordError)
            }
            return
        }

        _uiState.update {
            it.copy(
                emailError = null,
                passwordError = null,
                authError = null,
                isSubmitting = true
            )
        }

        viewModelScope.launch {
            try {
                authRepository.signIn(state.email, state.password)
                _uiState.update { it.copy(isSubmitting = false, navigateToSuccess = true) }
            } catch (error: Throwable) {
                _uiState.update {
                    it.copy(isSubmitting = false, authError = error.toAuthErrorMessage())
                }
            }
        }
    }

    fun onGoogleSignIn(idToken: String) {
        if (_uiState.value.isSubmitting) return

        _uiState.update { it.copy(authError = null, isSubmitting = true) }

        viewModelScope.launch {
            try {
                val user = authRepository.signInWithGoogle(idToken)
                registerBackendUser(authRepository, userRepository, user)
                _uiState.update { it.copy(isSubmitting = false, navigateToSuccess = true) }
            } catch (error: Throwable) {
                _uiState.update {
                    it.copy(isSubmitting = false, authError = error.toAuthErrorMessage())
                }
            }
        }
    }

    fun onGoogleSignInError(error: Throwable) {
        _uiState.update {
            it.copy(isSubmitting = false, authError = error.toAuthErrorMessage())
        }
    }

    fun onForgotPassword() {
        // TODO: navigate to password recovery when that screen exists.
    }

    /** Clears the one-shot navigation flag after the screen has consumed it. */
    fun onNavigatedToSuccess() {
        _uiState.update { it.copy(navigateToSuccess = false) }
    }
}
