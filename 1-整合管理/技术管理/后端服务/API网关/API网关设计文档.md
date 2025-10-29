# API网关设计文档

## 1. 服务概述

### 1.1 服务职责
API网关作为系统的统一入口，负责请求路由、认证鉴权、限流熔断等网关功能。

### 1.2 核心功能
- **请求路由**：动态路由到后端服务
- **认证鉴权**：统一JWT认证
- **限流保护**：基于Redis的限流
- **熔断保护**：服务熔断和降级
- **日志记录**：请求日志和审计

### 1.3 技术栈
- **语言**：Kotlin
- **框架**：Spring Cloud Gateway
- **服务发现**：Spring Cloud Eureka
- **限流**：Redis Rate Limiter
- **构建工具**：Gradle
- **文档**：SpringDoc OpenAPI 3

## 2. 架构设计

### 2.1 网关架构图
```
┌─────────────────┐
│   客户端请求    │
└─────────────────┘
         │
┌─────────────────┐
│   API网关       │
│                 │
│ • 路由转发      │
│ • 认证鉴权      │
│ • 限流熔断      │
│ • 日志记录      │
└─────────────────┘
         │
┌─────────────────┐
│   Eureka注册中心 │
└─────────────────┘
         │
┌─────────────────┐
│  后端服务集群    │
│                 │
│ • 用户服务      │
│ • 业务服务      │
└─────────────────┘
```

### 2.2 请求处理流程
```
1. 客户端请求 → 网关
2. 网关验证Token → 认证失败返回401
3. 网关限流检查 → 超限返回429
4. 网关路由转发 → 后端服务
5. 后端服务处理 → 返回结果
6. 网关记录日志 → 返回客户端
```

## 3. 路由配置

### 3.1 路由规则配置
```kotlin
@Configuration
class GatewayConfig {
    
    @Bean
    fun routes(builder: RouteLocatorBuilder): RouteLocator = builder.routes()
        // 用户服务路由
        .route("user-service") { r ->
            r.path("/auth/**", "/users/**")
                .filters { f ->
                    f.rewritePath("/auth/(?<segment>.*)", "/\${segment}")
                     .rewritePath("/users/(?<segment>.*)", "/\${segment}")
                     .requestRateLimiter { config ->
                         config.setRateLimiter(redisRateLimiter())
                         config.setKeyResolver(keyResolver())
                     }
                }
                .uri("lb://user-service")
        }
        // 业务服务路由
        .route("business-service") { r ->
            r.path("/prompts/**", "/tags/**", "/sync/**", "/shares/**")
                .filters { f ->
                    f.requestRateLimiter { config ->
                         config.setRateLimiter(redisRateLimiter())
                         config.setKeyResolver(keyResolver())
                     }
                }
                .uri("lb://business-service")
        }
        .build()
}
```

### 3.2 路由配置详情

#### 3.2.1 用户服务路由
- **路径模式**：`/auth/**`, `/users/**`
- **目标服务**：`user-service`
- **限流配置**：认证接口10请求/秒，用户接口20请求/秒
- **路径重写**：移除前缀

#### 3.2.2 业务服务路由
- **路径模式**：`/prompts/**`, `/tags/**`, `/sync/**`, `/shares/**`
- **目标服务**：`business-service`
- **限流配置**：100请求/秒
- **路径重写**：保持原路径

## 4. 认证模块

