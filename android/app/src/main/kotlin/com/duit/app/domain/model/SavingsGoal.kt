package com.duit.app.domain.model

data class SavingsGoal(
    val id: Int,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val deadline: String?,
    val isCompleted: Boolean
) {
    val progress: Float get() = if (targetAmount <= 0) 0f else (currentAmount / targetAmount).toFloat().coerceIn(0f, 1f)
}
