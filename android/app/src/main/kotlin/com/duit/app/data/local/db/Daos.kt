package com.duit.app.data.local.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAll(): Flow<List<TransactionEntity>>

    @Upsert
    suspend fun upsertAll(items: List<TransactionEntity>)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()
}

@Dao
interface WalletDao {
    @Query("SELECT * FROM wallets")
    fun getAll(): Flow<List<WalletEntity>>

    @Upsert
    suspend fun upsertAll(items: List<WalletEntity>)

    @Query("DELETE FROM wallets")
    suspend fun deleteAll()
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets WHERE month = :month AND year = :year")
    fun getByMonth(month: Int, year: Int): Flow<List<BudgetEntity>>

    @Upsert
    suspend fun upsertAll(items: List<BudgetEntity>)

    @Query("DELETE FROM budgets WHERE month = :month AND year = :year")
    suspend fun deleteByMonth(month: Int, year: Int)
}

@Dao
interface SavingsGoalDao {
    @Query("SELECT * FROM savings_goals")
    fun getAll(): Flow<List<SavingsGoalEntity>>

    @Upsert
    suspend fun upsertAll(items: List<SavingsGoalEntity>)

    @Query("DELETE FROM savings_goals")
    suspend fun deleteAll()
}
