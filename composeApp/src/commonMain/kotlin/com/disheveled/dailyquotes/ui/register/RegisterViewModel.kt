package com.disheveled.dailyquotes.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.disheveled.dailyquotes.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterUiState(
    val login: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
)

class RegisterViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterUiState())
    val state: StateFlow<RegisterUiState> = _state.asStateFlow()

    fun onLoginChange(value: String) = _state.update { it.copy(login = value, errorMessage = null) }
    fun onEmailChange(value: String) = _state.update { it.copy(email = value, errorMessage = null) }
    fun onPasswordChange(value: String) =
        _state.update { it.copy(password = value, errorMessage = null) }
    fun onConfirmPasswordChange(value: String) =
        _state.update { it.copy(confirmPassword = value, errorMessage = null) }

    fun submit() {
        val s = _state.value
        val login = s.login.trim()
        val email = s.email.trim()
        val password = s.password
        val confirmPassword = s.confirmPassword
        if (login.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            _state.update { it.copy(errorMessage = "Semua kolom wajib diisi") }
            return
        }
        if (password.length < 8) {
            _state.update { it.copy(errorMessage = "Sandi minimal 8 karakter") }
            return
        }
        if (password != confirmPassword) {
            _state.update { it.copy(errorMessage = "Sandi tidak cocok") }
            return
        }
        _state.update { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            val result = authRepository.register(login, email, password)
            _state.update { current ->
                result.fold(
                    onSuccess = { current.copy(isSubmitting = false) },
                    onFailure = { e ->
                        current.copy(
                            isSubmitting = false,
                            errorMessage = e.message ?: "Tidak dapat membuat akun",
                        )
                    },
                )
            }
        }
    }
}
