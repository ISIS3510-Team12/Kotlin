package com.team12kotlin.juggle.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
    val isSubmitting: Boolean = false,
    val navigateToSuccess: Boolean = false
) {
    /** True when the form is not currently being submitted. */
    val canSubmit: Boolean
        get() = !isSubmitting
}

class SignUpViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    fun onFirstNameChange(value: String) {
        _uiState.update { it.copy(firstName = value, firstNameError = null) }
    }

    fun onLastNameChange(value: String) {
        _uiState.update { it.copy(lastName = value, lastNameError = null) }
    }

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, emailError = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, passwordError = null) }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update { it.copy(confirmPassword = value, confirmPasswordError = null) }
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
                isSubmitting = true
            )
        }

        viewModelScope.launch {
            // TODO: replace with AuthRepository.signUp(...) when there's logic xd
            delay(600)
            _uiState.update { it.copy(isSubmitting = false, navigateToSuccess = true) }
        }
    }

    /** Clears the one-shot navigation flag after the screen has consumed it. */
    fun onNavigatedToSuccess() {
        _uiState.update { it.copy(navigateToSuccess = false) }
    }
}
