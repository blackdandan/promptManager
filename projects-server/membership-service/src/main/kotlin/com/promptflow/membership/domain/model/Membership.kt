package com.promptflow.membership.domain.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.LocalDateTime

@Document(collection = "memberships")
data class Membership(
    @Id
    val id: String? = null,
    
    @Field("user_id")
    val userId: String,
    
    @Field("plan_id")
    val planId: String,
    
    @Field("plan_type")
    val planType: PlanType,
    
    @Field("status")
    val status: MembershipStatus,
    
    @Field("current_period_start")
    val currentPeriodStart: LocalDateTime,
    
    @Field("current_period_end")
    val currentPeriodEnd: LocalDateTime,
    
    @Field("cancel_at_period_end")
    val cancelAtPeriodEnd: Boolean = false,
    
    @Field("features")
    val features: Map<String, Any> = emptyMap(),
    
    @Field("usage_limits")
    val usageLimits: Map<String, Int> = emptyMap(),
    
    @Field("current_usage")
    val currentUsage: Map<String, Int> = emptyMap(),
    
    @Field("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Field("updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class PlanType {
    FREE,           // 免费版
    BASIC,          // 基础版
    PREMIUM,        // 高级版
    ENTERPRISE      // 企业版
}

enum class MembershipStatus {
    ACTIVE,         // 活跃
    CANCELED,       // 已取消
    EXPIRED,        // 已过期
    TRIAL           // 试用中
}
