package com.promptflow.membership.interfaces.rest

import com.promptflow.common.dto.ApiResponse
import com.promptflow.membership.domain.exception.ErrorCode
import com.promptflow.membership.application.service.PaymentGatewayService
import com.promptflow.membership.domain.model.PaymentGateway
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/payment/gateway")
class PaymentGatewayController(
    private val paymentGatewayService: PaymentGatewayService
) {
    
    private val log = LoggerFactory.getLogger(this::class.java)
    
    /**
     * 获取可用的支付网关列表
     */
    @GetMapping("/available")
    fun getAvailableGateways(): ResponseEntity<ApiResponse<List<GatewayResponse>>> {
        log.info("获取可用的支付网关列表")
        
        try {
            val availableGateways = paymentGatewayService.getAvailableGateways()
            
            val response = availableGateways.map { gateway ->
                GatewayResponse(
                    gateway = gateway.name,
                    displayName = getGatewayDisplayName(gateway),
                    enabled = true
                )
            }
            
            return ResponseEntity.ok(ApiResponse.success(response, "获取支付网关列表成功"))
            
        } catch (e: Exception) {
            log.error("获取支付网关列表失败: ${e.message}")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCode.DATABASE_ERROR.code, "获取支付网关列表失败"))
        }
    }
    
    /**
     * 创建支付订单
     */
    @PostMapping("/create")
    fun createPayment(
        @RequestHeader("X-User-ID") userId: String,
        @RequestBody request: CreatePaymentRequest
    ): ResponseEntity<ApiResponse<PaymentResultResponse>> {
        log.info("创建支付订单: $userId -> ${request.gateway}")
        
        try {
            val gateway = PaymentGateway.valueOf(request.gateway.uppercase())
            
            val paymentResult = paymentGatewayService.createPayment(
                gateway = gateway,
                orderId = request.orderId,
                amount = request.amount,
                currency = request.currency,
                description = request.description,
                returnUrl = request.returnUrl,
                metadata = request.metadata ?: emptyMap()
            )
            
            if (!paymentResult.success) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(ErrorCode.PAYMENT_001.code, "创建支付订单失败"))
            }
            
            val response = PaymentResultResponse(
                gateway = paymentResult.gateway.name,
                orderId = paymentResult.orderId,
                paymentData = paymentResult.paymentData,
                redirectUrl = paymentResult.redirectUrl
            )
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "创建支付订单成功"))
                
        } catch (e: IllegalArgumentException) {
            log.error("无效的支付网关: ${e.message}")
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ErrorCode.VALIDATION_ERROR.code, "无效的支付网关"))
                
        } catch (e: Exception) {
            log.error("创建支付订单失败: ${e.message}")
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ErrorCode.PAYMENT_001.code, e.message ?: "创建支付订单失败"))
        }
    }
    
    /**
     * 验证支付结果
     */
    @PostMapping("/verify")
    fun verifyPayment(
        @RequestBody request: VerifyPaymentRequest
    ): ResponseEntity<ApiResponse<PaymentVerificationResponse>> {
        log.info("验证支付结果: ${request.gateway}")
        
        try {
            val gateway = PaymentGateway.valueOf(request.gateway.uppercase())
            
            val verificationResult = paymentGatewayService.verifyPayment(
                gateway = gateway,
                paymentData = request.paymentData
            )
            
            if (!verificationResult.success) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(ErrorCode.PAYMENT_002.code, "验证支付结果失败"))
            }
            
            val response = PaymentVerificationResponse(
                verified = verificationResult.verified,
                orderId = verificationResult.orderId,
                amount = verificationResult.amount,
                currency = verificationResult.currency,
                gatewayOrderId = verificationResult.gatewayOrderId
            )
            
            return ResponseEntity.ok(ApiResponse.success(response, "验证支付结果成功"))
            
        } catch (e: IllegalArgumentException) {
            log.error("无效的支付网关: ${e.message}")
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ErrorCode.VALIDATION_ERROR.code, "无效的支付网关"))
                
        } catch (e: Exception) {
            log.error("验证支付结果失败: ${e.message}")
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ErrorCode.PAYMENT_002.code, e.message ?: "验证支付结果失败"))
        }
    }
    
    /**
     * 处理支付回调
     */
    @PostMapping("/callback/{gateway}")
    fun handlePaymentCallback(
        @PathVariable gateway: String,
        @RequestBody callbackData: Map<String, Any>
    ): ResponseEntity<ApiResponse<PaymentCallbackResponse>> {
        log.info("处理支付回调: $gateway")
        
        try {
            val paymentGateway = PaymentGateway.valueOf(gateway.uppercase())
            
            val callbackResult = paymentGatewayService.handlePaymentCallback(
                gateway = paymentGateway,
                callbackData = callbackData
            )
            
            if (!callbackResult.success) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(ErrorCode.PAYMENT_003.code, "处理支付回调失败"))
            }
            
            val response = PaymentCallbackResponse(
                orderId = callbackResult.orderId,
                gatewayOrderId = callbackResult.gatewayOrderId,
                amount = callbackResult.amount,
                status = callbackResult.status.name
            )
            
            return ResponseEntity.ok(ApiResponse.success(response, "处理支付回调成功"))
            
        } catch (e: IllegalArgumentException) {
            log.error("无效的支付网关: ${e.message}")
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ErrorCode.VALIDATION_ERROR.code, "无效的支付网关"))
                
        } catch (e: Exception) {
            log.error("处理支付回调失败: ${e.message}")
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ErrorCode.PAYMENT_003.code, e.message ?: "处理支付回调失败"))
        }
    }
    
    /**
     * 检查支付网关状态
     */
    @GetMapping("/status/{gateway}")
    fun checkGatewayStatus(@PathVariable gateway: String): ResponseEntity<ApiResponse<GatewayStatusResponse>> {
        log.info("检查支付网关状态: $gateway")
        
        try {
            val paymentGateway = PaymentGateway.valueOf(gateway.uppercase())
            
            val isAvailable = paymentGatewayService.isGatewayAvailable(paymentGateway)
            
            val response = GatewayStatusResponse(
                gateway = paymentGateway.name,
                displayName = getGatewayDisplayName(paymentGateway),
                enabled = isAvailable,
                status = if (isAvailable) "AVAILABLE" else "DISABLED"
            )
            
            return ResponseEntity.ok(ApiResponse.success(response, "检查支付网关状态成功"))
            
        } catch (e: IllegalArgumentException) {
            log.error("无效的支付网关: ${e.message}")
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ErrorCode.VALIDATION_ERROR.code, "无效的支付网关"))
                
        } catch (e: Exception) {
            log.error("检查支付网关状态失败: ${e.message}")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCode.DATABASE_ERROR.code, "检查支付网关状态失败"))
        }
    }
    
    /**
     * 获取支付网关显示名称
     */
    private fun getGatewayDisplayName(gateway: PaymentGateway): String {
        return when (gateway) {
            PaymentGateway.WECHAT_PAY -> "微信支付"
            PaymentGateway.ALIPAY -> "支付宝"
            PaymentGateway.GOOGLE_PAY -> "Google Pay"
            PaymentGateway.APPLE_PAY -> "Apple Pay"
            PaymentGateway.STRIPE -> "Stripe"
        }
    }
}

