package com.duit.app.data.remote.dto

data class LoginRequest(val email: String, val password: String)
data class RegisterRequest(val name: String, val email: String, val password: String)

data class UserResponse(val id: Int, val name: String, val email: String)

// ponytail: nullable fields karena backend return dua shape berbeda — token+user atau requires_2fa+temp_token
data class LoginResponse(
    val token: String? = null,
    val user: UserResponse? = null,
    val requires_2fa: Boolean? = null,
    val temp_token: String? = null
)

data class TwoFactorChallengeRequest(val code: String)
data class TwoFactorChallengeResponse(val token: String, val user: UserResponse)
