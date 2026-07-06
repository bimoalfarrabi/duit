package com.duit.app.data.remote

import com.duit.app.data.remote.dto.*
import okhttp3.ResponseBody
import retrofit2.http.*

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): ApiResponse<LoginResponse>

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): ApiResponse<LoginResponse>

    @GET("auth/me")
    suspend fun me(): ApiResponse<UserResponse>

    @POST("auth/logout")
    suspend fun logout(): ApiResponse<Unit>

    @POST("auth/two-factor-challenge")
    suspend fun twoFactorChallenge(@Body body: TwoFactorChallengeRequest): ApiResponse<TwoFactorChallengeResponse>

    @GET("wallets")
    suspend fun getWallets(): ApiResponse<List<WalletResponse>>

    @POST("wallets")
    suspend fun createWallet(@Body body: CreateWalletRequest): ApiResponse<WalletResponse>

    @DELETE("wallets/{id}")
    suspend fun deleteWallet(@Path("id") id: Int): ApiResponse<Unit>

    @GET("categories")
    suspend fun getCategories(): ApiResponse<List<CategoryResponse>>

    @POST("categories")
    suspend fun createCategory(@Body body: CreateCategoryRequest): ApiResponse<CategoryResponse>

    @DELETE("categories/{id}")
    suspend fun deleteCategory(@Path("id") id: Int): ApiResponse<Unit>

    @GET("transactions")
    suspend fun getTransactions(@QueryMap params: Map<String, String>): ApiResponse<List<TransactionResponse>>

    @POST("transactions")
    suspend fun createTransaction(@Body body: CreateTransactionRequest): ApiResponse<TransactionResponse>

    @PUT("transactions/{id}")
    suspend fun updateTransaction(@Path("id") id: Int, @Body body: CreateTransactionRequest): ApiResponse<TransactionResponse>

    @DELETE("transactions/{id}")
    suspend fun deleteTransaction(@Path("id") id: Int): ApiResponse<Unit>

    @GET("statistics/summary")
    suspend fun getSummary(@QueryMap params: Map<String, String>): ApiResponse<SummaryResponse>

    @GET("transactions/export")
    suspend fun exportCsv(@QueryMap params: Map<String, String>): ResponseBody

    // Budget
    @GET("budgets")
    suspend fun getBudgets(@QueryMap params: Map<String, String>): ApiResponse<List<BudgetResponse>>

    @POST("budgets")
    suspend fun saveBudget(@Body body: CreateBudgetRequest): ApiResponse<BudgetResponse>

    @DELETE("budgets/{id}")
    suspend fun deleteBudget(@Path("id") id: Int): ApiResponse<Unit>

    // Savings Goals
    @GET("savings")
    suspend fun getSavingsGoals(): ApiResponse<List<SavingsGoalResponse>>

    @POST("savings")
    suspend fun createSavingsGoal(@Body body: CreateSavingsGoalRequest): ApiResponse<SavingsGoalResponse>

    @PUT("savings/{id}")
    suspend fun updateSavingsGoal(@Path("id") id: Int, @Body body: UpdateSavingsGoalRequest): ApiResponse<SavingsGoalResponse>

    @DELETE("savings/{id}")
    suspend fun deleteSavingsGoal(@Path("id") id: Int): ApiResponse<Unit>
}
