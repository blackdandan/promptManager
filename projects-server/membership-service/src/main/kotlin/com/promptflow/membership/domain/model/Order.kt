package com.promptflow.membership.domain.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.LocalDateTime

@Document(collection = "orders")
data class Order(
    @Id
    val id: String? = null,
    
    @Field("user_id")
    val userId: String,
    
    @Field("plan_id")
    val planId: String,
    
    @Field("plan_type")
    val planType: PlanType,
    
    @Field("billing_cycle")
    val billingCycle: BillingCycle,
    
    @Field("amount")
    val amount: Int, // 金额（分）
    
    @Field("currency")
    val currency: String = "CNY",
    
    @Field("status")
    val status: OrderStatus,
    
    @Field("payment_status")
    val paymentStatus: PaymentStatus,
    
    @Field("payment_method")
    val paymentMethod: PaymentMethod? = null,
    
    @Field("payment_gateway")
    val paymentGateway: PaymentGateway? = null,
    
    @Field("gateway_order_id")
    val gatewayOrderId: String? = null,
    
    @Field("gateway_payment_id")
    val gatewayPaymentId: String? = null,
    
    @Field("paid_at")
    val paidAt: LocalDateTime? = null,
    
    @Field("refunded_at")
    val refundedAt: LocalDateTime? = null,
    
    @Field("refund_amount")
    val refundAmount: Int = 0,
    
    @Field("customer_notes")
    val customerNotes: String? = null,
    
    @Field("admin_notes")
    val adminNotes: String? = null,
    
    @Field("metadata")
    val metadata: Map<String, Any> = emptyMap(),
    
    @Field("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Field("updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class OrderStatus {
    PENDING,        // 待处理
    CONFIRMED,      // 已确认
    COMPLETED,      // 已完成
    CANCELLED,      // 已取消
    REFUNDED        // 已退款
}

enum class PaymentStatus {
    PENDING,        // 待支付
    PROCESSING,     // 处理中
    SUCCEEDED,      // 支付成功
    FAILED,         // 支付失败
    REFUNDED        // 已退款
}

enum class PaymentMethod {
    CREDIT_CARD,    // 信用卡
    DEBIT_CARD,     // 借记卡
    ALIPAY,         // 支付宝
    WECHAT_PAY,     // 微信支付
    BANK_TRANSFER   // 银行转账
}
