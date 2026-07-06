package com.duit.app.domain.model

data class Budget(
    val id: Int,
    val categoryId: Int,
    val categoryName: String,
    val month: Int,
    val year: Int,
    val amount: Double,
    val spent: Double
) {
    val remaining: Double get() = amount - spent
    val progress: Float get() = if (amount <= 0) 0f else (spent / amount).toFloat().coerceIn(0f, 1f)
}
