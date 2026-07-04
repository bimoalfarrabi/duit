package com.duit.app.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duit.app.data.remote.toUserMessage
import com.duit.app.data.repository.CategoryRepository
import com.duit.app.data.repository.TransactionRepository
import com.duit.app.data.repository.WalletRepository
import com.duit.app.domain.model.Category
import com.duit.app.domain.model.Wallet
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddTransactionUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val wallets: List<Wallet> = emptyList(),
    val categories: List<Category> = emptyList()
)

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState

    init {
        viewModelScope.launch {
            val wallets = walletRepository.getWallets().getOrDefault(emptyList())
            val categories = categoryRepository.getCategories().getOrDefault(emptyList())
            _uiState.value = AddTransactionUiState(wallets = wallets, categories = categories)
        }
    }

    fun submit(
        title: String, amount: String, type: String,
        categoryId: Int, walletId: Int, date: String, note: String
    ) {
        val parsedAmount = amount.toDoubleOrNull()
        if (title.isBlank() || parsedAmount == null || date.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Semua field wajib diisi")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            transactionRepository.createTransaction(
                title, parsedAmount, type, categoryId, walletId, date,
                note.ifBlank { null }
            )
                .onSuccess { _uiState.value = _uiState.value.copy(isSuccess = true, isLoading = false) }
                .onFailure { _uiState.value = _uiState.value.copy(error = it.toUserMessage(), isLoading = false) }
        }
    }

    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }
}
