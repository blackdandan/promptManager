package com.promptflow.membership.interfaces.rest

import com.promptflow.common.dto.ApiResponse
import com.promptflow.membership.domain.exception.ErrorCode
import com.promptflow.membership.application.service.MembershipService
import com.promptflow.membership.domain.model.BillingCycle
import com.promptflow.membership.domain.model.PlanType
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/membership")
class MembershipController(
    private val membershipService: MembershipService
) {
    
    private val log = LoggerFactory.getLogger(this::class.java)
    
    @PostMapping("/free")
    fun createFreeMembership(@RequestHeader("X-User-ID") userId: String): ResponseEntity<ApiResponse<MembershipResponse>> {
        log.info("创建免费会员: $userId")
        
        try {
            val membership = membershipService.createFreeMembership(userId)
            val response = MembershipResponse(
                membershipId = membership.id!!,
                userId = membership.userId,
                planType = membership.planType.name,
                status = membership.status.name,
                features = membership.features,
                usageLimits = membership.usageLimits,
                currentUsage = membership.currentUsage,
                currentPeriodStart = membership.currentPeriodStart,
                currentPeriodEnd = membership.currentPeriodEnd
            )
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "免费会员创建成功"))
                
        } catch (e: Exception) {
            log.error("创建免费会员失败: ${e.message}")
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ErrorCode.MEMBERSHIP_001.code, e.message ?: "创建免费会员失败"))
        }
    }
    
    @GetMapping
    fun getMembership(@RequestHeader("X-User-ID") userId: String): ResponseEntity<ApiResponse<MembershipResponse>> {
        log.info("获取用户会员信息: $userId")
        
        try {
            val membership = membershipService.getUserMembership(userId)
            val response = MembershipResponse(
                membershipId = membership.id!!,
                userId = membership.userId,
                planType = membership.planType.name,
                status = membership.status.name,
                features = membership.features,
                usageLimits = membership.usageLimits,
                currentUsage = membership.currentUsage,
                currentPeriodStart = membership.currentPeriodStart,
                currentPeriodEnd = membership.currentPeriodEnd
            )
            
            return ResponseEntity.ok(ApiResponse.success(response, "获取会员信息成功"))
            
        } catch (e: Exception) {
            log.error("获取会员信息失败: ${e.message}")
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ErrorCode.MEMBERSHIP_001.code, e.message ?: "获取会员信息失败"))
        }
    }
    
    @PostMapping("/upgrade")
    fun upgradeMembership(
        @RequestHeader("X-User-ID") userId: String,
        @RequestBody request: UpgradeMembershipRequest
    ): ResponseEntity<ApiResponse<MembershipResponse>> {
        log.info("升级会员: $userId -> ${request.planType}")
        
        try {
            val planType = PlanType.valueOf(request.planType.uppercase())
            val billingCycle = BillingCycle.valueOf(request.billingCycle.uppercase())
            
            val membership = membershipService.upgradeMembership(
                userId = userId,
                planId = request.planId,
                planType = planType,
                billingCycle = billingCycle,
                amount = request.amount
            )
            
            val response = MembershipResponse(
                membershipId = membership.id!!,
                userId = membership.userId,
                planType = membership.planType.name,
                status = membership.status.name,
                features = membership.features,
                usageLimits = membership.usageLimits,
                currentUsage = membership.currentUsage,
                currentPeriodStart = membership.currentPeriodStart,
                currentPeriodEnd = membership.currentPeriodEnd
            )
            
            return ResponseEntity.ok(ApiResponse.success(response, "会员升级成功"))
            
        } catch (e: IllegalArgumentException) {
            log.error("无效的会员类型或计费周期: ${e.message}")
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ErrorCode.VALIDATION_ERROR.code, "无效的会员类型或计费周期"))
                
        } catch (e: Exception) {
            log.error("会员升级失败: ${e.message}")
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ErrorCode.MEMBERSHIP_003.code, e.message ?: "会员升级失败"))
        }
    }
    
    @PostMapping("/cancel")
    fun cancelMembership(@RequestHeader("X-User-ID") userId: String): ResponseEntity<ApiResponse<MembershipResponse>> {
        log.info("取消会员: $userId")
        
        try {
            val membership = membershipService.cancelMembership(userId)
            val response = MembershipResponse(
                membershipId = membership.id!!,
                userId = membership.userId,
                planType = membership.planType.name,
                status = membership.status.name,
                features = membership.features,
                usageLimits = membership.usageLimits,
                currentUsage = membership.currentUsage,
                currentPeriodStart = membership.currentPeriodStart,
                currentPeriodEnd = membership.currentPeriodEnd
            )
            
            return ResponseEntity.ok(ApiResponse.success(response, "会员取消成功"))
            
        } catch (e: Exception) {
            log.error("取消会员失败: ${e.message}")
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ErrorCode.MEMBERSHIP_004.code, e.message ?: "取消会员失败"))
        }
    }
    
    @PostMapping("/usage")
    fun updateUsage(
        @RequestHeader("X-User-ID") userId: String,
        @RequestBody request: UpdateUsageRequest
    ): ResponseEntity<ApiResponse<MembershipResponse>> {
        log.info("更新使用量: $userId -> ${request.feature}: ${request.usage}")
        
        try {
            val membership = membershipService.updateUsage(userId, request.feature, request.usage)
            val response = MembershipResponse(
                membershipId = membership.id!!,
                userId = membership.userId,
                planType = membership.planType.name,
                status = membership.status.name,
                features = membership.features,
                usageLimits = membership.usageLimits,
                currentUsage = membership.currentUsage,
                currentPeriodStart = membership.currentPeriodStart,
                currentPeriodEnd = membership.currentPeriodEnd
            )
            
            return ResponseEntity.ok(ApiResponse.success(response, "使用量更新成功"))
            
        } catch (e: Exception) {
            log.error("更新使用量失败: ${e.message}")
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ErrorCode.MEMBERSHIP_005.code, e.message ?: "更新使用量失败"))
        }
    }
    
    @GetMapping("/check-access/{feature}")
    fun checkFeatureAccess(
        @RequestHeader("X-User-ID") userId: String,
        @PathVariable feature: String
    ): ResponseEntity<ApiResponse<FeatureAccessResponse>> {
        log.info("检查功能访问权限: $userId -> $feature")
        
        try {
            val hasAccess = membershipService.checkFeatureAccess(userId, feature)
            val response = FeatureAccessResponse(
                feature = feature,
                hasAccess = hasAccess
            )
            
            return ResponseEntity.ok(ApiResponse.success(response, "功能访问权限检查成功"))
            
        } catch (e: Exception) {
            log.error("检查功能访问权限失败: ${e.message}")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCode.DATABASE_ERROR.code, "检查功能访问权限失败"))
        }
    }
    
    @GetMapping("/stats")
    fun getUsageStats(@RequestHeader("X-User-ID") userId: String): ResponseEntity<ApiResponse<Map<String, Any>>> {
        log.info("获取使用统计: $userId")
        
        try {
            val stats = membershipService.getUsageStats(userId)
            return ResponseEntity.ok(ApiResponse.success(stats, "获取使用统计成功"))
            
        } catch (e: Exception) {
            log.error("获取使用统计失败: ${e.message}")
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ErrorCode.MEMBERSHIP_001.code, e.message ?: "获取使用统计失败"))
        }
    }
}

data class UpgradeMembershipRequest(
    val planId: String,
    val planType: String,
    val billingCycle: String,
    val amount: Int
)

data class UpdateUsageRequest(
    val feature: String,
    val usage: Int
)

data class MembershipResponse(
    val membershipId: String,
    val userId: String,
    val planType: String,
    val status: String,
    val features: Map<String, Any>,
    val usageLimits: Map<String, Int>,
    val currentUsage: Map<String, Int>,
    val currentPeriodStart: java.time.LocalDateTime,
    val currentPeriodEnd: java.time.LocalDateTime
)

data class FeatureAccessResponse(
    val feature: String,
    val hasAccess: Boolean
)
