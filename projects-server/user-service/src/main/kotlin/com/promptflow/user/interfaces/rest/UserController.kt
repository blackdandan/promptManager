package com.promptflow.user.interfaces.rest

import com.promptflow.common.dto.ApiResponse
import com.promptflow.user.application.service.UserService
import com.promptflow.user.domain.model.User
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService
) {
    
    private val log = LoggerFactory.getLogger(this::class.java)
    
    @PutMapping("/profile")
    fun updateProfile(
        @RequestHeader("X-User-Id") userId: String,
        @RequestBody request: UpdateUserProfileRequest
    ): ResponseEntity<ApiResponse<User>> {
        log.info("更新用户资料: userId=$userId, displayName=${request.displayName}")
        
        return try {
            val updatedUser = userService.updateUserProfile(
                userId = userId,
                displayName = request.displayName,
                avatarUrl = request.avatarUrl
            )
            ResponseEntity.ok(ApiResponse.success(updatedUser, "用户资料更新成功"))
        } catch (e: Exception) {
            log.error("更新用户资料失败: ${e.message}")
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("USER_UPDATE_FAILED", e.message ?: "用户资料更新失败"))
        }
    }
    
    @GetMapping("/profile")
    fun getProfile(@RequestHeader("X-User-Id") userId: String): ResponseEntity<ApiResponse<User>> {
        log.info("获取用户资料: userId=$userId")
        
        return try {
            val user = userService.getUserById(userId)
            ResponseEntity.ok(ApiResponse.success(user, "获取用户资料成功"))
        } catch (e: Exception) {
            log.error("获取用户资料失败: ${e.message}")
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("USER_NOT_FOUND", e.message ?: "用户不存在"))
        }
    }
}

data class UpdateUserProfileRequest(
    val displayName: String? = null,
    val avatarUrl: String? = null
)
