package com.duit.app.data.repository

import com.duit.app.data.local.TokenStorage
import com.duit.app.data.local.db.TransactionDao
import com.duit.app.data.local.db.TransactionEntity
import com.duit.app.data.remote.ApiService
import com.duit.app.data.remote.dto.CreateTransactionRequest
import com.duit.app.domain.model.Category
import com.duit.app.domain.model.Summary
import com.duit.app.domain.model.Transaction
import com.duit.app.data.remote.toUserMessage
import com.duit.app.domain.model.Wallet
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class TransactionRepository @Inject constructor(
    private val api: ApiService,
    private val tokenStorage: TokenStorage,
    private val transactionDao: TransactionDao
) {
    suspend fun getTransactions(params: Map<String, String> = emptyMap()): Result<List<Transaction>> = runCatching {
        // ponytail: network-first, fall back to cache on failure
        try {
            val remote = api.getTransactions(params).data.map { it.toDomain() }
            transactionDao.upsertAll(remote.map { it.toEntity() })
            remote
        } catch (e: Exception) {
            handleUnauth(e)
            transactionDao.getAll().first().map { it.toDomain() }.takeIf { it.isNotEmpty() }
                ?: throw e
        }
    }

    suspend fun createTransaction(
        title: String, amount: Double, type: String,
        categoryId: Int, walletId: Int, date: String, note: String? = null
    ): Result<Transaction> = runCatching {
        api.createTransaction(CreateTransactionRequest(title, amount, type, categoryId, walletId, date, note))
            .data.toDomain().also { transactionDao.upsertAll(listOf(it.toEntity())) }
    }.onFailure { handleUnauth(it) }

    suspend fun updateTransaction(
        id: Int, title: String, amount: Double, type: String,
        categoryId: Int, walletId: Int, date: String, note: String? = null
    ): Result<Transaction> = runCatching {
        api.updateTransaction(id, CreateTransactionRequest(title, amount, type, categoryId, walletId, date, note))
            .data.toDomain().also { transactionDao.upsertAll(listOf(it.toEntity())) }
    }.onFailure { handleUnauth(it) }

    suspend fun deleteTransaction(id: Int): Result<Unit> = runCatching {
        api.deleteTransaction(id); Unit
    }.onFailure { handleUnauth(it) }

    suspend fun getSummary(params: Map<String, String> = emptyMap()): Result<Summary> = runCatching {
        api.getSummary(params).data.let { Summary(it.income, it.expense, it.balance) }
    }.onFailure { handleUnauth(it) }

    private fun handleUnauth(e: Throwable) {
        if (e.message?.contains("401") == true) tokenStorage.clearToken()
    }

    private fun com.duit.app.data.remote.dto.TransactionResponse.toDomain() = Transaction(
        id = id, title = title, amount = amount, type = type, date = date, note = note,
        category = Category(category.id, category.name, category.type, category.color, category.icon),
        wallet = Wallet(wallet.id, wallet.name, wallet.type, wallet.color, wallet.balance)
    )

    private fun Transaction.toEntity() = TransactionEntity(
        id = id, title = title, amount = amount, type = type, date = date, note = note,
        categoryId = category.id, categoryName = category.name, categoryType = category.type,
        categoryColor = category.color, categoryIcon = category.icon,
        walletId = wallet.id, walletName = wallet.name, walletType = wallet.type,
        walletColor = wallet.color, walletBalance = wallet.balance
    )

    private fun TransactionEntity.toDomain() = Transaction(
        id = id, title = title, amount = amount, type = type, date = date, note = note,
        category = Category(categoryId, categoryName, categoryType, categoryColor, categoryIcon),
        wallet = Wallet(walletId, walletName, walletType, walletColor, walletBalance)
    )
}
