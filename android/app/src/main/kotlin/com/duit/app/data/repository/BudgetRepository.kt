package com.duit.app.data.repository

import com.duit.app.data.local.TokenStorage
import com.duit.app.data.remote.ApiService
import com.duit.app.data.remote.dto.CreateBudgetRequest
import com.duit.app.domain.model.Budget
import javax.inject.Inject

class BudgetRepository @Inject constructor(
    private val api: ApiService,
    private val tokenStorage: TokenStorage
) {
    suspend fun getBudgets(month: Int, year: Int): Result<List<Budget>> = runCatching {
        api.getBudgets(mapOf("month" to month.toString(), "year" to year.toString())).data.map {
            Budget(it.id, it.category_id, it.category?.name ?: "", it.month, it.year, it.amount, it.spent)
        }
    }.onFailure { handleUnauth(it) }

    suspend fun saveBudget(categoryId: Int, month: Int, year: Int, amount: Double): Result<Budget> = runCatching {
        api.saveBudget(CreateBudgetRequest(categoryId, month, year, amount)).data.let {
            Budget(it.id, it.category_id, it.category?.name ?: "", it.month, it.year, it.amount, it.spent)
        }
    }.onFailure { handleUnauth(it) }

    suspend fun deleteBudget(id: Int): Result<Unit> = runCatching {
        api.deleteBudget(id); Unit
    }.onFailure { handleUnauth(it) }

    private fun handleUnauth(e: Throwable) {
        if (e.message?.contains("401") == true) tokenStorage.clearToken()
    }
}
