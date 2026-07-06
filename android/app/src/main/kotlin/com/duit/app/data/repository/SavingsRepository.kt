package com.duit.app.data.repository

import com.duit.app.data.local.TokenStorage
import com.duit.app.data.local.db.SavingsGoalDao
import com.duit.app.data.local.db.SavingsGoalEntity
import com.duit.app.data.remote.ApiService
import com.duit.app.data.remote.dto.CreateSavingsGoalRequest
import com.duit.app.data.remote.dto.UpdateSavingsGoalRequest
import com.duit.app.domain.model.SavingsGoal
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SavingsRepository @Inject constructor(
    private val api: ApiService,
    private val tokenStorage: TokenStorage,
    private val savingsGoalDao: SavingsGoalDao
) {
    suspend fun getSavingsGoals(): Result<List<SavingsGoal>> = runCatching {
        try {
            val remote = api.getSavingsGoals().data.map { it.toDomain() }
            savingsGoalDao.upsertAll(remote.map { it.toEntity() })
            remote
        } catch (e: Exception) {
            handleUnauth(e)
            savingsGoalDao.getAll().first().map { it.toDomain() }.takeIf { it.isNotEmpty() } ?: throw e
        }
    }

    suspend fun createSavingsGoal(name: String, targetAmount: Double, deadline: String?): Result<SavingsGoal> = runCatching {
        api.createSavingsGoal(CreateSavingsGoalRequest(name, targetAmount, deadline)).data.toDomain()
            .also { savingsGoalDao.upsertAll(listOf(it.toEntity())) }
    }.onFailure { handleUnauth(it) }

    suspend fun topup(id: Int, newAmount: Double): Result<SavingsGoal> = runCatching {
        api.updateSavingsGoal(id, UpdateSavingsGoalRequest(current_amount = newAmount)).data.toDomain()
            .also { savingsGoalDao.upsertAll(listOf(it.toEntity())) }
    }.onFailure { handleUnauth(it) }

    suspend fun deleteSavingsGoal(id: Int): Result<Unit> = runCatching {
        api.deleteSavingsGoal(id); Unit
    }.onFailure { handleUnauth(it) }

    private fun com.duit.app.data.remote.dto.SavingsGoalResponse.toDomain() =
        SavingsGoal(id, name, target_amount, current_amount, deadline, is_completed)

    private fun SavingsGoal.toEntity() =
        SavingsGoalEntity(id, name, targetAmount, currentAmount, deadline, isCompleted)

    private fun SavingsGoalEntity.toDomain() =
        SavingsGoal(id, name, targetAmount, currentAmount, deadline, isCompleted)

    private fun handleUnauth(e: Throwable) {
        if (e.message?.contains("401") == true) tokenStorage.clearToken()
    }
}
