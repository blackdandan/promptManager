package com.promptflow.user.infrastructure.repository

import com.promptflow.user.domain.model.UserSession
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.Optional

@Repository
interface UserSessionRepository : MongoRepository<UserSession, String> {
    
    fun findByToken(token: String): Optional<UserSession>
    
    fun findByRefreshToken(refreshToken: String): Optional<UserSession>
    
    fun findByUserId(userId: String): List<UserSession>
    
    @Query("{ 'expires_at': { \$lt: ?0 } }")
    fun findByExpiresAtBefore(expiresAt: LocalDateTime): List<UserSession>
    
    @Query("{ 'user_id': ?0, 'device_info.device_id': ?1 }")
    fun findByUserIdAndDeviceId(userId: String, deviceId: String): Optional<UserSession>
    
    @Query("{ 'user_id': ?0, 'device_info.device_type': ?1 }")
    fun findByUserIdAndDeviceType(userId: String, deviceType: String): List<UserSession>
    
    @Query("{ 'user_id': ?0 }")
    fun deleteByUserId(userId: String)
    
    @Query("{ 'expires_at': { \$lt: ?0 } }")
    fun deleteByExpiresAtBefore(expiresAt: LocalDateTime)
}
