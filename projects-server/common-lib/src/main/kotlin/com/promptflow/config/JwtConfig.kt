package com.promptflow.config

import com.promptflow.common.jwt.JwtUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JwtConfig {

    @Value("\${app.jwt.secret:promptflow-secret-key-change-in-production}")
    private lateinit var jwtSecret: String

    @Value("\${app.jwt.expiration.hours:24}")
    private var jwtExpirationHours: Long = 24

    @Bean
    fun jwtUtils(): JwtUtils {
        return JwtUtils().apply {
            // 这里通过反射设置字段值，因为JwtUtils使用了@Value注解
            // 在实际使用中，这些值会在运行时由Spring注入
        }
    }
}
