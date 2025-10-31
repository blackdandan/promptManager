package com.promptflow.membership.interfaces.rest

import com.promptflow.common.dto.ApiResponse
import com.promptflow.membership.domain.exception.ErrorCode
import com.promptflow.membership.application.service.PaymentService
import com.promptflow.membership.domain.model.BillingCycle
import com.promptflow.membership.domain.model.PaymentGateway
import com.promptflow.membership.domain.model.PaymentMethod
import com.promptflow.membership.domain.model.PlanType
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/payments")
class PaymentController(
    private val paymentService: PaymentService
) {
    
    private val log = LoggerFactory.getLogger(this::class.java)
    
    @PostMapping("/order")
    fun createOrder(
        @RequestHeader("X-User-ID") userId: String,
        @RequestBody request: CreateOrderRequest
    ): ResponseEntity<ApiResponse<OrderResponse>> {
        log.info("创建订单: $userId -> ${request.planType}")
        
        try {
            val planType = PlanType.valueOf(request.planType.uppercase())
            val billingCycle = BillingCycle.valueOf(request.billingCycle.uppercase())
            val paymentMethod = request.paymentMethod?.let { PaymentMethod.valueOf(it.uppercase()) }
            
            val order = paymentService.createOrder(
                userId = userId,
                planId = request.planId,
                planType = planType,
                billingCycle = billingCycle,
                amount = request.amount,
                paymentMethod = paymentMethod
            )
            
            val response = OrderResponse(
                orderId = order.id!!,
                userId = order.userId,
                planType = order.planType.name,
                billingCycle = order.billingCycle.name,
                amount = order.amount,
                currency = order.currency,
                status = order.status.name,
                paymentStatus = order.paymentStatus.name,
                createdAt = order.createdAt,
                updatedAt = order.updatedAt
            )
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "订单创建成功"))
                
        } catch (e: IllegalArgumentException) {
            log.error("无效的订单参数: ${e.message}")
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ErrorCode.VALIDATION_ERROR.code, "无效的订单参数"))
                
        } catch (e: Exception) {
            log.error("创建订单失败: ${e.message}")
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ErrorCode.PAYMENT_001.code, e.message ?: "创建订单失败"))
        }
    }
    
    @PostMapping("/process/{orderId}")
    fun processPayment(
        @PathVariable orderId: String,
        @RequestBody request: ProcessPaymentRequest
    ): ResponseEntity<ApiResponse<OrderResponse>> {
        log.info("处理支付: $orderId via ${request.paymentGateway}")
        
        try {
            val paymentGateway = PaymentGateway.valueOf(request.paymentGateway.uppercase())
            
            val order = paymentService.processPayment(
                orderId = orderId,
                paymentGateway = paymentGateway,
                gatewayOrderId = request.gatewayOrderId,
                gatewayPaymentId = request.gatewayPaymentId
            )
            
            val response = OrderResponse(
                orderId = order.id!!,
                userId = order.userId,
                planType = order.planType.name,
                billingCycle = order.billingCycle.name,
                amount = order.amount,
                currency = order.currency,
                status = order.status.name,
                paymentStatus = order.paymentStatus.name,
                paymentGateway = order.paymentGateway?.name,
                gatewayOrderId = order.gatewayOrderId,
                gatewayPaymentId = order.gatewayPaymentId,
                createdAt = order.createdAt,
                updatedAt = order.updatedAt
            )
            
            return ResponseEntity.ok(ApiResponse.success(response, "支付处理中"))
            
        } catch (e: IllegalArgumentException) {
            log.error("无效的支付网关: ${e.message}")
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ErrorCode.VALIDATION_ERROR.code, "无效的支付网关"))
                
        } catch (e: Exception) {
            log.error("处理支付失败: ${e.message}")
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ErrorCode.PAYMENT_002.code, e.message ?: "处理支付失败"))
        }
    }
    
    @PostMapping("/complete/{orderId}")
    fun completePayment(@PathVariable orderId: String): ResponseEntity<ApiResponse<OrderResponse>> {
        log.info("完成支付: $orderId")
        
        try {
            val order = paymentService.completePayment(orderId)
            
            val response = OrderResponse(
                orderId = order.id!!,
                userId = order.userId,
                planType = order.planType.name,
                billingCycle = order.billingCycle.name,
                amount = order.amount,
                currency = order.currency,
                status = order.status.name,
                paymentStatus = order.paymentStatus.name,
                paymentGateway = order.paymentGateway?.name,
                gatewayOrderId = order.gatewayOrderId,
                gatewayPaymentId = order.gatewayPaymentId,
                paidAt = order.paidAt,
                createdAt = order.createdAt,
                updatedAt = order.updatedAt
            )
            
            return ResponseEntity.ok(ApiResponse.success(response, "支付完成"))
            
        } catch (e: Exception) {
            log.error("完成支付失败: ${e.message}")
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ErrorCode.PAYMENT_003.code, e.message ?: "完成支付失败"))
        }
    }
    
    @PostMapping("/fail/{orderId}")
    fun failPayment(
        @PathVariable orderId: String,
        @RequestBody request: FailPaymentRequest? = null
    ): ResponseEntity<ApiResponse<OrderResponse>> {
        log.info("支付失败: $orderId - ${request?.reason}")
        
        try {
            val order = paymentService.failPayment(orderId, request?.reason)
            
            val response = OrderResponse(
                orderId = order.id!!,
                userId = order.userId,
                planType = order.planType.name,
                billingCycle = order.billingCycle.name,
                amount = order.amount,
                currency = order.currency,
                status = order.status.name,
                paymentStatus = order.paymentStatus.name,
                createdAt = order.createdAt,
                updatedAt = order.updatedAt
            )
            
            return ResponseEntity.ok(ApiResponse.success(response, "支付失败处理完成"))
            
        } catch (e: Exception) {
            log.error("支付失败处理失败: ${e.message}")
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ErrorCode.PAYMENT_004.code, e.message ?: "支付失败处理失败"))
        }
    }
    
    @PostMapping("/subscription/cancel/{subscriptionId}")
    fun cancelSubscription(@PathVariable subscriptionId: String): ResponseEntity<ApiResponse<SubscriptionResponse>> {
        log.info("取消订阅: $subscriptionId")
        
        try {
            val subscription = paymentService.cancelSubscription(subscriptionId)
            
            val response = SubscriptionResponse(
                subscriptionId = subscription.id!!,
                userId = subscription.userId,
                planType = subscription.planType.name,
                billingCycle = subscription.billingCycle.name,
                amount = subscription.amount,
                currency = subscription.currency,
                status = subscription.status.name,
                currentPeriodStart = subscription.currentPeriodStart,
                currentPeriodEnd = subscription.currentPeriodEnd,
                cancelAtPeriodEnd = subscription.cancelAtPeriodEnd,
                canceledAt = subscription.canceledAt,
                createdAt = subscription.createdAt,
                updatedAt = subscription.updatedAt
            )
            
            return ResponseEntity.ok(ApiResponse.success(response, "订阅取消成功"))
            
        } catch (e: Exception) {
            log.error("取消订阅失败: ${e.message}")
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ErrorCode.SUBSCRIPTION_001.code, e.message ?: "取消订阅失败"))
        }
    }
    
    @PostMapping("/subscription/renew/{subscriptionId}")
    fun renewSubscription(@PathVariable subscriptionId: String): ResponseEntity<ApiResponse<SubscriptionResponse>> {
        log.info("续订订阅: $subscriptionId")
        
        try {
            val subscription = paymentService.renewSubscription(subscriptionId)
            
            val response = SubscriptionResponse(
                subscriptionId = subscription.id!!,
                userId = subscription.userId,
                planType = subscription.planType.name,
                billingCycle = subscription.billingCycle.name,
                amount = subscription.amount,
                currency = subscription.currency,
                status = subscription.status.name,
                currentPeriodStart = subscription.currentPeriodStart,
                currentPeriodEnd = subscription.currentPeriodEnd,
                cancelAtPeriodEnd = subscription.cancelAtPeriodEnd,
                createdAt = subscription.createdAt,
                updatedAt = subscription.updatedAt
            )
            
            return ResponseEntity.ok(ApiResponse.success(response, "订阅续订成功"))
            
        } catch (e: Exception) {
            log.error("续订订阅失败: ${e.message}")
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ErrorCode.SUBSCRIPTION_002.code, e.message ?: "续订订阅失败"))
        }
    }
    
    @GetMapping("/orders")
    fun getUserOrders(@RequestHeader("X-User-ID") userId: String): ResponseEntity<ApiResponse<List<OrderResponse>>> {
        log.info("获取用户订单: $userId")
        
        try {
            val orders = paymentService.getUserOrders(userId)
            
            val response = orders.map { order ->
                OrderResponse(
                    orderId = order.id!!,
                    userId = order.userId,
                    planType = order.planType.name,
                    billingCycle = order.billingCycle.name,
                    amount = order.amount,
                    currency = order.currency,
                    status = order.status.name,
                    paymentStatus = order.paymentStatus.name,
                    paymentGateway = order.paymentGateway?.name,
                    gatewayOrderId = order.gatewayOrderId,
                    gatewayPaymentId = order.gatewayPaymentId,
                    paidAt = order.paidAt,
                    createdAt = order.createdAt,
                    updatedAt = order.updatedAt
                )
            }
            
            return ResponseEntity.ok(ApiResponse.success(response, "获取订单列表成功"))
            
        } catch (e: Exception) {
            log.error("获取订单列表失败: ${e.message}")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCode.DATABASE_ERROR.code, "获取订单列表失败"))
        }
    }
    
    @GetMapping("/subscriptions")
    fun getUserSubscriptions(@RequestHeader("X-User-ID") userId: String): ResponseEntity<ApiResponse<List<SubscriptionResponse>>> {
        log.info("获取用户订阅: $userId")
        
        try {
            val subscriptions = paymentService.getUserSubscriptions(userId)
            
            val response = subscriptions.map { subscription ->
                SubscriptionResponse(
                    subscriptionId = subscription.id!!,
                    userId = subscription.userId,
                    planType = subscription.planType.name,
                    billingCycle = subscription.billingCycle.name,
                    amount = subscription.amount,
                    currency = subscription.currency,
                    status = subscription.status.name,
                    currentPeriodStart = subscription.currentPeriodStart,
                    currentPeriodEnd = subscription.currentPeriodEnd,
                    cancelAtPeriodEnd = subscription.cancelAtPeriodEnd,
                    canceledAt = subscription.canceledAt,
                    paymentGateway = subscription.paymentGateway?.name,
                    gatewaySubscriptionId = subscription.gatewaySubscriptionId,
                    createdAt = subscription.createdAt,
                    updatedAt = subscription.updatedAt
                )
            }
            
            return ResponseEntity.ok(ApiResponse.success(response, "获取订阅列表成功"))
            
        } catch (e: Exception) {
            log.error("获取订阅列表失败: ${e.message}")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCode.DATABASE_ERROR.code, "获取订阅列表失败"))
        }
    }
    
    @GetMapping("/subscription/active")
    fun getActiveSubscription(@RequestHeader("X-User-ID") userId: String): ResponseEntity<ApiResponse<SubscriptionResponse?>> {
        log.info("获取活跃订阅: $userId")
        
        try {
            val subscription = paymentService.getActiveSubscription(userId)
            
            val response = subscription?.let { sub ->
                SubscriptionResponse(
                    subscriptionId = sub.id!!,
                    userId = sub.userId,
                    planType = sub.planType.name,
                    billingCycle = sub.billingCycle.name,
                    amount = sub.amount,
                    currency = sub.currency,
                    status = sub.status.name,
                    currentPeriodStart = sub.currentPeriodStart,
                    currentPeriodEnd = sub.currentPeriodEnd,
                    cancelAtPeriodEnd = sub.cancelAtPeriodEnd,
                    paymentGateway = sub.paymentGateway?.name,
                    gatewaySubscriptionId = sub.gatewaySubscriptionId,
                    createdAt = sub.createdAt,
                    updatedAt = sub.updatedAt
                )
            }
            
            return ResponseEntity.ok(ApiResponse.success(response, "获取活跃订阅成功"))
            
        } catch (e: Exception) {
            log.error("获取活跃订阅失败: ${e.message}")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCode.DATABASE_ERROR.code, "获取活跃订阅失败"))
        }
    }
}

