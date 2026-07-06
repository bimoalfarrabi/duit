package com.duit.app.data.repository

import com.duit.app.data.local.TokenStorage
import com.duit.app.data.remote.ApiService
import com.duit.app.data.remote.dto.CreateSavingsGoalRequest
import com.duit.app.data.remote.dto.UpdateSavingsGoalRequest
import com.duit.app.domain.model.SavingsGoal
import javax.inject.Inject

class SavingsRepository @Inject constructor(
    private val api: ApiService,
    private val tokenStorage: TokenStorage
) {
    suspend fun getSavingsGoals(): Result<List<SavingsGoal>> = runCatching {
        api.getSavingsGoals().data.map { it.toDomain() }
    }.onFailure { handleUnauth(it) }

    suspend fun createSavingsGoal(name: String, targetAmount: Double, deadline: String?): Result<SavingsGoal> = runCatching {
        api.createSavingsGoal(CreateSavingsGoalRequest(name, targetAmount, deadline)).data.toDomain()
    }.onFailure { handleUnauth(it) }

    suspend fun topup(id: Int, newAmount: Double): Result<SavingsGoal> = runCatching {
        api.updateSavingsGoal(id, UpdateSavingsGoalRequest(current_amount = newAmount)).data.toDomain()
    }.onFailure { handleUnauth(it) }

    suspend fun deleteSavingsGoal(id: Int): Result<Unit> = runCatching {
        api.deleteSavingsGoal(id); Unit
    }.onFailure { handleUnauth(it) }

    private fun com.duit.app.data.remote.dto.SavingsGoalResponse.toDomain() =
        SavingsGoal(id, name, target_amount, current_amount, deadline, is_completed)

    private fun handleUnauth(e: Throwable) {
        if (e.message?.contains("401") == true) tokenStorage.clearToken()
    }
}
