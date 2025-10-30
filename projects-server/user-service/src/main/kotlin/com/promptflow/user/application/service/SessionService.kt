package com.promptflow.user.application.service

import com.promptflow.user.domain.model.DeviceInfo
import com.promptflow.user.domain.model.UserSession
import com.promptflow.user.infrastructure.repository.UserSessionRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional
class SessionService(
    private val userSessionRepository: UserSessionRepository
) {
    
    private val log = LoggerFactory.getLogger(this::class.java)
    
    fun createUserSession(
        userId: String,
        deviceInfo: DeviceInfo,
        ipAddress: String? = null,
        userAgent: String? = null,
        tokenExpiryHours: Long = 24
    ): UserSession {
        log.info("创建用户会话: $userId, 设备: ${deviceInfo.deviceType}")
        
        val token = generateSecureToken()
        val refreshToken = generateSecureToken()
        val expiresAt = LocalDateTime.now().plusHours(tokenExpiryHours)
        
        val session = UserSession(
            userId = userId,
            token = token,
            refreshToken = refreshToken,
            expiresAt = expiresAt,
            deviceInfo = deviceInfo,
            ipAddress = ipAddress,
            userAgent = userAgent
        )
        
        return userSessionRepository.save(session).also {
            log.info("用户会话创建成功: $userId, 会话ID: ${it.id}")
        }
    }
    
    fun validateToken(token: String): UserSession? {
        val session = userSessionRepository.findByToken(token)
            .orElse(null) ?: return null
        
        // 检查会话是否过期
        if (session.expiresAt.isBefore(LocalDateTime.now())) {
            log.info("会话已过期: ${session.id}")
            userSessionRepository.deleteById(session.id!!)
            return null
        }
        
        return session
    }
    
    fun refreshToken(refreshToken: String, tokenExpiryHours: Long = 24): UserSession? {
        val session = userSessionRepository.findByRefreshToken(refreshToken)
            .orElse(null) ?: return null
        
        // 检查会话是否过期
        if (session.expiresAt.isBefore(LocalDateTime.now())) {
            log.info("刷新令牌已过期: ${session.id}")
            userSessionRepository.deleteById(session.id!!)
            return null
        }
        
        // 生成新的token和refreshToken
        val newToken = generateSecureToken()
        val newRefreshToken = generateSecureToken()
        val newExpiresAt = LocalDateTime.now().plusHours(tokenExpiryHours)
        
        val updatedSession = session.copy(
            token = newToken,
            refreshToken = newRefreshToken,
            expiresAt = newExpiresAt
        )
        
        return userSessionRepository.save(updatedSession).also {
            log.info("令牌刷新成功: ${session.userId}, 新会话ID: ${it.id}")
        }
    }
    
    fun getUserSessions(userId: String): List<UserSession> {
        return userSessionRepository.findByUserId(userId)
    }
    
    fun getActiveUserSessions(userId: String): List<UserSession> {
        val now = LocalDateTime.now()
        return userSessionRepository.findByUserId(userId)
            .filter { it.expiresAt.isAfter(now) }
    }
    
    fun invalidateSession(sessionId: String) {
        userSessionRepository.deleteById(sessionId)
        log.info("会话已失效: $sessionId")
    }
    
    fun invalidateUserSessions(userId: String) {
        val sessions = userSessionRepository.findByUserId(userId)
        userSessionRepository.deleteAll(sessions)
        log.info("用户所有会话已失效: $userId, 共${sessions.size}个会话")
    }
    
    fun invalidateOtherSessions(userId: String, currentSessionId: String) {
        val sessions = userSessionRepository.findByUserId(userId)
            .filter { it.id != currentSessionId }
        
        userSessionRepository.deleteAll(sessions)
        log.info("用户其他会话已失效: $userId, 共${sessions.size}个会话")
    }
    
    fun cleanupExpiredSessions() {
        val now = LocalDateTime.now()
        val expiredSessions = userSessionRepository.findByExpiresAtBefore(now)
        
        if (expiredSessions.isNotEmpty()) {
            userSessionRepository.deleteAll(expiredSessions)
            log.info("清理过期会话: 共${expiredSessions.size}个")
        }
    }
    
    fun getSessionStats(userId: String): Map<String, Any> {
        val allSessions = userSessionRepository.findByUserId(userId)
        val now = LocalDateTime.now()
        val activeSessions = allSessions.filter { it.expiresAt.isAfter(now) }
        val lastActivity = activeSessions.maxByOrNull { it.createdAt }?.createdAt ?: LocalDateTime.MIN
        return mapOf(
            "totalSessions" to allSessions.size,
            "activeSessions" to activeSessions.size,
            "deviceTypes" to activeSessions.groupBy { it.deviceInfo.deviceType }.mapValues { it.value.size },
            "lastActivity" to lastActivity
        )
    }
    
    private fun generateSecureToken(): String {
        return UUID.randomUUID().toString().replace("-", "") + 
               UUID.randomUUID().toString().replace("-", "")
    }
}