/**
 * 创建支付请求
 */
data class CreatePaymentRequest(
    val gateway: String,
    val orderId: String,
    val amount: Int,
    val currency: String,
    val description: String,
    val returnUrl: String? = null,
    val metadata: Map<String, String>? = null
)

/**
 * 验证支付请求
 */
data class VerifyPaymentRequest(
    val gateway: String,
    val paymentData: Map<String, Any>
)

/**
 * 网关响应
 */
data class GatewayResponse(
    val gateway: String,
    val displayName: String,
    val enabled: Boolean
)

/**
 * 支付结果响应
 */
data class PaymentResultResponse(
    val gateway: String,
    val orderId: String,
    val paymentData: Map<String, Any>,
    val redirectUrl: String?
)

/**
 * 支付验证响应
 */
data class PaymentVerificationResponse(
    val verified: Boolean,
    val orderId: String,
    val amount: Int,
    val currency: String,
    val gatewayOrderId: String
)

/**
 * 支付回调响应
 */
data class PaymentCallbackResponse(
    val orderId: String,
    val gatewayOrderId: String,
    val amount: Int,
    val status: String
)

/**
 * 网关状态响应
 */
data class GatewayStatusResponse(
    val gateway: String,
    val displayName: String,
    val enabled: Boolean,
    val status: String
)
