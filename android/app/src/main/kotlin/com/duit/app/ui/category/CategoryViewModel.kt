package com.duit.app.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duit.app.data.repository.CategoryRepository
import com.duit.app.domain.model.Category
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryUiState(
    val isLoading: Boolean = false,
    val categories: List<Category> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class CategoryViewModel @Inject constructor(private val categoryRepository: CategoryRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState: StateFlow<CategoryUiState> = _uiState

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = CategoryUiState(isLoading = true)
            categoryRepository.getCategories()
                .onSuccess { _uiState.value = CategoryUiState(categories = it) }
                .onFailure { _uiState.value = CategoryUiState(error = it.message) }
        }
    }

    fun createCategory(name: String, type: String, color: String, icon: String) {
        viewModelScope.launch {
            categoryRepository.createCategory(name, type, color, icon)
                .onSuccess { load() }
                .onFailure { _uiState.value = _uiState.value.copy(error = it.message) }
        }
    }

    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }
}
