package com.promptflow.user.domain.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.LocalDateTime

@Document(collection = "users")
data class User(
    @Id
    val id: String? = null,
    
    @Field("email")
    val email: String? = null,           // 允许为空（三方登录用户）
    
    @Field("password_hash")
    val passwordHash: String? = null,    // 允许为空（三方登录用户）
    
    @Field("username")
    val username: String? = null,        // 允许为空（游客用户）
    
    @Field("display_name")
    val displayName: String? = null,
    
    @Field("avatar_url")
    val avatarUrl: String? = null,
    
    @Field("user_type")
    val userType: UserType = UserType.REGISTERED,
    
    @Field("status")
    val status: UserStatus = UserStatus.ACTIVE,
    
    @Field("roles")
    val roles: List<String> = listOf("USER"),
    
    @Field("email_verified")
    val emailVerified: Boolean = false,
    
    @Field("last_login_at")
    val lastLoginAt: LocalDateTime? = null,
    
    @Field("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Field("updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    
    @Field("profile")
    val profile: UserProfile? = null
)

data class UserProfile(
    @Field("bio")
    val bio: String? = null,
    
    @Field("location")
    val location: String? = null,
    
    @Field("website")
    val website: String? = null,
    
    @Field("preferences")
    val preferences: Map<String, Any> = emptyMap()
)

enum class UserType {
    REGISTERED,     // 注册用户
    GUEST,          // 游客
    OAUTH           // 三方登录用户
}

enum class UserStatus {
    ACTIVE,         // 活跃
    INACTIVE,       // 非活跃
    SUSPENDED,      // 暂停
    DELETED         // 已删除
}
