package com.duit.app.data.repository

import com.duit.app.data.local.TokenStorage
import com.duit.app.data.remote.ApiService
import com.duit.app.data.remote.dto.LoginRequest
import com.duit.app.data.remote.dto.RegisterRequest
import com.duit.app.data.remote.dto.TwoFactorChallengeRequest
import com.duit.app.data.remote.toUserMessage
import com.duit.app.domain.model.User
import javax.inject.Inject

// ponytail: LoginResult sealed class — dua shape response dari backend, tidak ada cara lebih simpel
sealed class LoginResult {
    data class Success(val user: User) : LoginResult()
    data class Requires2FA(val tempToken: String) : LoginResult()
}

class AuthRepository @Inject constructor(
    private val api: ApiService,
    private val tokenStorage: TokenStorage
) {
    suspend fun login(email: String, password: String): Result<LoginResult> = runCatching {
        val response = api.login(LoginRequest(email, password))
        if (response.data.requires_2fa == true && response.data.temp_token != null) {
            LoginResult.Requires2FA(response.data.temp_token)
        } else {
            tokenStorage.saveToken(response.data.token!!)
            LoginResult.Success(response.data.user!!.let { User(it.id, it.name, it.email) })
        }
    }

    suspend fun twoFactorChallenge(tempToken: String, code: String): Result<User> = runCatching {
        // ponytail: simpan tempToken sementara untuk request, hapus setelah dapat token penuh
        tokenStorage.saveToken(tempToken)
        val response = api.twoFactorChallenge(TwoFactorChallengeRequest(code))
        tokenStorage.saveToken(response.data.token)
        response.data.user.let { User(it.id, it.name, it.email) }
    }

    suspend fun register(name: String, email: String, password: String): Result<User> = runCatching {
        val response = api.register(RegisterRequest(name, email, password))
        tokenStorage.saveToken(response.data.token!!)
        response.data.user!!.let { User(it.id, it.name, it.email) }
    }

    suspend fun logout(): Result<Unit> = runCatching {
        api.logout()
        tokenStorage.clearToken()
    }

    fun isLoggedIn(): Boolean = tokenStorage.getToken() != null
}