data class CreateOrderRequest(
    val planId: String,
    val planType: String,
    val billingCycle: String,
    val amount: Int,
    val paymentMethod: String? = null
)

data class ProcessPaymentRequest(
    val paymentGateway: String,
    val gatewayOrderId: String,
    val gatewayPaymentId: String
)

data class FailPaymentRequest(
    val reason: String? = null
)

data class OrderResponse(
    val orderId: String,
    val userId: String,
    val planType: String,
    val billingCycle: String,
    val amount: Int,
    val currency: String,
    val status: String,
    val paymentStatus: String,
    val paymentGateway: String? = null,
    val gatewayOrderId: String? = null,
    val gatewayPaymentId: String? = null,
    val paidAt: java.time.LocalDateTime? = null,
    val createdAt: java.time.LocalDateTime,
    val updatedAt: java.time.LocalDateTime
)

data class SubscriptionResponse(
    val subscriptionId: String,
    val userId: String,
    val planType: String,
    val billingCycle: String,
    val amount: Int,
    val currency: String,
    val status: String,
    val currentPeriodStart: java.time.LocalDateTime,
    val currentPeriodEnd: java.time.LocalDateTime,
    val cancelAtPeriodEnd: Boolean = false,
    val canceledAt: java.time.LocalDateTime? = null,
    val paymentGateway: String? = null,
    val gatewaySubscriptionId: String? = null,
    val createdAt: java.time.LocalDateTime,
    val updatedAt: java.time.LocalDateTime
)
