package com.duit.app

import com.duit.app.data.local.TokenStorage
import com.duit.app.data.remote.ApiService
import com.duit.app.data.remote.dto.*
import com.duit.app.data.repository.TransactionRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class TransactionRepositoryTest {

    private lateinit var api: ApiService
    private lateinit var tokenStorage: TokenStorage
    private lateinit var repository: TransactionRepository

    private val mockCategoryResponse = CategoryResponse(1, "Makan", "expense", "#ef4444", "🍽️")
    private val mockWalletResponse = WalletResponse(1, "Cash", "cash", "#10b981", 500_000.0)
    private val mockTransactionResponse = TransactionResponse(
        id = 1, title = "Makan siang", amount = 25_000.0, type = "expense",
        date = "2026-07-04", note = null,
        category = mockCategoryResponse, wallet = mockWalletResponse
    )

    @Before
    fun setUp() {
        api = mock()
        tokenStorage = mock()
        repository = TransactionRepository(api, tokenStorage)
    }

    @Test
    fun `getTransactions passes params to ApiService`() = runTest {
        val params = mapOf("month" to "7", "year" to "2026")
        whenever(api.getTransactions(params))
            .thenReturn(ApiResponse(listOf(mockTransactionResponse), "OK", true))

        val result = repository.getTransactions(params)

        assertTrue(result.isSuccess)
        verify(api).getTransactions(params)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals("Makan siang", result.getOrNull()?.first()?.title)
    }

    @Test
    fun `createTransaction maps response to domain model`() = runTest {
        val request = CreateTransactionRequest("Makan siang", 25_000.0, "expense", 1, 1, "2026-07-04", null)
        whenever(api.createTransaction(request))
            .thenReturn(ApiResponse(mockTransactionResponse, "OK", true))

        val result = repository.createTransaction("Makan siang", 25_000.0, "expense", 1, 1, "2026-07-04")

        assertTrue(result.isSuccess)
        with(result.getOrNull()!!) {
            assertEquals("Makan siang", title)
            assertEquals(25_000.0, amount, 0.01)
            assertEquals("expense", type)
            assertEquals("Makan", category.name)
            assertEquals("Cash", wallet.name)
        }
    }

    @Test
    fun `deleteTransaction returns success`() = runTest {
        whenever(api.deleteTransaction(1))
            .thenReturn(ApiResponse(Unit, "OK", true))

        val result = repository.deleteTransaction(1)

        assertTrue(result.isSuccess)
        verify(api).deleteTransaction(1)
    }

    @Test
    fun `getSummary returns mapped summary`() = runTest {
        val summaryResponse = SummaryResponse(income = 5_000_000.0, expense = 25_000.0, balance = 4_975_000.0)
        whenever(api.getSummary(any()))
            .thenReturn(ApiResponse(summaryResponse, "OK", true))

        val result = repository.getSummary()

        assertTrue(result.isSuccess)
        assertEquals(5_000_000.0, result.getOrNull()?.income, 0.01)
        assertEquals(25_000.0, result.getOrNull()?.expense, 0.01)
        assertEquals(4_975_000.0, result.getOrNull()?.balance, 0.01)
    }

    @Test
    fun `api failure returns failure result`() = runTest {
        whenever(api.getTransactions(any())).thenThrow(RuntimeException("Network error"))

        val result = repository.getTransactions()

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }
}
