package com.duit.app.data.remote.dto

data class LoginRequest(val email: String, val password: String)
data class RegisterRequest(val name: String, val email: String, val password: String)

data class UserResponse(val id: Int, val name: String, val email: String)
data class LoginResponse(val token: String, val user: UserResponse)
