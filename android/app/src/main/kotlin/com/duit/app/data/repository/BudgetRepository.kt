package com.duit.app.data.repository

import com.duit.app.data.local.TokenStorage
import com.duit.app.data.local.db.BudgetDao
import com.duit.app.data.local.db.BudgetEntity
import com.duit.app.data.remote.ApiService
import com.duit.app.data.remote.dto.CreateBudgetRequest
import com.duit.app.domain.model.Budget
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class BudgetRepository @Inject constructor(
    private val api: ApiService,
    private val tokenStorage: TokenStorage,
    private val budgetDao: BudgetDao
) {
    suspend fun getBudgets(month: Int, year: Int): Result<List<Budget>> = runCatching {
        try {
            val remote = api.getBudgets(mapOf("month" to month.toString(), "year" to year.toString())).data.map {
                Budget(it.id, it.category_id, it.category?.name ?: "", it.month, it.year, it.amount, it.spent)
            }
            budgetDao.deleteByMonth(month, year)
            budgetDao.upsertAll(remote.map { BudgetEntity(it.id, it.categoryId, it.categoryName, it.month, it.year, it.amount, it.spent) })
            remote
        } catch (e: Exception) {
            handleUnauth(e)
            budgetDao.getByMonth(month, year).first().map {
                Budget(it.id, it.categoryId, it.categoryName, it.month, it.year, it.amount, it.spent)
            }.takeIf { it.isNotEmpty() } ?: throw e
        }
    }

    suspend fun saveBudget(categoryId: Int, month: Int, year: Int, amount: Double): Result<Budget> = runCatching {
        api.saveBudget(CreateBudgetRequest(categoryId, month, year, amount)).data.let {
            Budget(it.id, it.category_id, it.category?.name ?: "", it.month, it.year, it.amount, it.spent)
        }.also { b ->
            budgetDao.upsertAll(listOf(BudgetEntity(b.id, b.categoryId, b.categoryName, b.month, b.year, b.amount, b.spent)))
        }
    }.onFailure { handleUnauth(it) }

    suspend fun deleteBudget(id: Int): Result<Unit> = runCatching {
        api.deleteBudget(id); Unit
    }.onFailure { handleUnauth(it) }

    private fun handleUnauth(e: Throwable) {
        if (e.message?.contains("401") == true) tokenStorage.clearToken()
    }
}
