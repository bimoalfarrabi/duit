package com.duit.app.data.remote.dto

data class ApiResponse<T>(
    val data: T,
    val message: String,
    val status: Boolean
)
