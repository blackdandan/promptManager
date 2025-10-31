package com.promptflow.user.interfaces.rest

import com.promptflow.common.dto.ApiResponse
import com.promptflow.common.dto.ErrorCodes
import com.promptflow.user.application.service.SessionService
import com.promptflow.user.domain.model.DeviceInfo
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/sessions")
class SessionController(
    private val sessionService: SessionService
) {
    
    private val log = LoggerFactory.getLogger(this::class.java)
    
    
    @PostMapping("/refresh")
    fun refreshToken(@RequestBody request: RefreshTokenRequest): ResponseEntity<ApiResponse<SessionResponse>> {
        log.info("刷新Token请求")
        
        try {
            val session = sessionService.refreshToken(
                refreshToken = request.refreshToken,
                tokenExpiryHours = 24
            )
            
            if (session == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(ErrorCodes.AUTH_002, "刷新令牌无效或已过期"))
            }
            
            val response = SessionResponse(
                sessionId = session.id!!,
                accessToken = session.token,
                refreshToken = session.refreshToken,
                expiresAt = session.expiresAt,
                deviceInfo = session.deviceInfo
            )
            
            return ResponseEntity.ok(ApiResponse.success(response, "Token刷新成功"))
            
        } catch (e: Exception) {
            log.error("刷新Token失败: ${e.message}")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCodes.SYSTEM_001, "刷新Token失败"))
        }
    }
    
    @GetMapping
    fun getUserSessions(@RequestHeader("X-User-ID") userId: String): ResponseEntity<ApiResponse<List<SessionResponse>>> {
        log.info("获取用户会话列表: $userId")
        
        try {
            val sessions = sessionService.getUserSessions(userId)
            val response = sessions.map { session ->
                SessionResponse(
                    sessionId = session.id!!,
                    accessToken = session.token,
                    refreshToken = session.refreshToken,
                    expiresAt = session.expiresAt,
                    deviceInfo = session.deviceInfo,
                    ipAddress = session.ipAddress,
                    userAgent = session.userAgent,
                    createdAt = session.createdAt
                )
            }
            
            return ResponseEntity.ok(ApiResponse.success(response, "获取会话列表成功"))
            
        } catch (e: Exception) {
            log.error("获取会话列表失败: ${e.message}")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCodes.SYSTEM_001, "获取会话列表失败"))
        }
    }
    
    @GetMapping("/active")
    fun getActiveSessions(@RequestHeader("X-User-ID") userId: String): ResponseEntity<ApiResponse<List<SessionResponse>>> {
        log.info("获取用户活跃会话: $userId")
        
        try {
            val sessions = sessionService.getActiveUserSessions(userId)
            val response = sessions.map { session ->
                SessionResponse(
                    sessionId = session.id!!,
                    accessToken = session.token,
                    refreshToken = session.refreshToken,
                    expiresAt = session.expiresAt,
                    deviceInfo = session.deviceInfo,
                    ipAddress = session.ipAddress,
                    userAgent = session.userAgent,
                    createdAt = session.createdAt
                )
            }
            
            return ResponseEntity.ok(ApiResponse.success(response, "获取活跃会话成功"))
            
        } catch (e: Exception) {
            log.error("获取活跃会话失败: ${e.message}")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCodes.SYSTEM_001, "获取活跃会话失败"))
        }
    }
    
    @DeleteMapping("/{sessionId}")
    fun invalidateSession(
        @RequestHeader("X-User-ID") userId: String,
        @PathVariable sessionId: String
    ): ResponseEntity<ApiResponse<Void>> {
        log.info("失效会话: $userId -> $sessionId")
        
        try {
            sessionService.invalidateSession(sessionId)
            return ResponseEntity.ok(ApiResponse.success(message = "会话失效成功"))
            
        } catch (e: Exception) {
            log.error("失效会话失败: ${e.message}")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCodes.SYSTEM_001, "失效会话失败"))
        }
    }
    
    @DeleteMapping
    fun invalidateAllSessions(@RequestHeader("X-User-ID") userId: String): ResponseEntity<ApiResponse<Void>> {
        log.info("失效用户所有会话: $userId")
        
        try {
            sessionService.invalidateUserSessions(userId)
            return ResponseEntity.ok(ApiResponse.success(message = "所有会话失效成功"))
            
        } catch (e: Exception) {
            log.error("失效所有会话失败: ${e.message}")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCodes.SYSTEM_001, "失效所有会话失败"))
        }
    }
    
    @DeleteMapping("/others")
    fun invalidateOtherSessions(
        @RequestHeader("X-User-ID") userId: String,
        @RequestHeader("X-Session-ID") currentSessionId: String
    ): ResponseEntity<ApiResponse<Void>> {
        log.info("失效用户其他会话: $userId, 当前会话: $currentSessionId")
        
        try {
            sessionService.invalidateOtherSessions(userId, currentSessionId)
            return ResponseEntity.ok(ApiResponse.success(message = "其他会话失效成功"))
            
        } catch (e: Exception) {
            log.error("失效其他会话失败: ${e.message}")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCodes.SYSTEM_001, "失效其他会话失败"))
        }
    }
    
    @GetMapping("/stats")
    fun getSessionStats(@RequestHeader("X-User-ID") userId: String): ResponseEntity<ApiResponse<Map<String, Any>>> {
        log.info("获取会话统计: $userId")
        
        try {
            val stats = sessionService.getSessionStats(userId)
            return ResponseEntity.ok(ApiResponse.success(stats, "获取会话统计成功"))
            
        } catch (e: Exception) {
            log.error("获取会话统计失败: ${e.message}")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCodes.SYSTEM_001, "获取会话统计失败"))
        }
    }
    
    private fun getClientIpAddress(request: HttpServletRequest): String? {
        var ipAddress = request.getHeader("X-Forwarded-For")
        if (ipAddress.isNullOrEmpty() || "unknown".equals(ipAddress, ignoreCase = true)) {
            ipAddress = request.getHeader("X-Real-IP")
        }
        if (ipAddress.isNullOrEmpty() || "unknown".equals(ipAddress, ignoreCase = true)) {
            ipAddress = request.remoteAddr
        }
        return ipAddress
    }
}

data class CreateSessionRequest(
    val userId: String,
    val deviceInfo: DeviceInfo,
    val tokenExpiryHours: Long? = 24
)


data class SessionResponse(
    val sessionId: String,
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: java.time.LocalDateTime,
    val deviceInfo: DeviceInfo,
    val ipAddress: String? = null,
    val userAgent: String? = null,
    val createdAt: java.time.LocalDateTime? = null
)
