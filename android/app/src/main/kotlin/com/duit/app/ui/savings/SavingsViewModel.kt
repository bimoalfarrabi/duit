package com.duit.app.ui.savings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duit.app.data.remote.toUserMessage
import com.duit.app.data.repository.SavingsRepository
import com.duit.app.domain.model.SavingsGoal
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SavingsUiState(
    val isLoading: Boolean = false,
    val goals: List<SavingsGoal> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class SavingsViewModel @Inject constructor(private val savingsRepository: SavingsRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(SavingsUiState())
    val uiState: StateFlow<SavingsUiState> = _uiState

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = SavingsUiState(isLoading = true)
            savingsRepository.getSavingsGoals()
                .onSuccess { goals -> _uiState.value = SavingsUiState(goals = goals) }
                .onFailure { _uiState.value = SavingsUiState(error = it.toUserMessage()) }
        }
    }

    fun createGoal(name: String, targetAmount: Double, deadline: String?) {
        viewModelScope.launch {
            savingsRepository.createSavingsGoal(name, targetAmount, deadline)
                .onSuccess { load() }
                .onFailure { _uiState.value = _uiState.value.copy(error = it.toUserMessage()) }
        }
    }

    fun topup(id: Int, newAmount: Double) {
        viewModelScope.launch {
            savingsRepository.topup(id, newAmount)
                .onSuccess { load() }
                .onFailure { _uiState.value = _uiState.value.copy(error = it.toUserMessage()) }
        }
    }

    fun deleteGoal(id: Int) {
        viewModelScope.launch {
            savingsRepository.deleteSavingsGoal(id)
                .onSuccess { load() }
                .onFailure { _uiState.value = _uiState.value.copy(error = it.toUserMessage()) }
        }
    }

    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }
}
