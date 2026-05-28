package com.disheveled.dailyquotes.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.disheveled.dailyquotes.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val login: String = "",
    val password: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
)

class LoginViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun onLoginChange(value: String) {
        _state.update { it.copy(login = value, errorMessage = null) }
    }

    fun onPasswordChange(value: String) {
        _state.update { it.copy(password = value, errorMessage = null) }
    }

    fun submit() {
        val login = _state.value.login.trim()
        val password = _state.value.password
        if (login.isEmpty() || password.isEmpty()) {
            _state.update { it.copy(errorMessage = "Username dan sandi wajib diisi") }
            return
        }
        _state.update { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            val result = authRepository.login(login, password)
            _state.update { current ->
                result.fold(
                    onSuccess = { current.copy(isSubmitting = false) },
                    onFailure = { e ->
                        current.copy(
                            isSubmitting = false,
                            errorMessage = e.message ?: "Tidak dapat masuk",
                        )
                    },
                )
            }
        }
    }
}
