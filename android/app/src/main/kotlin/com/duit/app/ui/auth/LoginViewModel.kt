package com.duit.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duit.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun login(email: String, password: String) {
        if (email.isBlank() || password.length < 6) {
            _uiState.value = LoginUiState(error = "Email dan password wajib diisi (min. 6 karakter)")
            return
        }
        viewModelScope.launch {
            _uiState.value = LoginUiState(isLoading = true)
            authRepository.login(email, password)
                .onSuccess { _uiState.value = LoginUiState(isSuccess = true) }
                .onFailure { _uiState.value = LoginUiState(error = it.message ?: "Login gagal") }
        }
    }

    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }
}
