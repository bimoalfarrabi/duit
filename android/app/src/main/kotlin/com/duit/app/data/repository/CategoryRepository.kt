package com.duit.app.data.repository

import com.duit.app.data.local.TokenStorage
import com.duit.app.data.remote.ApiService
import com.duit.app.data.remote.dto.CreateCategoryRequest
import com.duit.app.domain.model.Category
import javax.inject.Inject

class CategoryRepository @Inject constructor(
    private val api: ApiService,
    private val tokenStorage: TokenStorage
) {
    suspend fun getCategories(): Result<List<Category>> = runCatching {
        api.getCategories().data.map { Category(it.id, it.name, it.type, it.color, it.icon) }
    }.onFailure { handleUnauth(it) }

    suspend fun createCategory(name: String, type: String, color: String, icon: String): Result<Category> = runCatching {
        api.createCategory(CreateCategoryRequest(name, type, color, icon)).data
            .let { Category(it.id, it.name, it.type, it.color, it.icon) }
    }.onFailure { handleUnauth(it) }

    suspend fun deleteCategory(id: Int): Result<Unit> = runCatching {
        api.deleteCategory(id)
        Unit
    }.onFailure { handleUnauth(it) }

    private fun handleUnauth(e: Throwable) {
        if (e.message?.contains("401") == true) tokenStorage.clearToken()
    }
}
