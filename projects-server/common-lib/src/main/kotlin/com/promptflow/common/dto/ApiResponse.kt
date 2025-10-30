package com.promptflow.common.dto

data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val error: ApiError? = null
) {
    companion object {
        fun <T> success(data: T? = null, message: String? = null): ApiResponse<T> {
            return ApiResponse(
                success = true,
                data = data,
                message = message
            )
        }
        
        fun <T> error(error: ApiError, message: String? = null): ApiResponse<T> {
            return ApiResponse(
                success = false,
                message = message,
                error = error
            )
        }
        
        fun <T> error(code: String, message: String, details: String? = null): ApiResponse<T> {
            return ApiResponse(
                success = false,
                error = ApiError(code, message, details)
            )
        }
    }
}

data class ApiError(
    val code: String,
    val message: String,
    val details: String? = null
)

// 常见错误码
object ErrorCodes {
    // 认证相关
    const val AUTH_001 = "AUTH_001" // 认证失败
    const val AUTH_002 = "AUTH_002" // Token过期
    const val AUTH_003 = "AUTH_003" // 三方登录失败
    const val AUTH_004 = "AUTH_004" // 游客升级失败
    
    // 验证相关
    const val VALIDATION_001 = "VALIDATION_001" // 参数验证失败
    
    // 资源相关
    const val NOT_FOUND_001 = "NOT_FOUND_001" // 用户不存在
    const val NOT_FOUND_002 = "NOT_FOUND_002" // Prompt不存在
    
    // 冲突相关
    const val CONFLICT_001 = "CONFLICT_001" // 邮箱已存在
    const val CONFLICT_002 = "CONFLICT_002" // 用户名已存在
    
    // 权限相关
    const val ACCESS_DENIED_001 = "ACCESS_DENIED_001" // 无权访问
    const val ACCESS_DENIED_002 = "ACCESS_DENIED_002" // 权限不足
    
    // 系统相关
    const val SYSTEM_001 = "SYSTEM_001" // 系统错误
    const val SYSTEM_002 = "SYSTEM_002" // 数据库错误
}
