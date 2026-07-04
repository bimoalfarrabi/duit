package com.duit.app.data.remote.dto

data class WalletResponse(
    val id: Int,
    val name: String,
    val type: String,
    val color: String,
    val balance: Double
)

data class CreateWalletRequest(val name: String, val type: String, val color: String)
