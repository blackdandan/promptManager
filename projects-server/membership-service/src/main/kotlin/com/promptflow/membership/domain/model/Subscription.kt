package com.promptflow.membership.domain.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.LocalDateTime

@Document(collection = "subscriptions")
data class Subscription(
    @Id
    val id: String? = null,
    
    @Field("user_id")
    val userId: String,
    
    @Field("plan_id")
    val planId: String,
    
    @Field("plan_type")
    val planType: PlanType,
    
    @Field("status")
    val status: SubscriptionStatus,
    
    @Field("billing_cycle")
    val billingCycle: BillingCycle,
    
    @Field("amount")
    val amount: Int, // 金额（分）
    
    @Field("currency")
    val currency: String = "CNY",
    
    @Field("current_period_start")
    val currentPeriodStart: LocalDateTime,
    
    @Field("current_period_end")
    val currentPeriodEnd: LocalDateTime,
    
    @Field("cancel_at_period_end")
    val cancelAtPeriodEnd: Boolean = false,
    
    @Field("canceled_at")
    val canceledAt: LocalDateTime? = null,
    
    @Field("payment_method_id")
    val paymentMethodId: String? = null,
    
    @Field("payment_gateway")
    val paymentGateway: PaymentGateway? = null,
    
    @Field("gateway_subscription_id")
    val gatewaySubscriptionId: String? = null,
    
    @Field("trial_start")
    val trialStart: LocalDateTime? = null,
    
    @Field("trial_end")
    val trialEnd: LocalDateTime? = null,
    
    @Field("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Field("updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class SubscriptionStatus {
    ACTIVE,         // 活跃
    CANCELED,       // 已取消
    PAST_DUE,       // 逾期
    UNPAID,         // 未支付
    TRIAL           // 试用中
}

enum class BillingCycle {
    MONTHLY,        // 月付
    QUARTERLY,      // 季付
    YEARLY          // 年付
}
