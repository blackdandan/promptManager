package com.promptflow.gateway.filter

import com.promptflow.gateway.jwt.GatewayJwtUtils
import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class JwtAuthenticationFilter : AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config>(Config::class.java) {

    private val jwtUtils = GatewayJwtUtils()

    private val log = LoggerFactory.getLogger(this::class.java)

    override fun apply(config: Config): GatewayFilter {
        return GatewayFilter { exchange: ServerWebExchange, chain: GatewayFilterChain ->
            val request = exchange.request
            val path = request.path.value()

            // 检查是否为公开接口
            if (isPublicPath(path)) {
                log.debug("公开接口放行: $path")
                return@GatewayFilter chain.filter(exchange)
            }

            // 获取Authorization头
            val authHeader = request.headers.getFirst(HttpHeaders.AUTHORIZATION)
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("缺少有效的Authorization头: $path")
                return@GatewayFilter unauthorizedResponse(exchange)
            }

            // 提取Token
            val token = authHeader.substring(7)

            // 验证JWT Token
            val jwtClaims = jwtUtils.validateAndParseToken(token)
            if (jwtClaims == null) {
                log.warn("JWT Token验证失败: $path")
                return@GatewayFilter unauthorizedResponse(exchange)
            }

            // 检查Token是否过期
            if (jwtUtils.isTokenExpired(token)) {
                log.warn("JWT Token已过期: $path")
                return@GatewayFilter unauthorizedResponse(exchange)
            }

            log.debug("JWT Token验证成功: userId=${jwtClaims.userId}, path=$path")

            // 将用户信息添加到请求头中传递给下游服务
            val modifiedRequest = exchange.request.mutate().apply {
                header("X-User-ID", jwtClaims.userId)
                header("X-Username", jwtClaims.username)
                header("X-Email", jwtClaims.email)
                header("X-Roles", jwtClaims.roles.joinToString(","))
                header("X-Session-ID", jwtClaims.sessionId)
                header("X-User-Type", jwtClaims.userType)
            }.build()

            val modifiedExchange = exchange.mutate().request(modifiedRequest).build()

            chain.filter(modifiedExchange)
        }
    }

    /**
     * 检查是否为公开接口
     */
    private fun isPublicPath(path: String): Boolean {
        val publicRegex = listOf(
            "^/api/auth.*",
            "^/api/sessions.*",
            "^/actuator.*",
            "^/v3/api-docs.*",
            "^/swagger-ui.*"
        )
        return publicRegex.any { Regex(it).matches(path) }
    }

    /**
     * 返回未授权响应
     */
    private fun unauthorizedResponse(exchange: ServerWebExchange): Mono<Void> {
        val response = exchange.response
        response.statusCode = HttpStatus.UNAUTHORIZED
        response.headers.add(HttpHeaders.CONTENT_TYPE, "application/json")
        
        val errorBody = """
            {
                "code": "AUTH_001",
                "message": "未授权访问",
                "data": null
            }
        """.trimIndent()
        
        val buffer = response.bufferFactory().wrap(errorBody.toByteArray())
        return response.writeWith(Mono.just(buffer))
    }

    class Config
}
