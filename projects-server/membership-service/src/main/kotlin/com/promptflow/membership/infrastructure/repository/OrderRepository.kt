package com.promptflow.membership.infrastructure.repository

import com.promptflow.membership.domain.model.Order
import com.promptflow.membership.domain.model.OrderStatus
import com.promptflow.membership.domain.model.PaymentStatus
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.Optional

@Repository
interface OrderRepository : MongoRepository<Order, String> {
    
    fun findByUserId(userId: String): List<Order>
    
    fun findByUserIdAndStatus(userId: String, status: OrderStatus): List<Order>
    
    fun findByUserIdAndPaymentStatus(userId: String, paymentStatus: PaymentStatus): List<Order>
    
    @Query("{ 'user_id': ?0, 'status': { \$in: ?1 } }")
    fun findByUserIdAndStatusIn(userId: String, status: List<OrderStatus>): List<Order>
    
    @Query("{ 'user_id': ?0, 'payment_status': { \$in: ?1 } }")
    fun findByUserIdAndPaymentStatusIn(userId: String, paymentStatus: List<PaymentStatus>): List<Order>
    
    @Query("{ 'status': ?0 }")
    fun findByStatus(status: OrderStatus): List<Order>
    
    @Query("{ 'payment_status': ?0 }")
    fun findByPaymentStatus(paymentStatus: PaymentStatus): List<Order>
    
    @Query("{ 'gateway_order_id': ?0 }")
    fun findByGatewayOrderId(gatewayOrderId: String): Optional<Order>
    
    @Query("{ 'gateway_payment_id': ?0 }")
    fun findByGatewayPaymentId(gatewayPaymentId: String): Optional<Order>
    
    @Query("{ 'created_at': { \$gte: ?0, \$lte: ?1 } }")
    fun findByCreatedAtBetween(startDate: LocalDateTime, endDate: LocalDateTime): List<Order>
    
    @Query("{ 'paid_at': { \$gte: ?0, \$lte: ?1 } }")
    fun findByPaidAtBetween(startDate: LocalDateTime, endDate: LocalDateTime): List<Order>
    
    @Query("{ 'user_id': ?0 }")
    fun deleteByUserId(userId: String)
    
    fun existsByUserId(userId: String): Boolean
    
    @Query("{ 'user_id': ?0, 'status': ?1 }")
    fun existsByUserIdAndStatus(userId: String, status: OrderStatus): Boolean
    
    @Query("{ 'user_id': ?0, 'payment_status': ?1 }")
    fun existsByUserIdAndPaymentStatus(userId: String, paymentStatus: PaymentStatus): Boolean
}
