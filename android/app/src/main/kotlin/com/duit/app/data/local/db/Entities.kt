package com.duit.app.data.local.db

import androidx.room.*

// ponytail: flatten nested Category+Wallet into primitives — no TypeConverters needed
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val amount: Double,
    val type: String,
    val date: String,
    val note: String?,
    val categoryId: Int,
    val categoryName: String,
    val categoryType: String,
    val categoryColor: String,
    val categoryIcon: String,
    val walletId: Int,
    val walletName: String,
    val walletType: String,
    val walletColor: String,
    val walletBalance: Double
)

@Entity(tableName = "wallets")
data class WalletEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val type: String,
    val color: String,
    val balance: Double
)

@Entity(tableName = "budgets", primaryKeys = ["id"])
data class BudgetEntity(
    val id: Int,
    val categoryId: Int,
    val categoryName: String,
    val month: Int,
    val year: Int,
    val amount: Double,
    val spent: Double
)

@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val deadline: String?,
    val isCompleted: Boolean
)
