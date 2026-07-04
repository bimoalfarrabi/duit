package com.duit.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duit.app.data.remote.toUserMessage
import com.duit.app.data.repository.TransactionRepository
import com.duit.app.domain.model.Summary
import com.duit.app.domain.model.Transaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = false,
    val summary: Summary? = null,
    val recentTransactions: List<Transaction> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(private val transactionRepository: TransactionRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = HomeUiState(isLoading = true)
            val now = LocalDate.now()
            val params = mapOf("month" to now.monthValue.toString(), "year" to now.year.toString())

            val summaryResult = transactionRepository.getSummary(params)
            val txResult = transactionRepository.getTransactions(params)

            _uiState.value = HomeUiState(
                summary = summaryResult.getOrNull(),
                recentTransactions = txResult.getOrNull()?.take(5) ?: emptyList(),
                error = summaryResult.exceptionOrNull()?.toUserMessage() ?: txResult.exceptionOrNull()?.toUserMessage()
            )
        }
    }
}
