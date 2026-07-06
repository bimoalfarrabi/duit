package com.duit.app.data.remote.dto

data class SavingsGoalResponse(
    val id: Int,
    val name: String,
    val target_amount: Double,
    val current_amount: Double,
    val deadline: String?,
    val is_completed: Boolean
)

data class CreateSavingsGoalRequest(
    val name: String,
    val target_amount: Double,
    val deadline: String? = null
)

data class UpdateSavingsGoalRequest(
    val name: String? = null,
    val current_amount: Double? = null,
    val target_amount: Double? = null,
    val deadline: String? = null
)
