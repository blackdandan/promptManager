package com.promptflow.user.domain.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.LocalDateTime

@Document(collection = "user_sessions")
data class UserSession(
    @Id
    val id: String? = null,
    
    @Field("user_id")
    val userId: String,
    
    @Field("token")
    val token: String,
    
    @Field("refresh_token")
    val refreshToken: String,
    
    @Field("expires_at")
    val expiresAt: LocalDateTime,
    
    @Field("device_info")
    val deviceInfo: DeviceInfo,
    
    @Field("ip_address")
    val ipAddress: String? = null,
    
    @Field("user_agent")
    val userAgent: String? = null,
    
    @Field("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
)

data class DeviceInfo(
    @Field("device_id")
    val deviceId: String,
    
    @Field("device_type")
    val deviceType: DeviceType,
    
    @Field("os")
    val os: String? = null,
    
    @Field("browser")
    val browser: String? = null,
    
    @Field("app_version")
    val appVersion: String? = null
)

enum class DeviceType {
    WEB,            // Web端
    ANDROID,        // Android端
    IOS             // iOS端
}
