package com.promptflow.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

class HeaderAuthenticationFilter : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(this::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val userId = request.getHeader("X-User-ID")
        val username = request.getHeader("X-Username")
        val email = request.getHeader("X-Email")
        val rolesHeader = request.getHeader("X-Roles")
        val sessionId = request.getHeader("X-Session-ID")
        val userType = request.getHeader("X-User-Type")

        // 检查是否包含必要的用户信息头
        if (userId.isNullOrBlank()) {
            log.debug("缺少X-User-ID头信息，跳过认证")
            filterChain.doFilter(request, response)
            return
        }

        try {
            // 解析角色信息
            val roles = parseRoles(rolesHeader)
            
            // 创建认证对象
            val authentication = UsernamePasswordAuthenticationToken(
                userId, // principal
                null,   // credentials
                roles.map { SimpleGrantedAuthority("ROLE_$it") }
            )
            
            // 设置认证详情
            authentication.details = mapOf(
                "username" to username,
                "email" to email,
                "sessionId" to sessionId,
                "userType" to userType
            )
            
            // 设置安全上下文
            SecurityContextHolder.getContext().authentication = authentication
            
            log.debug("Header认证成功: userId=$userId, username=$username, roles=$roles")
            
        } catch (e: Exception) {
            log.warn("Header认证失败: ${e.message}")
            // 认证失败时不设置安全上下文，让后续过滤器处理
        }

        filterChain.doFilter(request, response)
    }

    /**
     * 解析角色字符串
     */
    private fun parseRoles(rolesHeader: String?): List<String> {
        return if (rolesHeader.isNullOrBlank()) {
            listOf("USER") // 默认角色
        } else {
            rolesHeader.split(",").map { it.trim() }.filter { it.isNotBlank() }
        }
    }

    /**
     * 跳过公开接口的认证
     */
    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.servletPath
        val publicPaths = listOf(
            "/auth/",
            "/sessions/",
            "/actuator/",
            "/v3/api-docs/",
            "/swagger-ui/",
            "/swagger-ui.html"
        )
        return publicPaths.any { path.startsWith(it) }
    }
}
