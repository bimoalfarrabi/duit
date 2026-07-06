package com.duit.app.ui.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duit.app.data.remote.toUserMessage
import com.duit.app.data.repository.BudgetRepository
import com.duit.app.data.repository.CategoryRepository
import com.duit.app.domain.model.Budget
import com.duit.app.domain.model.Category
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class BudgetUiState(
    val isLoading: Boolean = false,
    val budgets: List<Budget> = emptyList(),
    val categories: List<Category> = emptyList(),
    val error: String? = null,
    val month: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
    val year: Int = Calendar.getInstance().get(Calendar.YEAR)
)

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val month = _uiState.value.month
            val year = _uiState.value.year
            budgetRepository.getBudgets(month, year)
                .onSuccess { budgets ->
                    _uiState.value = _uiState.value.copy(isLoading = false, budgets = budgets)
                }
                .onFailure { _uiState.value = _uiState.value.copy(isLoading = false, error = it.toUserMessage()) }
            categoryRepository.getCategories()
                .onSuccess { cats -> _uiState.value = _uiState.value.copy(categories = cats) }
                .onFailure { /* non-critical */ }
        }
    }

    fun saveBudget(categoryId: Int, amount: Double) {
        viewModelScope.launch {
            budgetRepository.saveBudget(categoryId, _uiState.value.month, _uiState.value.year, amount)
                .onSuccess { load() }
                .onFailure { _uiState.value = _uiState.value.copy(error = it.toUserMessage()) }
        }
    }

    fun deleteBudget(id: Int) {
        viewModelScope.launch {
            budgetRepository.deleteBudget(id)
                .onSuccess { load() }
                .onFailure { _uiState.value = _uiState.value.copy(error = it.toUserMessage()) }
        }
    }

    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }
}
