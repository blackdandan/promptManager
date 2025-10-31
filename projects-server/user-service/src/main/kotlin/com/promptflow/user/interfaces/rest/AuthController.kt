package com.promptflow.user.interfaces.rest

import com.promptflow.common.dto.ApiResponse
import com.promptflow.common.dto.ErrorCodes
import com.promptflow.user.application.service.SessionService
import com.promptflow.user.application.service.UserJwtUtils
import com.promptflow.user.application.service.UserService
import com.promptflow.user.domain.model.DeviceInfo
import com.promptflow.user.domain.model.DeviceType
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
class AuthController(
    private val userService: UserService,
    private val sessionService: SessionService,
    private val jwtUtils: UserJwtUtils
) {
    
    private val log = LoggerFactory.getLogger(this::class.java)
    
    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<ApiResponse<AuthResponse>> {
        log.info("用户注册请求: ${request.email}")
        
        try {
            val user = userService.registerUser(
                username = request.username,
                email = request.email,
                password = request.password,
                displayName = request.displayName
            )
            
            val response = AuthResponse(
                userId = user.id!!,
                username = user.username,
                email = user.email,
                displayName = user.displayName,
                userType = user.userType.name,
                roles = user.roles
            )
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "注册成功"))
                
        } catch (e: Exception) {
            log.error("用户注册失败: ${e.message}")
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ErrorCodes.CONFLICT_001, e.message ?: "注册失败"))
        }
    }
    
    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<ApiResponse<LoginResponse>> {
        log.info("用户登录请求: ${request.email}")
        
        try {
            val user = userService.authenticateUser(request.email, request.password)
            
            // 自动创建用户会话
            val session = sessionService.createUserSession(
                userId = user.id!!,
                deviceInfo = extractDeviceInfo(httpRequest),
                ipAddress = getClientIpAddress(httpRequest),
                userAgent = httpRequest.getHeader("User-Agent"),
                tokenExpiryHours = 24
            )
            
            // 生成JWT Token
            val accessToken = jwtUtils.generateToken(user, session.id!!)
            
            val response = LoginResponse(
                accessToken = accessToken,
                refreshToken = session.refreshToken,
                expiresIn = 3600, // 1小时
                user = UserInfo(
                    userId = user.id!!,
                    username = user.username,
                    email = user.email,
                    displayName = user.displayName,
                    userType = user.userType.name,
                    roles = user.roles
                )
            )
            
            return ResponseEntity.ok(ApiResponse.success(response, "登录成功"))
            
        } catch (e: Exception) {
            log.error("用户登录失败: ${e.message}")
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(ErrorCodes.AUTH_001, e.message ?: "登录失败"))
        }
    }
    
    @PostMapping("/refresh")
    fun refreshToken(@RequestBody request: RefreshTokenRequest): ResponseEntity<ApiResponse<LoginResponse>> {
        log.info("Token刷新请求")
        
        try {
            val session = sessionService.refreshToken(
                refreshToken = request.refreshToken,
                tokenExpiryHours = 24
            )
            
            if (session == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(ErrorCodes.AUTH_002, "刷新令牌无效或已过期"))
            }
            
            // 获取用户信息
            val user = userService.getUserById(session.userId)
            
            // 生成新的JWT Token
            val accessToken = jwtUtils.generateToken(user, session.id!!)
            
            val response = LoginResponse(
                accessToken = accessToken,
                refreshToken = session.refreshToken,
                expiresIn = 3600, // 1小时
                user = UserInfo(
                    userId = user.id!!,
                    username = user.username,
                    email = user.email,
                    displayName = user.displayName,
                    userType = user.userType.name,
                    roles = user.roles
                )
            )
            
            return ResponseEntity.ok(ApiResponse.success(response, "Token刷新成功"))
            
        } catch (e: Exception) {
            log.error("Token刷新失败: ${e.message}")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCodes.SYSTEM_001, "Token刷新失败"))
        }
    }
    
    /**
     * 提取设备信息
     */
    private fun extractDeviceInfo(request: HttpServletRequest): DeviceInfo {
        val userAgent = request.getHeader("User-Agent") ?: ""
        val deviceType = when {
            userAgent.contains("Android") -> DeviceType.ANDROID
            userAgent.contains("Tablet") -> DeviceType.IOS
            userAgent.contains("Web") -> DeviceType.WEB
            else -> DeviceType.UNKNOWN
        }
        
        return DeviceInfo(
            deviceType = deviceType,
            deviceId = request.getHeader("X-Device-ID") ?: "unknown"
        )
    }
    
    /**
     * 获取客户端IP地址
     */
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

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val displayName: String? = null
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class RefreshTokenRequest(
    val refreshToken: String
)

data class AuthResponse(
    val userId: String,
    val username: String?,
    val email: String?,
    val displayName: String?,
    val userType: String,
    val roles: List<String>
)

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val user: UserInfo
)

data class UserInfo(
    val userId: String,
    val username: String?,
    val email: String?,
    val displayName: String?,
    val userType: String,
    val roles: List<String>
)
