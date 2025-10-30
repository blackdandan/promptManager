package com.promptflow.membership.infrastructure.repository

import com.promptflow.membership.domain.model.Subscription
import com.promptflow.membership.domain.model.SubscriptionStatus
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.Optional

@Repository
interface SubscriptionRepository : MongoRepository<Subscription, String> {
    
    fun findByUserId(userId: String): List<Subscription>
    
    fun findByUserIdAndStatus(userId: String, status: SubscriptionStatus): Optional<Subscription>
    
    @Query("{ 'user_id': ?0, 'status': { \$in: ?1 } }")
    fun findByUserIdAndStatusIn(userId: String, status: List<SubscriptionStatus>): List<Subscription>
    
    @Query("{ 'status': ?0 }")
    fun findByStatus(status: SubscriptionStatus): List<Subscription>
    
    @Query("{ 'current_period_end': { \$lt: ?0 } }")
    fun findByCurrentPeriodEndBefore(currentPeriodEnd: LocalDateTime): List<Subscription>
    
    @Query("{ 'gateway_subscription_id': ?0 }")
    fun findByGatewaySubscriptionId(gatewaySubscriptionId: String): Optional<Subscription>
    
    @Query("{ 'user_id': ?0 }")
    fun deleteByUserId(userId: String)
    
    fun existsByUserId(userId: String): Boolean
    
    @Query("{ 'user_id': ?0, 'status': ?1 }")
    fun existsByUserIdAndStatus(userId: String, status: SubscriptionStatus): Boolean
}
