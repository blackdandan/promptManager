package com.promptflow.user.interfaces.rest

import com.promptflow.common.dto.ApiResponse
import com.promptflow.common.dto.ErrorCodes
import com.promptflow.user.application.service.OAuthService
import com.promptflow.user.domain.model.OAuthProvider
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/oauth")
class OAuthController(
    private val oauthService: OAuthService
) {
    
    private val log = LoggerFactory.getLogger(this::class.java)
    
    @GetMapping("/connections")
    fun getUserOAuthConnections(@RequestHeader("X-User-ID") userId: String): ResponseEntity<ApiResponse<List<OAuthConnectionResponse>>> {
        log.info("获取用户三方登录关联: $userId")
        
        try {
            val connections = oauthService.getUserOAuthConnections(userId)
            val response = connections.map { connection ->
                OAuthConnectionResponse(
                    provider = connection.provider.name,
                    providerUserId = connection.providerUserId,
                    createdAt = connection.createdAt
                )
            }
            
            return ResponseEntity.ok(ApiResponse.success(response, "获取三方登录关联成功"))
            
        } catch (e: Exception) {
            log.error("获取三方登录关联失败: ${e.message}")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCodes.SYSTEM_001, "获取三方登录关联失败"))
        }
    }
    
    @DeleteMapping("/connections/{provider}")
    fun disconnectOAuthProvider(
        @RequestHeader("X-User-ID") userId: String,
        @PathVariable provider: String
    ): ResponseEntity<ApiResponse<Void>> {
        log.info("解绑三方登录: $userId -> $provider")
        
        try {
            val oauthProvider = OAuthProvider.valueOf(provider.uppercase())
            oauthService.disconnectOAuthProvider(userId, oauthProvider)
            
            return ResponseEntity.ok(ApiResponse.success(message = "解绑三方登录成功"))
            
        } catch (e: IllegalArgumentException) {
            log.error("无效的三方登录提供商: $provider")
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ErrorCodes.VALIDATION_001, "无效的三方登录提供商"))
                
        } catch (e: Exception) {
            log.error("解绑三方登录失败: ${e.message}")
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ErrorCodes.AUTH_003, e.message ?: "解绑三方登录失败"))
        }
    }
    
    @PostMapping("/callback/{provider}")
    fun handleOAuthCallback(
        @PathVariable provider: String,
        @RequestBody request: OAuthCallbackRequest
    ): ResponseEntity<ApiResponse<OAuthLoginResponse>> {
        log.info("处理三方登录回调: $provider")
        
        try {
            val oauthProvider = OAuthProvider.valueOf(provider.uppercase())
            val user = oauthService.handleOAuthLogin(
                provider = oauthProvider,
                providerUserId = request.providerUserId,
                email = request.email,
                username = request.username,
                avatarUrl = request.avatarUrl,
                profileData = request.profileData,
                accessToken = request.accessToken,
                refreshToken = request.refreshToken,
                expiresAt = request.expiresAt
            )
            
            val response = OAuthLoginResponse(
                userId = user.id!!,
                username = user.username,
                email = user.email,
                displayName = user.displayName,
                userType = user.userType.name,
                roles = user.roles
            )
            
            return ResponseEntity.ok(ApiResponse.success(response, "三方登录成功"))
            
        } catch (e: IllegalArgumentException) {
            log.error("无效的三方登录提供商: $provider")
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ErrorCodes.VALIDATION_001, "无效的三方登录提供商"))
                
        } catch (e: Exception) {
            log.error("三方登录失败: ${e.message}")
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ErrorCodes.AUTH_003, e.message ?: "三方登录失败"))
        }
    }
}

data class OAuthCallbackRequest(
    val providerUserId: String,
    val email: String? = null,
    val username: String? = null,
    val avatarUrl: String? = null,
    val profileData: Map<String, Any>? = null,
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val expiresAt: java.time.LocalDateTime? = null
)

data class OAuthLoginResponse(
    val userId: String,
    val username: String?,
    val email: String?,
    val displayName: String?,
    val userType: String,
    val roles: List<String>
)

data class OAuthConnectionResponse(
    val provider: String,
    val providerUserId: String,
    val createdAt: java.time.LocalDateTime
)
