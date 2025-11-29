package com.promptmanager.app.feature.auth.data

import com.promptmanager.app.core.database.dao.UserDao
import com.promptmanager.app.core.database.entity.UserEntity
import com.promptmanager.app.core.network.TokenManager
import com.promptmanager.app.core.network.model.LoginRequest
import com.promptmanager.app.core.network.model.RegisterRequest
import com.promptmanager.app.core.network.model.UserDto
import com.promptmanager.app.core.network.service.AuthService
import com.promptmanager.app.feature.auth.domain.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authService: AuthService,
    private val userDao: UserDao,
    private val tokenManager: TokenManager
) : AuthRepository {

    override val currentUser: Flow<UserEntity?> = userDao.getCurrentUser()

    override suspend fun login(request: LoginRequest): Result<Unit> {
        return try {
            val response = authService.login(request)
            if (response.success && response.data != null) {
                val authData = response.data
                tokenManager.saveAccessToken(authData.accessToken)
                tokenManager.saveRefreshToken(authData.refreshToken)
                
                val userEntity = authData.user.toEntity()
                userDao.insertUser(userEntity)
                Result.success(Unit)
            } else {
                val errorMsg = response.message ?: response.error?.message ?: "Unknown error"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(request: RegisterRequest): Result<Unit> {
        return try {
            val response = authService.register(request)
            if (response.success && response.data != null) {
                // Registration successful. 
                Result.success(Unit)
            } else {
                val errorMsg = response.message ?: response.error?.message ?: "Unknown error"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        tokenManager.clearTokens()
        userDao.clearUsers()
    }

    private fun UserDto.toEntity(): UserEntity {
        return UserEntity(
            userId = userId,
            username = username,
            email = email,
            displayName = displayName,
            userType = userType,
            roles = roles
        )
    }
}
