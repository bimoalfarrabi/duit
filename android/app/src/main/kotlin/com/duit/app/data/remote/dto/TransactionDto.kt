package com.duit.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class TransactionResponse(
    val id: Int,
    val title: String,
    val amount: Double,
    val type: String,
    val date: String,
    val note: String?,
    val category: CategoryResponse,
    val wallet: WalletResponse
)

data class CreateTransactionRequest(
    val title: String,
    val amount: Double,
    val type: String,
    @SerializedName("category_id") val categoryId: Int,
    @SerializedName("wallet_id") val walletId: Int,
    val date: String,
    val note: String? = null
)
