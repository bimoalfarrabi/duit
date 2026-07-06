package com.duit.app.data.remote.dto

data class BudgetResponse(
    val id: Int,
    val category_id: Int,
    val category: CategoryResponse?,
    val month: Int,
    val year: Int,
    val amount: Double,
    val spent: Double
)

data class CreateBudgetRequest(
    val category_id: Int,
    val month: Int,
    val year: Int,
    val amount: Double
)
