package com.promptflow.user.application.service

import com.promptflow.user.domain.model.OAuthConnection
import com.promptflow.user.domain.model.OAuthProvider
import com.promptflow.user.domain.model.User
import com.promptflow.user.domain.model.UserType
import com.promptflow.user.infrastructure.repository.OAuthConnectionRepository
import com.promptflow.user.infrastructure.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class OAuthService(
    private val userRepository: UserRepository,
    private val oauthConnectionRepository: OAuthConnectionRepository
) {
    
    private val log = LoggerFactory.getLogger(this::class.java)
    
    fun handleOAuthLogin(
        provider: OAuthProvider,
        providerUserId: String,
        email: String?,
        username: String?,
        avatarUrl: String?,
        profileData: Map<String, Any>? = null,
        accessToken: String? = null,
        refreshToken: String? = null,
        expiresAt: LocalDateTime? = null
    ): User {
        log.info("处理三方登录: $provider, 用户ID: $providerUserId")
        
        // 检查是否已有三方登录关联
        val existingConnection = oauthConnectionRepository.findByProviderAndProviderUserId(provider, providerUserId)
        
        return if (existingConnection.isPresent) {
            // 已有关联，返回关联的用户
            val connection = existingConnection.get()
            val user = userRepository.findById(connection.userId)
                .orElseThrow { OAuthUserNotFoundException("三方登录用户不存在: ${connection.userId}") }
            
            log.info("三方登录成功 (已有用户): ${user.username} via $provider")
            user
        } else {
            // 新三方登录，创建用户和关联
            createOAuthUser(
                provider = provider,
                providerUserId = providerUserId,
                email = email,
                username = username,
                avatarUrl = avatarUrl,
                profileData = profileData,
                accessToken = accessToken,
                refreshToken = refreshToken,
                expiresAt = expiresAt
            )
        }
    }
    
    private fun createOAuthUser(
        provider: OAuthProvider,
        providerUserId: String,
        email: String?,
        username: String?,
        avatarUrl: String?,
        profileData: Map<String, Any>?,
        accessToken: String?,
        refreshToken: String?,
        expiresAt: LocalDateTime?
    ): User {
        // 检查邮箱是否已被其他用户使用
        val existingUser = email?.let { userRepository.findByEmail(it) }
        
        val user = if (existingUser?.isPresent == true) {
            // 邮箱已存在，关联到现有用户
            val existing = existingUser.get()
            log.info("三方登录关联到现有用户: ${existing.username} via $provider")
            existing
        } else {
            // 创建新用户
            val newUser = User(
                email = email,
                username = generateUniqueUsername(username ?: "user_${provider.name.lowercase()}_$providerUserId"),
                displayName = username,
                avatarUrl = avatarUrl,
                userType = UserType.OAUTH,
                emailVerified = email != null
            )
            
            userRepository.save(newUser).also {
                log.info("创建三方登录用户: ${it.username} via $provider")
            }
        }
        
        // 创建三方登录关联
        val connection = OAuthConnection(
            userId = user.id!!,
            provider = provider,
            providerUserId = providerUserId,
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresAt = expiresAt,
            profileData = profileData
        )
        
        oauthConnectionRepository.save(connection)
        log.info("创建三方登录关联: ${user.username} -> $provider")
        
        return user
    }
    
    fun getUserOAuthConnections(userId: String): List<OAuthConnection> {
        return oauthConnectionRepository.findByUserId(userId)
    }
    
    fun disconnectOAuthProvider(userId: String, provider: OAuthProvider) {
        val connections = oauthConnectionRepository.findByUserId(userId)
        val providerConnection = connections.find { it.provider == provider }
        
        if (providerConnection == null) {
            throw OAuthConnectionNotFoundException("未找到该三方登录关联")
        }
        
        // 检查用户是否还有其他登录方式
        val remainingConnections = connections.filter { it.provider != provider }
        val user = userRepository.findById(userId)
            .orElseThrow { UserNotFoundException("用户不存在: $userId") }
        
        if (remainingConnections.isEmpty() && user.passwordHash == null) {
            throw OAuthDisconnectNotAllowedException("无法解绑唯一的三方登录方式")
        }
        
        oauthConnectionRepository.deleteByUserIdAndProvider(userId, provider)
        log.info("解绑三方登录: $userId -> $provider")
    }
    
    fun updateOAuthToken(
        userId: String,
        provider: OAuthProvider,
        accessToken: String?,
        refreshToken: String?,
        expiresAt: LocalDateTime?
    ) {
        val connection = oauthConnectionRepository.findByUserIdAndProvider(userId, provider)
            .orElseThrow { OAuthConnectionNotFoundException("未找到三方登录关联") }
        
        val updatedConnection = connection.copy(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresAt = expiresAt,
            updatedAt = LocalDateTime.now()
        )
        
        oauthConnectionRepository.save(updatedConnection)
        log.info("更新三方登录令牌: $userId -> $provider")
    }
    
    private fun generateUniqueUsername(baseUsername: String): String {
        var username = baseUsername
        var counter = 1
        
        while (userRepository.existsByUsername(username)) {
            username = "${baseUsername}_${counter}"
            counter++
        }
        
        return username
    }
}

class OAuthUserNotFoundException(message: String) : RuntimeException(message)
class OAuthConnectionNotFoundException(message: String) : RuntimeException(message)
class OAuthDisconnectNotAllowedException(message: String) : RuntimeException(message)
