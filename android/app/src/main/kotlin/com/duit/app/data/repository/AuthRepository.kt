package com.duit.app.data.repository

import com.duit.app.data.local.TokenStorage
import com.duit.app.data.remote.ApiService
import com.duit.app.data.remote.dto.LoginRequest
import com.duit.app.data.remote.dto.RegisterRequest
import com.duit.app.domain.model.User
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val api: ApiService,
    private val tokenStorage: TokenStorage
) {
    suspend fun login(email: String, password: String): Result<User> = runCatching {
        val response = api.login(LoginRequest(email, password))
        tokenStorage.saveToken(response.data.token)
        response.data.user.let { User(it.id, it.name, it.email) }
    }

    suspend fun register(name: String, email: String, password: String): Result<User> = runCatching {
        val response = api.register(RegisterRequest(name, email, password))
        tokenStorage.saveToken(response.data.token)
        response.data.user.let { User(it.id, it.name, it.email) }
    }

    suspend fun logout(): Result<Unit> = runCatching {
        api.logout()
        tokenStorage.clearToken()
    }

    fun isLoggedIn(): Boolean = tokenStorage.getToken() != null
}
