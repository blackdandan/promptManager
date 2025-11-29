package com.promptmanager.app.core.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiResponse<T>(
    @Json(name = "success") val success: Boolean,
    @Json(name = "data") val data: T? = null,
    @Json(name = "message") val message: String? = null,
    @Json(name = "error") val error: ApiError? = null
)

@JsonClass(generateAdapter = true)
data class ApiError(
    @Json(name = "code") val code: String,
    @Json(name = "message") val message: String,
    @Json(name = "details") val details: String? = null
)
