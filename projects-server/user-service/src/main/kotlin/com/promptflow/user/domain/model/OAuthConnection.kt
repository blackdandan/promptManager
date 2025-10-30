package com.promptflow.user.domain.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.LocalDateTime

@Document(collection = "oauth_connections")
data class OAuthConnection(
    @Id
    val id: String? = null,
    
    @Field("user_id")
    val userId: String,
    
    @Field("provider")
    val provider: OAuthProvider,
    
    @Field("provider_user_id")
    val providerUserId: String,
    
    @Field("access_token")
    val accessToken: String? = null,
    
    @Field("refresh_token")
    val refreshToken: String? = null,
    
    @Field("expires_at")
    val expiresAt: LocalDateTime? = null,
    
    @Field("profile_data")
    val profileData: Map<String, Any>? = null,
    
    @Field("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Field("updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class OAuthProvider {
    GITHUB,         // GitHub登录
    GOOGLE,         // Google登录
    WECHAT,         // 微信登录
    APPLE           // Apple登录
}
