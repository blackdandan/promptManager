package com.promptflow.common.exception

import com.promptflow.common.dto.ApiResponse
import com.promptflow.common.dto.ErrorCodes
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest

@RestControllerAdvice
class GlobalExceptionHandler {
    
    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    
    
    // 参数验证异常
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException, request: WebRequest): ResponseEntity<ApiResponse<Any>> {
        log.warn("参数验证异常: ${ex.message}")
        
        val errors = ex.bindingResult.allErrors.map { error ->
            val fieldName = (error as FieldError).field
            val errorMessage = error.defaultMessage ?: "无效参数"
            "$fieldName: $errorMessage"
        }
        
        val errorMessage = if (errors.size == 1) errors[0] else "参数验证失败"
        val details = if (errors.size > 1) errors.joinToString("; ") else null
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(
                ErrorCodes.VALIDATION_001,
                errorMessage,
                details
            ))
    }
    
    // 非法参数异常
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException, request: WebRequest): ResponseEntity<ApiResponse<Any>> {
        log.warn("非法参数异常: ${ex.message}")
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(ErrorCodes.VALIDATION_001, ex.message ?: "非法参数"))
    }
    
    // 权限不足异常
    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDeniedException(ex: AccessDeniedException, request: WebRequest): ResponseEntity<ApiResponse<Any>> {
        log.warn("权限不足异常: ${ex.message}")
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ApiResponse.error(ErrorCodes.ACCESS_DENIED_001, ex.message ?: "权限不足"))
    }
    
    // 通用运行时异常
    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(ex: RuntimeException, request: WebRequest): ResponseEntity<ApiResponse<Any>> {
        log.error("运行时异常: ${ex.message}")
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error(ErrorCodes.SYSTEM_001, "系统内部错误"))
    }
    
    // 通用异常
    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception, request: WebRequest): ResponseEntity<ApiResponse<Any>> {
        log.error("通用异常: ${ex.message}")
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error(ErrorCodes.SYSTEM_001, "系统内部错误"))
    }
}

// 权限不足异常
class AccessDeniedException(message: String) : RuntimeException(message)
