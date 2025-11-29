package com.promptmanager.app.feature.auth.domain

import com.promptmanager.app.core.database.entity.UserEntity
import com.promptmanager.app.core.network.model.LoginRequest
import com.promptmanager.app.core.network.model.RegisterRequest
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<UserEntity?>

    suspend fun login(request: LoginRequest): Result<Unit>
    suspend fun register(request: RegisterRequest): Result<Unit>
    suspend fun logout()
}
