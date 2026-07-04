package com.duit.app

import com.duit.app.data.local.TokenStorage
import com.duit.app.data.repository.AuthRepository
import com.duit.app.ui.auth.LoginViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        authRepository = mock()
        viewModel = LoginViewModel(authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `login with blank email shows error`() = runTest {
        viewModel.login("", "password123")
        val state = viewModel.uiState.value
        assertFalse(state.isSuccess)
        assertTrue(state.error != null)
    }

    @Test
    fun `login with short password shows error`() = runTest {
        viewModel.login("user@test.com", "123")
        val state = viewModel.uiState.value
        assertFalse(state.isSuccess)
        assertTrue(state.error != null)
    }

    @Test
    fun `login success sets isSuccess true`() = runTest {
        val mockUser = com.duit.app.domain.model.User(1, "Test", "test@test.com")
        whenever(authRepository.login("user@test.com", "password123"))
            .thenReturn(Result.success(mockUser))

        viewModel.login("user@test.com", "password123")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSuccess)
    }

    @Test
    fun `login failure sets error message`() = runTest {
        whenever(authRepository.login("user@test.com", "password123"))
            .thenReturn(Result.failure(Exception("Invalid credentials")))

        viewModel.login("user@test.com", "password123")
        advanceUntilIdle()

        assertEquals("Invalid credentials", viewModel.uiState.value.error)
    }

    @Test
    fun `clearError sets error to null`() = runTest {
        viewModel.login("", "")
        viewModel.clearError()
        assertEquals(null, viewModel.uiState.value.error)
    }
}
