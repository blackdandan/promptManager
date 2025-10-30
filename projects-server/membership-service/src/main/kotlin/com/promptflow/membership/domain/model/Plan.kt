package com.promptflow.membership.domain.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

@Document(collection = "plans")
data class Plan(
    @Id
    val id: String? = null,
    
    @Field("name")
    val name: String,
    
    @Field("description")
    val description: String,
    
    @Field("plan_type")
    val planType: PlanType,
    
    @Field("billing_cycles")
    val billingCycles: List<BillingCycleOption>,
    
    @Field("features")
    val features: Map<String, Any>,
    
    @Field("usage_limits")
    val usageLimits: Map<String, Int>,
    
    @Field("is_active")
    val isActive: Boolean = true,
    
    @Field("is_default")
    val isDefault: Boolean = false,
    
    @Field("sort_order")
    val sortOrder: Int = 0
)

data class BillingCycleOption(
    @Field("billing_cycle")
    val billingCycle: BillingCycle,
    
    @Field("amount")
    val amount: Int, // 金额（分）
    
    @Field("currency")
    val currency: String = "CNY",
    
    @Field("trial_days")
    val trialDays: Int = 0,
    
    @Field("is_recommended")
    val isRecommended: Boolean = false
)
