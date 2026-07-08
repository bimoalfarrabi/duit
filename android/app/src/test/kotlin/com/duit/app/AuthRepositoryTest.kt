package com.duit.app

import com.duit.app.data.local.TokenStorage
import com.duit.app.data.remote.ApiService
import com.duit.app.data.remote.dto.LoginRequest
import com.duit.app.data.remote.dto.LoginResponse
import com.duit.app.data.remote.dto.ApiResponse
import com.duit.app.data.remote.dto.UserResponse
import com.duit.app.data.repository.AuthRepository
import com.duit.app.data.repository.LoginResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class AuthRepositoryTest {

    private lateinit var api: ApiService
    private lateinit var tokenStorage: TokenStorage
    private lateinit var repository: AuthRepository

    @Before
    fun setUp() {
        api = mock()
        tokenStorage = mock()
        repository = AuthRepository(api, tokenStorage)
    }

    @Test
    fun `login success saves token and returns user`() = runTest {
        val userResponse = UserResponse(1, "Test User", "test@test.com")
        val loginResponse = LoginResponse(token = "abc123", user = userResponse)
        val apiResponse = ApiResponse(data = loginResponse, message = "OK", status = true)

        whenever(api.login(LoginRequest("test@test.com", "password123")))
            .thenReturn(apiResponse)

        val result = repository.login("test@test.com", "password123")

        assertTrue(result.isSuccess)
        assertEquals("Test User", (result.getOrNull() as? LoginResult.Success)?.user?.name)
        verify(tokenStorage).saveToken("abc123")
    }

    @Test
    fun `login failure returns failure result`() = runTest {
        whenever(api.login(LoginRequest("bad@test.com", "wrong")))
            .thenThrow(RuntimeException("401 Unauthorized"))

        val result = repository.login("bad@test.com", "wrong")

        assertTrue(result.isFailure)
    }

    @Test
    fun `logout clears token`() = runTest {
        val apiResponse = ApiResponse(data = Unit, message = "OK", status = true)
        whenever(api.logout()).thenReturn(apiResponse)

        repository.logout()

        verify(tokenStorage).clearToken()
    }

    @Test
    fun `isLoggedIn returns true when token exists`() {
        whenever(tokenStorage.getToken()).thenReturn("some-token")
        assertTrue(repository.isLoggedIn())
    }

    @Test
    fun `isLoggedIn returns false when no token`() {
        whenever(tokenStorage.getToken()).thenReturn(null)
        assertTrue(!repository.isLoggedIn())
    }
}
