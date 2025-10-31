package com.promptflow.common.jwt

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import java.util.*
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.security.Key
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

class JwtUtilsTest {

    private lateinit var jwtUtils: JwtUtils

    @BeforeEach
    fun setUp() {
        jwtUtils = object : JwtUtils() {
            init {
                // 使用反射设置测试配置
                val secretField = JwtUtils::class.java.getDeclaredField("jwtSecret")
                secretField.isAccessible = true
                secretField.set(this, "test-secret-key-for-unit-testing")
                
                val expirationField = JwtUtils::class.java.getDeclaredField("jwtExpirationHours")
                expirationField.isAccessible = true
                expirationField.set(this, 1L)
            }
        }
    }

    @Test
    fun `should generate valid JWT token`() {
        // Given
        val claims = JwtClaims(
            userId = "1234567890",
            username = "testuser",
            email = "test@example.com",
            roles = listOf("USER", "ADMIN"),
            sessionId = UUID.randomUUID().toString(),
            userType = "REGISTERED"
        )

        // When
        val token = jwtUtils.generateToken(claims)

        // Then
        assertNotNull(token)
        assertTrue(token.isNotEmpty())
        assertTrue(token.contains(".")) // JWT tokens have 3 parts separated by dots
    }

    @Test
    fun `should validate and parse valid JWT token`() {
        // Given
        val originalClaims = JwtClaims(
            userId = "1234567890",
            username = "testuser",
            email = "test@example.com",
            roles = listOf("USER", "ADMIN"),
            sessionId = UUID.randomUUID().toString(),
            userType = "REGISTERED"
        )
        val token = jwtUtils.generateToken(originalClaims)

        // When
        val parsedClaims = jwtUtils.validateAndParseToken(token)

        // Then
        assertNotNull(parsedClaims)
        assertEquals(originalClaims.userId, parsedClaims!!.userId)
        assertEquals(originalClaims.username, parsedClaims.username)
        assertEquals(originalClaims.email, parsedClaims.email)
        assertEquals(originalClaims.roles, parsedClaims.roles)
        assertEquals(originalClaims.sessionId, parsedClaims.sessionId)
        assertEquals(originalClaims.userType, parsedClaims.userType)
    }

    @Test
    fun `should return null for invalid token`() {
        // Given
        val invalidToken = "invalid.token.here"

        // When
        val result = jwtUtils.validateAndParseToken(invalidToken)

        // Then
        assertNull(result)
    }

    @Test
    fun `should return null for tampered token`() {
        // Given
        val claims = JwtClaims(
            userId = "1234567890",
            username = "testuser",
            email = "test@example.com",
            roles = listOf("USER"),
            sessionId = UUID.randomUUID().toString(),
            userType = "REGISTERED"
        )
        val validToken = jwtUtils.generateToken(claims)
        val tamperedToken = if (validToken.length > 10) {
            validToken.substring(0, validToken.length - 10) + "tampered"
        } else {
            "tampered.$validToken"
        }

        // When
        val result = jwtUtils.validateAndParseToken(tamperedToken)

        // Then
        assertNull(result)
    }

    @Test
    fun `should extract user ID from valid token`() {
        // Given
        val expectedUserId = "1234567890"
        val claims = JwtClaims(
            userId = expectedUserId,
            username = "testuser",
            email = "test@example.com",
            roles = listOf("USER"),
            sessionId = UUID.randomUUID().toString(),
            userType = "REGISTERED"
        )
        val token = jwtUtils.generateToken(claims)

        // When
        val extractedUserId = jwtUtils.extractUserId(token)

        // Then
        assertNotNull(extractedUserId)
        assertEquals(expectedUserId, extractedUserId)
    }

    @Test
    fun `should return null when extracting user ID from invalid token`() {
        // Given
        val invalidToken = "invalid.token.here"

        // When
        val extractedUserId = jwtUtils.extractUserId(invalidToken)

        // Then
        assertNull(extractedUserId)
    }

    @Test
    fun `should generate refresh token`() {
        // When
        val refreshToken1 = jwtUtils.generateRefreshToken()
        val refreshToken2 = jwtUtils.generateRefreshToken()

        // Then
        assertNotNull(refreshToken1)
        assertNotNull(refreshToken2)
        assertTrue(refreshToken1.isNotEmpty())
        assertTrue(refreshToken2.isNotEmpty())
        assertNotEquals(refreshToken1, refreshToken2) // Should be different each time
    }

    @Test
    fun `should detect expired token`() {
        // Given
        val claims = JwtClaims(
            userId = "1234567890",
            username = "testuser",
            email = "test@example.com",
            roles = listOf("USER"),
            sessionId = UUID.randomUUID().toString(),
            userType = "REGISTERED"
        )

        // When - 生成token并检查是否过期
        val token = jwtUtils.generateToken(claims)
        
        // Then - 新生成的token不应该过期
        assertFalse(jwtUtils.isTokenExpired(token))
    }

    @Test
    fun `should detect token expiration correctly`() {
        // Given - 创建一个过期的token（通过设置很短的过期时间）
        val expiredJwtUtils = object : JwtUtils() {
            init {
                // 使用反射设置测试配置
                val secretField = JwtUtils::class.java.getDeclaredField("jwtSecret")
                secretField.isAccessible = true
                secretField.set(this, "test-secret-key-for-unit-testing")
                
                val expirationField = JwtUtils::class.java.getDeclaredField("jwtExpirationHours")
                expirationField.isAccessible = true
                expirationField.set(this, -1L) // 负值表示过去
            }
        }

        val claims = JwtClaims(
            userId = "1234567890",
            username = "testuser",
            email = "test@example.com",
            roles = listOf("USER"),
            sessionId = UUID.randomUUID().toString(),
            userType = "REGISTERED"
        )

        // 这里我们直接测试isTokenExpired方法
        val token = expiredJwtUtils.generateToken(claims)
        
        // When & Then - 新生成的token应该过期
        assertTrue(expiredJwtUtils.isTokenExpired(token))
    }
}
