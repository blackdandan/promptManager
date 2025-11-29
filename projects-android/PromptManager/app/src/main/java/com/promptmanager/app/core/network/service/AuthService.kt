package com.promptmanager.app.core.network.service

import com.promptmanager.app.core.network.model.ApiResponse
import com.promptmanager.app.core.network.model.AuthResponse
import com.promptmanager.app.core.network.model.LoginRequest
import com.promptmanager.app.core.network.model.RegisterRequest
import com.promptmanager.app.core.network.model.UserDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): ApiResponse<UserDto>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<AuthResponse>

}