### 4.1 JWT认证过滤器
```kotlin
@Component
class JwtAuthenticationFilter(
    private val jwtUtil: JwtUtil
) : GlobalFilter {
    
    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val request = exchange.request
        val path = request.path.value()
        
        // 跳过认证的路径
        if (shouldSkipAuth(path)) {
            return chain.filter(exchange)
        }
        
        val token = extractToken(request)
        if (token == null) {
            return unauthorizedResponse(exchange, "未提供认证令牌")
        }
        
        return try {
            val claims = jwtUtil.validateToken(token)
            val userId = claims.subject
            val userType = claims.get("userType", String::class.java)
            
            // 添加用户信息到请求头
            val modifiedRequest = exchange.request.mutate()
                .header("X-User-Id", userId)
                .header("X-User-Type", userType)
                .build()
                
            chain.filter(exchange.mutate().request(modifiedRequest).build())
        } catch (e: Exception) {
            unauthorizedResponse(exchange, "认证令牌无效")
        }
    }
    
    private fun shouldSkipAuth(path: String): Boolean {
        return path.startsWith("/auth/login") || 
               path.startsWith("/auth/register") ||
               path.startsWith("/auth/guest") ||
               path.startsWith("/auth/oauth") ||
               path.startsWith("/shares/")
    }
    
    private fun extractToken(request: ServerHttpRequest): String? {
        val authHeader = request.headers.getFirst(HttpHeaders.AUTHORIZATION)
        return if (authHeader != null && authHeader.startsWith("Bearer ")) {
            authHeader.substring(7)
        } else {
            null
        }
    }
    
    private fun unauthorizedResponse(exchange: ServerWebExchange, message: String): Mono<Void> {
        val response = exchange.response
        response.statusCode = HttpStatus.UNAUTHORIZED
        response.headers.contentType = MediaType.APPLICATION_JSON
        
        val errorResponse = mapOf(
            "success" to false,
            "error" to mapOf(
                "code" to "AUTH_001",
                "message" to message
            ),
            "data" to null
        )
        
        val dataBuffer = response.bufferFactory()
            .wrap(ObjectMapper().writeValueAsBytes(errorResponse))
            
        return response.writeWith(Mono.just(dataBuffer))
    }
}
```

### 4.2 游客认证处理
```kotlin
@Component
class GuestAuthenticationFilter(
    private val jwtUtil: JwtUtil
) : GlobalFilter {
    
    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val request = exchange.request
        val path = request.path.value()
        
        // 仅处理游客相关路径
        if (!path.startsWith("/auth/guest")) {
            return chain.filter(exchange)
        }
        
        val token = extractToken(request)
        if (token != null) {
            return try {
                val claims = jwtUtil.validateToken(token)
                val guestId = claims.subject
                val userType = claims.get("userType", String::class.java)
                
                // 验证是否为游客token
                if (userType == "GUEST") {
                    val modifiedRequest = exchange.request.mutate()
                        .header("X-Guest-Id", guestId)
                        .header("X-User-Type", "GUEST")
                        .build()
                    return chain.filter(exchange.mutate().request(modifiedRequest).build())
                }
            } catch (e: Exception) {
                // Token验证失败，继续处理
            }
        }
        
        return chain.filter(exchange)
    }
}
```

## 5. 限流模块

### 5.1 Redis限流配置
```kotlin
@Configuration
class RateLimitConfig {
    
    @Bean
    fun keyResolver(): KeyResolver = KeyResolver { exchange ->
        val userId = exchange.request.headers.getFirst("X-User-Id")
        val guestId = exchange.request.headers.getFirst("X-Guest-Id")
        
        // 优先使用用户ID，其次游客ID，最后IP地址
        val key = when {
            userId != null -> "user:$userId"
            guestId != null -> "guest:$guestId"
            else -> {
                val remoteAddress = exchange.request.remoteAddress?.address?.hostAddress
                "ip:${remoteAddress ?: "unknown"}"
            }
        }
        Mono.just(key)
    }
    
    @Bean
    fun redisRateLimiter(): RedisRateLimiter = RedisRateLimiter(
        defaultReplenishRate = 100,  // 每秒100个令牌
        defaultBurstCapacity = 200   // 最大200个令牌
    )
}
```

