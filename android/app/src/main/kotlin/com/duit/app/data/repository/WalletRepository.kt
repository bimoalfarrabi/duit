package com.duit.app.data.repository

import com.duit.app.data.local.TokenStorage
import com.duit.app.data.local.db.WalletDao
import com.duit.app.data.local.db.WalletEntity
import com.duit.app.data.remote.ApiService
import com.duit.app.data.remote.dto.CreateWalletRequest
import com.duit.app.data.remote.toUserMessage
import com.duit.app.domain.model.Wallet
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class WalletRepository @Inject constructor(
    private val api: ApiService,
    private val tokenStorage: TokenStorage,
    private val walletDao: WalletDao
) {
    suspend fun getWallets(): Result<List<Wallet>> = runCatching {
        try {
            val remote = api.getWallets().data.map { Wallet(it.id, it.name, it.type, it.color, it.balance) }
            walletDao.upsertAll(remote.map { WalletEntity(it.id, it.name, it.type, it.color, it.balance) })
            remote
        } catch (e: Exception) {
            handleUnauth(e)
            walletDao.getAll().first().map { Wallet(it.id, it.name, it.type, it.color, it.balance) }.takeIf { it.isNotEmpty() }
                ?: throw e
        }
    }

    suspend fun createWallet(name: String, type: String, color: String): Result<Wallet> = runCatching {
        api.createWallet(CreateWalletRequest(name, type, color)).data
            .let { Wallet(it.id, it.name, it.type, it.color, it.balance) }
            .also { walletDao.upsertAll(listOf(WalletEntity(it.id, it.name, it.type, it.color, it.balance))) }
    }.onFailure { handleUnauth(it) }

    suspend fun deleteWallet(id: Int): Result<Unit> = runCatching {
        api.deleteWallet(id); Unit
    }.onFailure { handleUnauth(it) }

    private fun handleUnauth(e: Throwable) {
        if (e.message?.contains("401") == true) tokenStorage.clearToken()
    }
}
