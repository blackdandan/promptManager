package com.promptflow.user.interfaces.rest

import com.promptflow.common.dto.ApiResponse
import com.promptflow.common.dto.ErrorCodes
import com.promptflow.user.application.service.UserService
import org.slf4j.LoggerFactory
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
class AuthController(
    private val userService: UserService
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
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<ApiResponse<AuthResponse>> {
        log.info("用户登录请求: ${request.email}")
        
        try {
            val user = userService.authenticateUser(request.email, request.password)
            
            val response = AuthResponse(
                userId = user.id!!,
                username = user.username,
                email = user.email,
                displayName = user.displayName,
                userType = user.userType.name,
                roles = user.roles
            )
            
            return ResponseEntity.ok(ApiResponse.success(response, "登录成功"))
            
        } catch (e: Exception) {
            log.error("用户登录失败: ${e.message}")
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(ErrorCodes.AUTH_001, e.message ?: "登录失败"))
        }
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

data class AuthResponse(
    val userId: String,
    val username: String?,
    val email: String?,
    val displayName: String?,
    val userType: String,
    val roles: List<String>
)
