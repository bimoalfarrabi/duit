package com.duit.app.data.remote.dto

data class CategoryResponse(
    val id: Int,
    val name: String,
    val type: String,
    val color: String,
    val icon: String
)

data class CreateCategoryRequest(val name: String, val type: String, val color: String, val icon: String)
