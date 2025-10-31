package com.promptflow.user.application.service

import com.promptflow.user.domain.model.User
import com.promptflow.user.domain.model.UserType
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class UserJwtUtilsTest {

    private lateinit var jwtUtils: UserJwtUtils

    @BeforeEach
    fun setUp() {
        jwtUtils = UserJwtUtils("test-secret-key-change-in-production", 3600)
    }

    @Test
    fun `should generate valid JWT token`() {
        // Arrange
        val user = User(
            id = "user123",
            username = "testuser",
            email = "test@example.com",
            passwordHash = "hashed_password",
            displayName = "Test User",
            userType = UserType.REGISTERED,
            roles = listOf("USER", "ADMIN"),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        val sessionId = "session123"

        // Act
        val token = jwtUtils.generateToken(user, sessionId)

        // Assert
        assertNotNull(token)
        assertTrue(token.isNotBlank())
        assertTrue(token.contains(".")) // JWT tokens have 3 parts separated by dots
    }

    @Test
    fun `should validate and parse valid token`() {
        // Arrange
        val user = User(
            id = "user123",
            username = "testuser",
            email = "test@example.com",
            passwordHash = "hashed_password",
            displayName = "Test User",
            userType = UserType.REGISTERED,
            roles = listOf("USER"),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        val sessionId = "session123"

        val token = jwtUtils.generateToken(user, sessionId)

        // Act
        val claims = jwtUtils.validateAndParseToken(token)

        // Assert
        assertNotNull(claims)
        assertEquals("user123", claims?.userId)
        assertEquals("testuser", claims?.username)
        assertEquals("test@example.com", claims?.email)
        assertEquals(listOf("USER"), claims?.roles)
        assertEquals("session123", claims?.sessionId)
        assertEquals("REGISTERED", claims?.userType)
        assertEquals("Test User", claims?.displayName)
    }

    @Test
    fun `should return null for invalid token`() {
        // Arrange
        val invalidToken = "invalid.jwt.token"

        // Act
        val claims = jwtUtils.validateAndParseToken(invalidToken)

        // Assert
        assertNull(claims)
    }

    @Test
    fun `should return null for expired token`() {
        // Arrange
        val expiredJwtUtils = UserJwtUtils("test-secret-key-change-in-production", -1) // Negative expiration for testing
        val user = User(
            id = "user123",
            username = "testuser",
            email = "test@example.com",
            passwordHash = "hashed_password",
            displayName = "Test User",
            userType = UserType.REGISTERED,
            roles = listOf("USER"),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        val sessionId = "session123"

        val token = expiredJwtUtils.generateToken(user, sessionId)

        // Act
        val claims = jwtUtils.validateAndParseToken(token)

        // Assert
        assertNull(claims)
    }

    @Test
    fun `should extract user ID from valid token`() {
        // Arrange
        val user = User(
            id = "user123",
            username = "testuser",
            email = "test@example.com",
            passwordHash = "hashed_password",
            displayName = "Test User",
            userType = UserType.REGISTERED,
            roles = listOf("USER"),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        val sessionId = "session123"

        val token = jwtUtils.generateToken(user, sessionId)

        // Act
        val userId = jwtUtils.extractUserId(token)

        // Assert
        assertEquals("user123", userId)
    }

    @Test
    fun `should return null when extracting user ID from invalid token`() {
        // Arrange
        val invalidToken = "invalid.jwt.token"

        // Act
        val userId = jwtUtils.extractUserId(invalidToken)

        // Assert
        assertNull(userId)
    }

    @Test
    fun `should detect expired token`() {
        // Arrange
        val expiredJwtUtils = UserJwtUtils("test-secret-key-change-in-production", -1) // Negative expiration for testing
        val user = User(
            id = "user123",
            username = "testuser",
            email = "test@example.com",
            passwordHash = "hashed_password",
            displayName = "Test User",
            userType = UserType.REGISTERED,
            roles = listOf("USER"),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        val sessionId = "session123"

        val token = expiredJwtUtils.generateToken(user, sessionId)

        // Act
        val isExpired = jwtUtils.isTokenExpired(token)

        // Assert
        assertTrue(isExpired)
    }

    @Test
    fun `should detect valid token as not expired`() {
        // Arrange
        val user = User(
            id = "user123",
            username = "testuser",
            email = "test@example.com",
            passwordHash = "hashed_password",
            displayName = "Test User",
            userType = UserType.REGISTERED,
            roles = listOf("USER"),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        val sessionId = "session123"

        val token = jwtUtils.generateToken(user, sessionId)

        // Act
        val isExpired = jwtUtils.isTokenExpired(token)

        // Assert
        assertFalse(isExpired)
    }

    @Test
    fun `should handle invalid token in isTokenExpired check`() {
        // Arrange
        val invalidToken = "invalid.jwt.token"

        // Act
        val isExpired = jwtUtils.isTokenExpired(invalidToken)

        // Assert
        assertTrue(isExpired) // Invalid tokens are considered expired
    }

    @Test
    fun `should handle user with null display name`() {
        // Arrange
        val user = User(
            id = "user123",
            username = "testuser",
            email = "test@example.com",
            passwordHash = "hashed_password",
            displayName = null,
            userType = UserType.REGISTERED,
            roles = listOf("USER"),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        val sessionId = "session123"

        // Act
        val token = jwtUtils.generateToken(user, sessionId)
        val claims = jwtUtils.validateAndParseToken(token)

        // Assert
        assertNotNull(claims)
        assertEquals("testuser", claims?.displayName) // Should fallback to username
    }

    @Test
    fun `should handle multiple roles`() {
        // Arrange
        val user = User(
            id = "user123",
            username = "testuser",
            email = "test@example.com",
            passwordHash = "hashed_password",
            displayName = "Test User",
            userType = UserType.REGISTERED,
            roles = listOf("USER", "ADMIN", "MODERATOR"),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        val sessionId = "session123"

        // Act
        val token = jwtUtils.generateToken(user, sessionId)
        val claims = jwtUtils.validateAndParseToken(token)

        // Assert
        assertNotNull(claims)
        assertEquals(listOf("USER", "ADMIN", "MODERATOR"), claims?.roles)
    }
}
