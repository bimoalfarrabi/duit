package com.duit.app

import com.duit.app.data.repository.TransactionRepository
import com.duit.app.domain.model.Category
import com.duit.app.domain.model.Summary
import com.duit.app.domain.model.Transaction
import com.duit.app.domain.model.Wallet
import com.duit.app.ui.home.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var viewModel: HomeViewModel

    private val mockCategory = Category(1, "Makan", "expense", "#ef4444", "🍽️")
    private val mockWallet = Wallet(1, "Cash", "cash", "#10b981", 500_000.0)
    private val mockTransactions = listOf(
        Transaction(1, "Makan siang", 25_000.0, "expense", "2026-07-04", null, mockCategory, mockWallet),
        Transaction(2, "Gaji", 5_000_000.0, "income", "2026-07-01", null, mockCategory, mockWallet)
    )
    private val mockSummary = Summary(income = 5_000_000.0, expense = 25_000.0, balance = 4_975_000.0)

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        transactionRepository = mock()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads summary and recent transactions`() = runTest {
        whenever(transactionRepository.getSummary(any())).thenReturn(Result.success(mockSummary))
        whenever(transactionRepository.getTransactions(any())).thenReturn(Result.success(mockTransactions))

        viewModel = HomeViewModel(transactionRepository)
        advanceUntilIdle()

        assertEquals(mockSummary, viewModel.uiState.value.summary)
        assertEquals(2, viewModel.uiState.value.recentTransactions.size)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `recent transactions capped at 5`() = runTest {
        val manyTx = (1..10).map {
            Transaction(it, "Tx $it", 10_000.0, "expense", "2026-07-04", null, mockCategory, mockWallet)
        }
        whenever(transactionRepository.getSummary(any())).thenReturn(Result.success(mockSummary))
        whenever(transactionRepository.getTransactions(any())).thenReturn(Result.success(manyTx))

        viewModel = HomeViewModel(transactionRepository)
        advanceUntilIdle()

        assertEquals(5, viewModel.uiState.value.recentTransactions.size)
    }

    @Test
    fun `network error sets error message`() = runTest {
        whenever(transactionRepository.getSummary(any()))
            .thenReturn(Result.failure(Exception("No internet")))
        whenever(transactionRepository.getTransactions(any()))
            .thenReturn(Result.failure(Exception("No internet")))

        viewModel = HomeViewModel(transactionRepository)
        advanceUntilIdle()

        assertEquals("No internet", viewModel.uiState.value.error)
    }
}
