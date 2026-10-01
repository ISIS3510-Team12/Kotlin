package com.team12kotlin.juggle.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team12kotlin.juggle.data.Dependencies
import com.team12kotlin.juggle.data.repository.AuthRepository
import com.team12kotlin.juggle.data.repository.UserRepository
import com.team12kotlin.juggle.ui.dto.UserCreateRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class SignUpUiState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val firstNameError: String? = null,
    val lastNameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val authError: String? = null,
    val isSubmitting: Boolean = false,
    val navigateToSuccess: Boolean = false
) {
    /** True when the form is not currently being submitted. */
    val canSubmit: Boolean
        get() = !isSubmitting
}

class SignUpViewModel(
    private val authRepository: AuthRepository = Dependencies.authRepository,
    private val userRepository: UserRepository = Dependencies.userRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    fun onFirstNameChange(value: String) {
        _uiState.update { it.copy(firstName = value, firstNameError = null, authError = null) }
    }

    fun onLastNameChange(value: String) {
        _uiState.update { it.copy(lastName = value, lastNameError = null, authError = null) }
    }

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, emailError = null, authError = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, passwordError = null, authError = null) }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update {
            it.copy(confirmPassword = value, confirmPasswordError = null, authError = null)
        }
    }

    fun onSignUpSubmit() {
        val state = _uiState.value
        if (state.isSubmitting) return

        val firstNameError = AuthValidation.validateRequired(state.firstName, "First name")
        val lastNameError = AuthValidation.validateRequired(state.lastName, "Last name")
        val emailError = AuthValidation.validateEmail(state.email)
        val passwordError = AuthValidation.validatePassword(state.password)
        val confirmPasswordError = passwordError
            ?: AuthValidation.validatePasswordMatch(state.password, state.confirmPassword)

        val hasError = listOf(
            firstNameError,
            lastNameError,
            emailError,
            passwordError,
            confirmPasswordError
        ).any { it != null }

        if (hasError) {
            _uiState.update {
                it.copy(
                    firstNameError = firstNameError,
                    lastNameError = lastNameError,
                    emailError = emailError,
                    passwordError = passwordError,
                    confirmPasswordError = confirmPasswordError
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                firstNameError = null,
                lastNameError = null,
                emailError = null,
                passwordError = null,
                confirmPasswordError = null,
                authError = null,
                isSubmitting = true
            )
        }

        viewModelScope.launch {
            try {
                authRepository.signUp(state.email, state.password)
                try {
                    userRepository.createUser(
                        UserCreateRequest(firstName = state.firstName, lastName = state.lastName)
                    )
                } catch (error: HttpException) {
                    // The Firebase account may already have a backend profile-
                    if (error.code() != 400) throw error
                }
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
                registerBackendUser(userRepository, user)
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

    /** Clears the one-shot navigation flag after the screen has consumed it. */
    fun onNavigatedToSuccess() {
        _uiState.update { it.copy(navigateToSuccess = false) }
    }
}
