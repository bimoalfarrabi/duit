package com.duit.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duit.app.data.remote.toUserMessage
import com.duit.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TotpUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class TotpViewModel @Inject constructor(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(TotpUiState())
    val uiState: StateFlow<TotpUiState> = _uiState

    fun challenge(tempToken: String, code: String) {
        if (code.length != 6) {
            _uiState.value = TotpUiState(error = "Kode harus 6 digit")
            return
        }
        viewModelScope.launch {
            _uiState.value = TotpUiState(isLoading = true)
            authRepository.twoFactorChallenge(tempToken, code)
                .onSuccess { _uiState.value = TotpUiState(isSuccess = true) }
                .onFailure { _uiState.value = TotpUiState(error = it.toUserMessage()) }
        }
    }

    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }
}
