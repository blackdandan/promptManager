package com.promptflow.gateway.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.security.Key
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

/**
 * 网关服务专用的JWT工具类
 * 不依赖Spring框架，避免与WebFlux冲突
 */
class GatewayJwtUtils(
    private val jwtSecret: String = "promptflow-secret-key-change-in-production",
    private val jwtExpirationHours: Long = 24
) {

    private val signingKey: Key
        get() = Keys.hmacShaKeyFor(jwtSecret.toByteArray())

    /**
     * 生成JWT Token
     */
    fun generateToken(claims: JwtClaims): String {
        val now = Date()
        val expiration = Date.from(
            LocalDateTime.now()
                .plusHours(jwtExpirationHours)
                .atZone(ZoneId.systemDefault())
                .toInstant()
        )

        return Jwts.builder()
            .setClaims(createClaims(claims))
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
     * 生成刷新Token（简单的UUID，用于关联会话）
     */
    fun generateRefreshToken(): String {
        return UUID.randomUUID().toString().replace("-", "")
    }

    /**
     * 创建JWT Claims
     */
    private fun createClaims(claims: JwtClaims): Map<String, Any> {
        return mutableMapOf<String, Any>().apply {
            put("userId", claims.userId)
            put("username", claims.username)
            put("email", claims.email)
            put("roles", claims.roles.joinToString(","))
            put("sessionId", claims.sessionId)
            put("userType", claims.userType)
        }
    }

    /**
     * 解析JWT Claims
     */
    private fun parseClaims(claims: Claims): JwtClaims {
        return JwtClaims(
            userId = claims["userId"] as String,
            username = claims["username"] as String,
            email = claims["email"] as String,
            roles = (claims["roles"] as String).split(","),
            sessionId = claims["sessionId"] as String,
            userType = claims["userType"] as String
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
    val userType: String = "REGISTERED"
)
