package com.duit.app.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duit.app.data.repository.TransactionRepository
import com.duit.app.data.repository.WalletRepository
import com.duit.app.domain.model.Transaction
import com.duit.app.domain.model.Wallet
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class TransactionListUiState(
    val isLoading: Boolean = false,
    val transactions: List<Transaction> = emptyList(),
    val wallets: List<Wallet> = emptyList(),
    val selectedMonth: Int = LocalDate.now().monthValue,
    val selectedYear: Int = LocalDate.now().year,
    val selectedWalletId: Int? = null,
    val error: String? = null
)

@HiltViewModel
class TransactionListViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionListUiState())
    val uiState: StateFlow<TransactionListUiState> = _uiState

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val state = _uiState.value
            val params = buildMap {
                put("month", state.selectedMonth.toString())
                put("year", state.selectedYear.toString())
                state.selectedWalletId?.let { put("wallet_id", it.toString()) }
            }
            val wallets = walletRepository.getWallets().getOrDefault(emptyList())
            transactionRepository.getTransactions(params)
                .onSuccess { _uiState.value = _uiState.value.copy(transactions = it, wallets = wallets, isLoading = false) }
                .onFailure { _uiState.value = _uiState.value.copy(error = it.message, isLoading = false) }
        }
    }

    fun setMonth(month: Int, year: Int) {
        _uiState.value = _uiState.value.copy(selectedMonth = month, selectedYear = year)
        load()
    }

    fun setWallet(walletId: Int?) {
        _uiState.value = _uiState.value.copy(selectedWalletId = walletId)
        load()
    }

    fun deleteTransaction(id: Int) {
        viewModelScope.launch {
            transactionRepository.deleteTransaction(id)
                .onSuccess { load() }
                .onFailure { _uiState.value = _uiState.value.copy(error = it.message) }
        }
    }
}
