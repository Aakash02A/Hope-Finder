package com.hope_finder.ui.auth

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hope_finder.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _state = mutableStateOf(AuthState())
    val state: State<AuthState> = _state

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun onEvent(event: AuthUiEvent) {
        when (event) {
            is AuthUiEvent.EmailChanged -> {
                _state.value = _state.value.copy(email = event.email)
            }
            is AuthUiEvent.PasswordChanged -> {
                _state.value = _state.value.copy(password = event.password)
            }
            is AuthUiEvent.ConfirmPasswordChanged -> {
                _state.value = _state.value.copy(confirmPassword = event.confirmPassword)
            }
            is AuthUiEvent.Login -> login()
            is AuthUiEvent.Register -> register()
            is AuthUiEvent.ForgotPassword -> forgotPassword()
            is AuthUiEvent.TogglePasswordVisibility -> {
                _state.value = _state.value.copy(isPasswordVisible = !_state.value.isPasswordVisible)
            }
        }
    }

    private fun login() {
        if (!validateEmail() || !validatePassword()) return
        
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val result = repository.login(_state.value.email, _state.value.password)
            _state.value = _state.value.copy(isLoading = false)
            
            result.onSuccess {
                _eventFlow.emit(UiEvent.NavigateToHome)
            }.onFailure {
                _eventFlow.emit(UiEvent.ShowSnackbar(it.message ?: "Login failed"))
            }
        }
    }

    private fun register() {
        if (!validateEmail() || !validatePassword() || !validateConfirmPassword()) return
        
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val result = repository.register(_state.value.email, _state.value.password)
            _state.value = _state.value.copy(isLoading = false)
            
            result.onSuccess {
                _eventFlow.emit(UiEvent.NavigateToHome)
            }.onFailure {
                _eventFlow.emit(UiEvent.ShowSnackbar(it.message ?: "Registration failed"))
            }
        }
    }

    private fun forgotPassword() {
        if (!validateEmail()) return
        
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val result = repository.forgotPassword(_state.value.email)
            _state.value = _state.value.copy(isLoading = false)
            
            result.onSuccess {
                _eventFlow.emit(UiEvent.ShowSnackbar("Password reset email sent"))
            }.onFailure {
                _eventFlow.emit(UiEvent.ShowSnackbar(it.message ?: "Failed to send reset email"))
            }
        }
    }

    private fun validateEmail(): Boolean {
        return if (_state.value.email.isBlank()) {
            _state.value = _state.value.copy(emailError = "Email cannot be empty")
            false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(_state.value.email).matches()) {
            _state.value = _state.value.copy(emailError = "Invalid email format")
            false
        } else {
            _state.value = _state.value.copy(emailError = null)
            true
        }
    }

    private fun validatePassword(): Boolean {
        return if (_state.value.password.length < 6) {
            _state.value = _state.value.copy(passwordError = "Password must be at least 6 characters")
            false
        } else {
            _state.value = _state.value.copy(passwordError = null)
            true
        }
    }

    private fun validateConfirmPassword(): Boolean {
        return if (_state.value.password != _state.value.confirmPassword) {
            _state.value = _state.value.copy(confirmPasswordError = "Passwords do not match")
            false
        } else {
            _state.value = _state.value.copy(confirmPasswordError = null)
            true
        }
    }

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
        object NavigateToHome : UiEvent()
    }
}

data class AuthState(
    val email: String = "",
    val emailError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val confirmPassword: String = "",
    val confirmPasswordError: String? = null,
    val isLoading: Boolean = false,
    val isPasswordVisible: Boolean = false
)

sealed class AuthUiEvent {
    data class EmailChanged(val email: String) : AuthUiEvent()
    data class PasswordChanged(val password: String) : AuthUiEvent()
    data class ConfirmPasswordChanged(val confirmPassword: String) : AuthUiEvent()
    object Login : AuthUiEvent()
    object Register : AuthUiEvent()
    object ForgotPassword : AuthUiEvent()
    object TogglePasswordVisibility : AuthUiEvent()
}
