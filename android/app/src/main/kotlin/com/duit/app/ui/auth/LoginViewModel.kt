package com.duit.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duit.app.data.remote.toUserMessage
import com.duit.app.data.repository.AuthRepository
import com.duit.app.data.repository.LoginResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val requires2fa: Boolean = false,
    val tempToken: String? = null
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
                .onSuccess { result ->
                    when (result) {
                        is LoginResult.Success -> _uiState.value = LoginUiState(isSuccess = true)
                        is LoginResult.Requires2FA -> _uiState.value = LoginUiState(
                            requires2fa = true,
                            tempToken = result.tempToken
                        )
                    }
                }
                .onFailure { _uiState.value = LoginUiState(error = it.toUserMessage()) }
        }
    }

    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }
}
