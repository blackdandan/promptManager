package com.promptmanager.app.core.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginRequest(
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String
)

@JsonClass(generateAdapter = true)
data class RegisterRequest(
    @Json(name = "username") val username: String,
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String,
    @Json(name = "displayName") val displayName: String
)

@JsonClass(generateAdapter = true)
data class AuthResponse(
    @Json(name = "accessToken") val accessToken: String,
    @Json(name = "refreshToken") val refreshToken: String,
    @Json(name = "expiresIn") val expiresIn: Long,
    @Json(name = "user") val user: UserDto
)

@JsonClass(generateAdapter = true)
data class UserDto(
    @Json(name = "userId") val userId: String,
    @Json(name = "username") val username: String,
    @Json(name = "email") val email: String,
    @Json(name = "displayName") val displayName: String,
    @Json(name = "userType") val userType: String,
    @Json(name = "roles") val roles: List<String>
)
