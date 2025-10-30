package com.promptflow.membership.application.service

import com.promptflow.membership.domain.model.*
import com.promptflow.membership.infrastructure.repository.MembershipRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class MembershipService(
    private val membershipRepository: MembershipRepository
) {
    
    private val log = LoggerFactory.getLogger(this::class.java)
    
    fun createFreeMembership(userId: String): Membership {
        log.info("为用户创建免费会员: $userId")
        
        val existingMembership = membershipRepository.findByUserId(userId)
        if (existingMembership.isPresent) {
            throw MembershipAlreadyExistsException("用户已存在会员信息")
        }
        
        val now = LocalDateTime.now()
        val membership = Membership(
            userId = userId,
            planId = "free_plan",
            planType = PlanType.FREE,
            status = MembershipStatus.ACTIVE,
            currentPeriodStart = now,
            currentPeriodEnd = now.plusYears(100), // 免费会员长期有效
            features = getFreePlanFeatures(),
            usageLimits = getFreePlanUsageLimits(),
            currentUsage = emptyMap()
        )
        
        return membershipRepository.save(membership).also {
            log.info("免费会员创建成功: $userId")
        }
    }
    
    fun getUserMembership(userId: String): Membership {
        return membershipRepository.findByUserId(userId)
            .orElseThrow { MembershipNotFoundException("用户会员信息不存在: $userId") }
    }
    
    fun getActiveUserMembership(userId: String): Membership? {
        val activeStatuses = listOf(MembershipStatus.ACTIVE, MembershipStatus.TRIAL)
        return membershipRepository.findByUserIdAndStatusIn(userId, activeStatuses).orElse(null)
    }
    
    fun upgradeMembership(
        userId: String,
        planId: String,
        planType: PlanType,
        billingCycle: BillingCycle,
        amount: Int
    ): Membership {
        log.info("升级用户会员: $userId -> $planType")
        
        val existingMembership = membershipRepository.findByUserId(userId)
            .orElseThrow { MembershipNotFoundException("用户会员信息不存在: $userId") }
        
        // 检查是否可以升级
        if (existingMembership.planType == planType) {
            throw MembershipUpgradeNotAllowedException("已经是相同等级的会员")
        }
        
        if (existingMembership.planType.ordinal > planType.ordinal) {
            throw MembershipUpgradeNotAllowedException("无法降级会员等级")
        }
        
        val now = LocalDateTime.now()
        val newMembership = existingMembership.copy(
            planId = planId,
            planType = planType,
            status = MembershipStatus.ACTIVE,
            currentPeriodStart = now,
            currentPeriodEnd = calculatePeriodEnd(now, billingCycle),
            features = getPlanFeatures(planType),
            usageLimits = getPlanUsageLimits(planType),
            currentUsage = emptyMap(), // 重置使用量
            updatedAt = now
        )
        
        return membershipRepository.save(newMembership).also {
            log.info("会员升级成功: $userId -> $planType")
        }
    }
    
    fun cancelMembership(userId: String): Membership {
        log.info("取消用户会员: $userId")
        
        val membership = membershipRepository.findByUserId(userId)
            .orElseThrow { MembershipNotFoundException("用户会员信息不存在: $userId") }
        
        if (membership.status != MembershipStatus.ACTIVE) {
            throw MembershipOperationNotAllowedException("会员状态不允许取消")
        }
        
        val updatedMembership = membership.copy(
            status = MembershipStatus.CANCELED,
            cancelAtPeriodEnd = true,
            updatedAt = LocalDateTime.now()
        )
        
        return membershipRepository.save(updatedMembership).also {
            log.info("会员取消成功: $userId")
        }
    }
    
    fun updateUsage(userId: String, feature: String, usage: Int): Membership {
        log.info("更新用户使用量: $userId -> $feature: $usage")
        
        val membership = membershipRepository.findByUserId(userId)
            .orElseThrow { MembershipNotFoundException("用户会员信息不存在: $userId") }
        
        val currentUsage = membership.currentUsage.toMutableMap()
        currentUsage[feature] = usage
        
        val updatedMembership = membership.copy(
            currentUsage = currentUsage,
            updatedAt = LocalDateTime.now()
        )
        
        return membershipRepository.save(updatedMembership).also {
            log.info("使用量更新成功: $userId -> $feature: $usage")
        }
    }
    
    fun checkFeatureAccess(userId: String, feature: String): Boolean {
        val membership = getActiveUserMembership(userId) ?: return false
        
        val featureEnabled = membership.features[feature] as? Boolean ?: false
        if (!featureEnabled) {
            return false
        }
        
        // 检查使用量限制
        val usageLimit = membership.usageLimits[feature] ?: return true
        val currentUsage = membership.currentUsage[feature] ?: 0
        
        return currentUsage < usageLimit
    }
    
    fun getUsageStats(userId: String): Map<String, Any> {
        val membership = membershipRepository.findByUserId(userId)
            .orElseThrow { MembershipNotFoundException("用户会员信息不存在: $userId") }
        
        return mapOf(
            "planType" to membership.planType.name,
            "status" to membership.status.name,
            "features" to membership.features,
            "usageLimits" to membership.usageLimits,
            "currentUsage" to membership.currentUsage,
            "currentPeriodStart" to membership.currentPeriodStart,
            "currentPeriodEnd" to membership.currentPeriodEnd
        )
    }
    
    private fun calculatePeriodEnd(startDate: LocalDateTime, billingCycle: BillingCycle): LocalDateTime {
        return when (billingCycle) {
            BillingCycle.MONTHLY -> startDate.plusMonths(1)
            BillingCycle.QUARTERLY -> startDate.plusMonths(3)
            BillingCycle.YEARLY -> startDate.plusYears(1)
        }
    }
    
    private fun getFreePlanFeatures(): Map<String, Any> {
        return mapOf(
            "prompt_creation" to true,
            "prompt_search" to true,
            "basic_templates" to true,
            "export_prompts" to false,
            "advanced_templates" to false,
            "priority_support" to false
        )
    }
    
    private fun getPlanFeatures(planType: PlanType): Map<String, Any> {
        return when (planType) {
            PlanType.FREE -> getFreePlanFeatures()
            PlanType.BASIC -> mapOf(
                "prompt_creation" to true,
                "prompt_search" to true,
                "basic_templates" to true,
                "export_prompts" to true,
                "advanced_templates" to false,
                "priority_support" to false
            )
            PlanType.PREMIUM -> mapOf(
                "prompt_creation" to true,
                "prompt_search" to true,
                "basic_templates" to true,
                "export_prompts" to true,
                "advanced_templates" to true,
                "priority_support" to true
            )
            PlanType.ENTERPRISE -> mapOf(
                "prompt_creation" to true,
                "prompt_search" to true,
                "basic_templates" to true,
                "export_prompts" to true,
                "advanced_templates" to true,
                "priority_support" to true,
                "custom_templates" to true,
                "api_access" to true
            )
        }
    }
    
    private fun getFreePlanUsageLimits(): Map<String, Int> {
        return mapOf(
            "prompts_per_month" to 50,
            "templates_access" to 10,
            "export_count" to 0
        )
    }
    
    private fun getPlanUsageLimits(planType: PlanType): Map<String, Int> {
        return when (planType) {
            PlanType.FREE -> getFreePlanUsageLimits()
            PlanType.BASIC -> mapOf(
                "prompts_per_month" to 500,
                "templates_access" to 50,
                "export_count" to 100
            )
            PlanType.PREMIUM -> mapOf(
                "prompts_per_month" to 5000,
                "templates_access" to 200,
                "export_count" to 1000
            )
            PlanType.ENTERPRISE -> mapOf(
                "prompts_per_month" to -1, // 无限制
                "templates_access" to -1, // 无限制
                "export_count" to -1 // 无限制
            )
        }
    }
}

class MembershipNotFoundException(message: String) : RuntimeException(message)
class MembershipAlreadyExistsException(message: String) : RuntimeException(message)
class MembershipUpgradeNotAllowedException(message: String) : RuntimeException(message)
class MembershipOperationNotAllowedException(message: String) : RuntimeException(message)
