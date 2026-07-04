package com.duit.app.domain.model

data class Transaction(
    val id: Int,
    val title: String,
    val amount: Double,
    val type: String,
    val date: String,
    val note: String?,
    val category: Category,
    val wallet: Wallet
)
