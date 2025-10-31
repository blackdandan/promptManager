package com.promptflow.user.application.service

import com.promptflow.user.domain.model.User
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.Key
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

/**
 * 用户服务专用的JWT工具类
 * 用于生成和验证JWT Token
 */
@Component
class UserJwtUtils(
    @Value("\${app.jwt.secret:promptflow-user-secret-key-change-in-production}") 
    private val jwtSecret: String,
    
    @Value("\${app.jwt.expiration:3600}") 
    private val jwtExpirationSeconds: Long = 3600
) {

    private val signingKey: Key
        get() = Keys.hmacShaKeyFor(jwtSecret.toByteArray())

    /**
     * 生成JWT Token
     */
    fun generateToken(user: User, sessionId: String): String {
        val now = Date()
        val expiration = Date.from(
            LocalDateTime.now()
                .plusSeconds(jwtExpirationSeconds)
                .atZone(ZoneId.systemDefault())
                .toInstant()
        )

        return Jwts.builder()
            .setClaims(createClaims(user, sessionId))
            .setIssuedAt(now)
            .setExpiration(expiration)
            .signWith(signingKey)
            .compact()
    }

    /**
     * 验证并解析JWT Token
     */
    fun validateAndParseToken(token: String): JwtClaims? {
        return try {
            val claims = Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .body

            parseClaims(claims)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 从Token中提取用户ID
     */
    fun extractUserId(token: String): String? {
        return validateAndParseToken(token)?.userId
    }

    /**
     * 检查Token是否过期
     */
    fun isTokenExpired(token: String): Boolean {
        return try {
            val claims = Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .body

            claims.expiration.before(Date())
        } catch (e: Exception) {
            true
        }
    }

    /**
     * 创建JWT Claims
     */
    private fun createClaims(user: User, sessionId: String): Map<String, Any> {
        return mutableMapOf<String, Any>().apply {
            put("userId", user.id!!)
            put("username", user.username?:"")
            put("email", user.email?:"")
            put("roles", user.roles.joinToString(","))
            put("sessionId", sessionId)
            put("userType", user.userType.name)
            put("displayName", user.displayName ?: user.username ?: "")
        }
    }

    /**
     * 解析JWT Claims
     */
    private fun parseClaims(claims: Claims): JwtClaims {
        return JwtClaims(
            userId = claims["userId"]?.toString() ?: "",
            username = claims["username"]?.toString() ?: "",
            email = claims["email"]?.toString() ?: "",
            roles = (claims["roles"]?.toString() ?: "").split(","),
            sessionId = claims["sessionId"]?.toString() ?: "",
            userType = claims["userType"]?.toString() ?: "REGISTERED",
            displayName = claims["displayName"]?.toString() ?: ""
        )
    }
}

/**
 * JWT Claims数据结构
 */
data class JwtClaims(
    val userId: String,
    val username: String,
    val email: String,
    val roles: List<String>,
    val sessionId: String,
    val userType: String = "REGISTERED",
    val displayName: String = ""
)
