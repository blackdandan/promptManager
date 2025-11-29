package com.promptmanager.app.core.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FolderDto(
    @Json(name = "id") val id: String,
    @Json(name = "userId") val userId: String,
    @Json(name = "name") val name: String,
    @Json(name = "parentId") val parentId: String? = null,
    @Json(name = "order") val order: Int = 0,
    @Json(name = "color") val color: String? = null,
    @Json(name = "icon") val icon: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "promptCount") val promptCount: Int = 0,
    @Json(name = "createdAt") val createdAt: String? = null,
    @Json(name = "updatedAt") val updatedAt: String? = null
)
