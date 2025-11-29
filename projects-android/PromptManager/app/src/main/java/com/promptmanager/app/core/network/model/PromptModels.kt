package com.promptmanager.app.core.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PromptDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "userId") val userId: String? = null,
    @Json(name = "title") val title: String,
    @Json(name = "content") val content: String,
    @Json(name = "description") val description: String? = null,
    @Json(name = "tags") val tags: List<String> = emptyList(),
    @Json(name = "category") val category: String? = null,
    @Json(name = "isPublic") val isPublic: Boolean = false,
    @Json(name = "isFavorite") val isFavorite: Boolean = false,
    @Json(name = "usageCount") val usageCount: Int = 0,
    @Json(name = "folderId") val folderId: String? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "lastUsedAt") val lastUsedAt: String? = null,
    @Json(name = "createdAt") val createdAt: String? = null,
    @Json(name = "updatedAt") val updatedAt: String? = null
)

@JsonClass(generateAdapter = true)
data class Page<T>(
    @Json(name = "content") val content: List<T>,
    @Json(name = "totalElements") val totalElements: Long,
    @Json(name = "totalPages") val totalPages: Int,
    @Json(name = "last") val last: Boolean,
    @Json(name = "first") val first: Boolean
)
