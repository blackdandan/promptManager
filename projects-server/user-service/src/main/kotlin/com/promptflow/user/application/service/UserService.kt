package com.promptflow.user.application.service

import com.promptflow.user.domain.model.User
import com.promptflow.user.domain.model.UserStatus
import com.promptflow.user.domain.model.UserType
import com.promptflow.user.infrastructure.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    
    private val log = LoggerFactory.getLogger(this::class.java)
    
    fun registerUser(
        username: String,
        email: String,
        password: String,
        displayName: String? = null
    ): User {
        // 检查用户是否已存在
        if (userRepository.existsByEmail(email)) {
            throw UserAlreadyExistsException("邮箱已被注册: $email")
        }
        if (userRepository.existsByUsername(username)) {
            throw UserAlreadyExistsException("用户名已被使用: $username")
        }
        
        // 创建用户
        val user = User(
            username = username,
            email = email,
            passwordHash = passwordEncoder.encode(password),
            displayName = displayName ?: username,
            userType = UserType.REGISTERED,
            status = UserStatus.ACTIVE,
            roles = listOf("USER"),
            emailVerified = false
        )
        
        return userRepository.save(user).also {
            log.info("用户注册成功: ${it.username} (${it.email})")
        }
    }
    
    fun authenticateUser(email: String, password: String): User {
        val user = userRepository.findByEmail(email)
            .orElseThrow { UserNotFoundException("用户不存在: $email") }
        
        // 检查用户状态
        if (user.status != UserStatus.ACTIVE) {
            throw UserNotActiveException("用户账户未激活: ${user.status}")
        }
        
        // 验证密码
        if (!passwordEncoder.matches(password, user.passwordHash)) {
            throw InvalidCredentialsException("密码错误")
        }
        
        // 更新最后登录时间
        val updatedUser = user.copy(lastLoginAt = LocalDateTime.now())
        userRepository.save(updatedUser)
        
        log.info("用户登录成功: ${user.username}")
        return updatedUser
    }
    
    fun getUserById(id: String): User {
        return userRepository.findById(id)
            .orElseThrow { UserNotFoundException("用户不存在: $id") }
    }
    
    fun getUserByEmail(email: String): User {
        return userRepository.findByEmail(email)
            .orElseThrow { UserNotFoundException("用户不存在: $email") }
    }
    
    fun updateUserProfile(
        userId: String,
        displayName: String?,
        avatarUrl: String?
    ): User {
        val user = getUserById(userId)
        
        val updatedUser = user.copy(
            displayName = displayName ?: user.displayName,
            avatarUrl = avatarUrl ?: user.avatarUrl,
            updatedAt = LocalDateTime.now()
        )
        
        return userRepository.save(updatedUser).also {
            log.info("用户资料更新: ${it.username}")
        }
    }
    
    fun changePassword(userId: String, oldPassword: String, newPassword: String): User {
        val user = getUserById(userId)
        
        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.passwordHash)) {
            throw InvalidCredentialsException("原密码错误")
        }
        
        val updatedUser = user.copy(
            passwordHash = passwordEncoder.encode(newPassword),
            updatedAt = LocalDateTime.now()
        )
        
        return userRepository.save(updatedUser).also {
            log.info("用户密码修改: ${user.username}")
        }
    }
    
    fun deactivateUser(userId: String): User {
        val user = getUserById(userId)
        
        val updatedUser = user.copy(
            status = UserStatus.INACTIVE,
            updatedAt = LocalDateTime.now()
        )
        
        return userRepository.save(updatedUser).also {
            log.info("用户账户停用: ${user.username}")
        }
    }
    
    fun searchUsers(query: String): List<User> {
        return userRepository.findByEmailContaining(query) + 
               userRepository.findByUsernameContaining(query)
    }
    
    fun getUserStats(userId: String): Map<String, Any?> {
        val user = getUserById(userId)
        
        return mapOf(
            "userId" to user.id,
            "username" to user.username,
            "email" to user.email,
            "displayName" to user.displayName,
            "status" to user.status.name,
            "role" to user.roles,
            "lastLoginAt" to user.lastLoginAt,
            "createdAt" to user.createdAt,
            "updatedAt" to user.updatedAt
        )
    }
}

class UserAlreadyExistsException(message: String) : RuntimeException(message)
class UserNotFoundException(message: String) : RuntimeException(message)
class UserNotActiveException(message: String) : RuntimeException(message)
class InvalidCredentialsException(message: String) : RuntimeException(message)