### 5.2 限流规则配置
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service-auth
          uri: lb://user-service
          predicates:
            - Path=/auth/login,/auth/register,/auth/guest
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 10
                redis-rate-limiter.burstCapacity: 20
                key-resolver: "#{@keyResolver}"
        
        - id: user-service-api
          uri: lb://user-service
          predicates:
            - Path=/users/**
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 20
                redis-rate-limiter.burstCapacity: 40
                key-resolver: "#{@keyResolver}"
        
        - id: business-service
          uri: lb://business-service
          predicates:
            - Path=/prompts/**,/tags/**,/sync/**,/shares/**
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 100
                redis-rate-limiter.burstCapacity: 200
                key-resolver: "#{@keyResolver}"
```

## 6. 服务配置

### 6.1 Spring Cloud配置
```yaml
spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true
      globalcors:
        cors-configurations:
          '[/**]':
            allowed-origins: "*"
            allowed-methods:
              - GET
              - POST
              - PUT
              - DELETE
              - OPTIONS
            allowed-headers: "*"
            allow-credentials: true
    
  redis:
    host: localhost
    port: 6379

server:
  port: 8080

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka
  instance:
    prefer-ip-address: true

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

### 6.2 JWT配置
```yaml
jwt:
  secret: ${JWT_SECRET:default-secret-key}
  expiration: 86400000  # 24小时
  refresh-expiration: 604800000  # 7天
```

## 7. 健康检查和监控

### 7.1 健康检查端点
```kotlin
@RestController
class HealthController {
    
    @GetMapping("/actuator/health")
    fun health(): ResponseEntity<Map<String, Any>> {
        val healthStatus = mapOf(
            "status" to "UP",
            "timestamp" to Instant.now().toString(),
            "service" to "api-gateway",
            "version" to "1.0.0"
        )
        return ResponseEntity.ok(healthStatus)
    }
    
    @GetMapping("/actuator/info")
    fun info(): ResponseEntity<Map<String, Any>> {
        val info = mapOf(
            "name" to "API Gateway",
            "description" to "PromptManager API Gateway Service",
            "version" to "1.0.0",
            "environment" to "development"
        )
        return ResponseEntity.ok(info)
    }
}
```

### 7.2 网关指标监控
```kotlin
@Component
class GatewayMetricsFilter : GlobalFilter {
    
    private val requestCounter = Counter.builder("gateway.requests")
        .description("API Gateway请求计数器")
        .register(Metrics.globalRegistry)
    
    private val requestDuration = Timer.builder("gateway.request.duration")
        .description("API Gateway请求耗时")
        .register(Metrics.globalRegistry)
    
    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val startTime = System.currentTimeMillis()
        requestCounter.increment()
        
        return chain.filter(exchange).doOnSuccessOrError { _, throwable ->
            val duration = System.currentTimeMillis() - startTime
            requestDuration.record(duration, TimeUnit.MILLISECONDS)
            
            if (throwable != null) {
                // 记录错误指标
                Counter.builder("gateway.errors")
                    .tag("path", exchange.request.path.value())
                    .register(Metrics.globalRegistry)
                    .increment()
            }
        }
    }
}
```

## 8. 错误处理

### 8.1 全局错误处理
```kotlin
@Component
class GlobalErrorHandler : ErrorWebExceptionHandler {
    
    override fun handle(exchange: ServerWebExchange, ex: Throwable): Mono<Void> {
        val response = exchange.response
        response.headers.contentType = MediaType.APPLICATION_JSON
        
        val (status, errorCode, message) = when (ex) {
            is RateLimiterException -> Triple(HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMIT_001", "请求频率超限")
            is AuthenticationException -> Triple(HttpStatus.UNAUTHORIZED, "AUTH_001", "认证失败")
            is ServiceUnavailableException -> Triple(HttpStatus.SERVICE_UNAVAILABLE, "SERVICE_001", "服务暂时不可用")
            else -> Triple(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_001", "内部服务器错误")
        }
        
        response.statusCode = status
        
        val errorResponse = mapOf(
            "success" to false,
            "error" to mapOf(
                "code" to errorCode,
                "message" to message,
                "details" to ex.message
            ),
            "data" to null
        )
        
        val dataBuffer = response.bufferFactory()
            .wrap(ObjectMapper().writeValueAsBytes(errorResponse))
            
        return response.writeWith(Mono.just(dataBuffer))
    }
}
```

## 9. 部署配置

### 9.1 Docker配置
```dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY build/libs/api-gateway-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 9.2 环境变量配置
```yaml
# application-docker.yml
spring:
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
  redis:
    host: redis
    port: 6379

server:
  port: 8080

eureka:
  client:
    service-url:
      defaultZone: http://eureka-server:8761/eureka

jwt:
  secret: ${JWT_SECRET}
```

---
**文档版本**：1.0  
**编制人**：项目经理  
**编制日期**：第1周  
**下次评审**：API网关实现阶段开始前
