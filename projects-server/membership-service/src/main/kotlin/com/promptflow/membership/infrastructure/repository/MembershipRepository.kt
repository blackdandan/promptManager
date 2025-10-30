package com.promptflow.membership.infrastructure.repository

import com.promptflow.membership.domain.model.Membership
import com.promptflow.membership.domain.model.MembershipStatus
import com.promptflow.membership.domain.model.PlanType
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.Optional

@Repository
interface MembershipRepository : MongoRepository<Membership, String> {
    
    fun findByUserId(userId: String): Optional<Membership>
    
    fun findByUserIdAndStatus(userId: String, status: MembershipStatus): Optional<Membership>
    
    @Query("{ 'user_id': ?0, 'status': { \$in: ?1 } }")
    fun findByUserIdAndStatusIn(userId: String, status: List<MembershipStatus>): Optional<Membership>
    
    @Query("{ 'status': ?0 }")
    fun findByStatus(status: MembershipStatus): List<Membership>
    
    @Query("{ 'plan_type': ?0 }")
    fun findByPlanType(planType: PlanType): List<Membership>
    
    @Query("{ 'current_period_end': { \$lt: ?0 } }")
    fun findByCurrentPeriodEndBefore(currentPeriodEnd: LocalDateTime): List<Membership>
    
    @Query("{ 'user_id': ?0 }")
    fun deleteByUserId(userId: String)
    
    fun existsByUserId(userId: String): Boolean
    
    @Query("{ 'user_id': ?0, 'status': ?1 }")
    fun existsByUserIdAndStatus(userId: String, status: MembershipStatus): Boolean
}
