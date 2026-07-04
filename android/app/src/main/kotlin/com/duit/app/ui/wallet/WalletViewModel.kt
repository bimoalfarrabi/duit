package com.duit.app.ui.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duit.app.data.repository.WalletRepository
import com.duit.app.domain.model.Wallet
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WalletUiState(
    val isLoading: Boolean = false,
    val wallets: List<Wallet> = emptyList(),
    val error: String? = null,
    val hasCashWallet: Boolean = false
)

@HiltViewModel
class WalletViewModel @Inject constructor(private val walletRepository: WalletRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = WalletUiState(isLoading = true)
            walletRepository.getWallets()
                .onSuccess { wallets ->
                    _uiState.value = WalletUiState(
                        wallets = wallets,
                        hasCashWallet = wallets.any { it.type == "cash" }
                    )
                }
                .onFailure { _uiState.value = WalletUiState(error = it.message) }
        }
    }

    fun createWallet(name: String, type: String, color: String) {
        viewModelScope.launch {
            walletRepository.createWallet(name, type, color)
                .onSuccess { load() }
                .onFailure { _uiState.value = _uiState.value.copy(error = it.message) }
        }
    }

    fun deleteWallet(id: Int) {
        viewModelScope.launch {
            walletRepository.deleteWallet(id)
                .onSuccess { load() }
                .onFailure { _uiState.value = _uiState.value.copy(error = it.message) }
        }
    }

    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }
}
