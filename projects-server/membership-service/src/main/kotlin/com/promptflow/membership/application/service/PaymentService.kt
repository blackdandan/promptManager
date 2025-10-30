package com.promptflow.membership.application.service

import com.promptflow.membership.domain.model.*
import com.promptflow.membership.infrastructure.repository.OrderRepository
import com.promptflow.membership.infrastructure.repository.SubscriptionRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class PaymentService(
    private val orderRepository: OrderRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val membershipService: MembershipService
) {
    
    private val log = LoggerFactory.getLogger(this::class.java)
    
    fun createOrder(
        userId: String,
        planId: String,
        planType: PlanType,
        billingCycle: BillingCycle,
        amount: Int,
        paymentMethod: PaymentMethod? = null
    ): Order {
        log.info("创建订单: $userId -> $planType ($billingCycle)")
        
        val order = Order(
            userId = userId,
            planId = planId,
            planType = planType,
            billingCycle = billingCycle,
            amount = amount,
            status = OrderStatus.PENDING,
            paymentStatus = PaymentStatus.PENDING,
            paymentMethod = paymentMethod
        )
        
        return orderRepository.save(order).also {
            log.info("订单创建成功: ${it.id}")
        }
    }
    
    fun processPayment(
        orderId: String,
        paymentGateway: PaymentGateway,
        gatewayOrderId: String,
        gatewayPaymentId: String
    ): Order {
        log.info("处理支付: $orderId via $paymentGateway")
        
        val order = orderRepository.findById(orderId)
            .orElseThrow { OrderNotFoundException("订单不存在: $orderId") }
        
        if (order.paymentStatus != PaymentStatus.PENDING) {
            throw PaymentProcessingException("订单支付状态不允许处理: ${order.paymentStatus}")
        }
        
        val updatedOrder = order.copy(
            paymentStatus = PaymentStatus.PROCESSING,
            paymentGateway = paymentGateway,
            gatewayOrderId = gatewayOrderId,
            gatewayPaymentId = gatewayPaymentId,
            updatedAt = LocalDateTime.now()
        )
        
        return orderRepository.save(updatedOrder).also {
            log.info("支付处理中: $orderId")
        }
    }
    
    fun completePayment(orderId: String): Order {
        log.info("完成支付: $orderId")
        
        val order = orderRepository.findById(orderId)
            .orElseThrow { OrderNotFoundException("订单不存在: $orderId") }
        
        if (order.paymentStatus != PaymentStatus.PROCESSING) {
            throw PaymentProcessingException("订单支付状态不允许完成: ${order.paymentStatus}")
        }
        
        val now = LocalDateTime.now()
        val updatedOrder = order.copy(
            status = OrderStatus.CONFIRMED,
            paymentStatus = PaymentStatus.SUCCEEDED,
            paidAt = now,
            updatedAt = now
        )
        
        val savedOrder = orderRepository.save(updatedOrder)
        
        // 创建或更新订阅
        createOrUpdateSubscription(savedOrder)
        
        log.info("支付完成: $orderId")
        return savedOrder
    }
    
    fun failPayment(orderId: String, reason: String? = null): Order {
        log.info("支付失败: $orderId - $reason")
        
        val order = orderRepository.findById(orderId)
            .orElseThrow { OrderNotFoundException("订单不存在: $orderId") }
        
        val updatedOrder = order.copy(
            status = OrderStatus.CANCELLED,
            paymentStatus = PaymentStatus.FAILED,
            updatedAt = LocalDateTime.now()
        )
        
        return orderRepository.save(updatedOrder).also {
            log.info("支付失败处理完成: $orderId")
        }
    }
    
    fun createSubscription(
        userId: String,
        planId: String,
        planType: PlanType,
        billingCycle: BillingCycle,
        amount: Int,
        paymentGateway: PaymentGateway? = null,
        gatewaySubscriptionId: String? = null
    ): Subscription {
        log.info("创建订阅: $userId -> $planType ($billingCycle)")
        
        val now = LocalDateTime.now()
        val periodEnd = calculatePeriodEnd(now, billingCycle)
        
        val subscription = Subscription(
            userId = userId,
            planId = planId,
            planType = planType,
            status = SubscriptionStatus.ACTIVE,
            billingCycle = billingCycle,
            amount = amount,
            currentPeriodStart = now,
            currentPeriodEnd = periodEnd,
            paymentGateway = paymentGateway,
            gatewaySubscriptionId = gatewaySubscriptionId
        )
        
        return subscriptionRepository.save(subscription).also {
            log.info("订阅创建成功: ${it.id}")
        }
    }
    
    fun cancelSubscription(subscriptionId: String): Subscription {
        log.info("取消订阅: $subscriptionId")
        
        val subscription = subscriptionRepository.findById(subscriptionId)
            .orElseThrow { SubscriptionNotFoundException("订阅不存在: $subscriptionId") }
        
        if (subscription.status != SubscriptionStatus.ACTIVE) {
            throw SubscriptionOperationNotAllowedException("订阅状态不允许取消: ${subscription.status}")
        }
        
        val updatedSubscription = subscription.copy(
            status = SubscriptionStatus.CANCELED,
            cancelAtPeriodEnd = true,
            canceledAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        return subscriptionRepository.save(updatedSubscription).also {
            log.info("订阅取消成功: $subscriptionId")
        }
    }
    
    fun renewSubscription(subscriptionId: String): Subscription {
        log.info("续订订阅: $subscriptionId")
        
        val subscription = subscriptionRepository.findById(subscriptionId)
            .orElseThrow { SubscriptionNotFoundException("订阅不存在: $subscriptionId") }
        
        if (subscription.status != SubscriptionStatus.ACTIVE) {
            throw SubscriptionOperationNotAllowedException("订阅状态不允许续订: ${subscription.status}")
        }
        
        val now = LocalDateTime.now()
        val newPeriodEnd = calculatePeriodEnd(now, subscription.billingCycle)
        
        val updatedSubscription = subscription.copy(
            currentPeriodStart = now,
            currentPeriodEnd = newPeriodEnd,
            updatedAt = now
        )
        
        return subscriptionRepository.save(updatedSubscription).also {
            log.info("订阅续订成功: $subscriptionId")
        }
    }
    
    fun getUserOrders(userId: String): List<Order> {
        return orderRepository.findByUserId(userId)
    }
    
    fun getUserSubscriptions(userId: String): List<Subscription> {
        return subscriptionRepository.findByUserId(userId)
    }
    
    fun getActiveSubscription(userId: String): Subscription? {
        return subscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE).orElse(null)
    }
    
    private fun createOrUpdateSubscription(order: Order) {
        log.info("为订单创建或更新订阅: ${order.id}")
        
        val existingSubscription = getActiveSubscription(order.userId)
        
        if (existingSubscription != null) {
            // 更新现有订阅
            val updatedSubscription = existingSubscription.copy(
                planId = order.planId,
                planType = order.planType,
                billingCycle = order.billingCycle,
                amount = order.amount,
                updatedAt = LocalDateTime.now()
            )
            subscriptionRepository.save(updatedSubscription)
            log.info("订阅更新成功: ${existingSubscription.id}")
        } else {
            // 创建新订阅
            createSubscription(
                userId = order.userId,
                planId = order.planId,
                planType = order.planType,
                billingCycle = order.billingCycle,
                amount = order.amount
            )
        }
        
        // 升级会员
        membershipService.upgradeMembership(
            userId = order.userId,
            planId = order.planId,
            planType = order.planType,
            billingCycle = order.billingCycle,
            amount = order.amount
        )
    }
    
    private fun calculatePeriodEnd(startDate: LocalDateTime, billingCycle: BillingCycle): LocalDateTime {
        return when (billingCycle) {
            BillingCycle.MONTHLY -> startDate.plusMonths(1)
            BillingCycle.QUARTERLY -> startDate.plusMonths(3)
            BillingCycle.YEARLY -> startDate.plusYears(1)
        }
    }
}

class OrderNotFoundException(message: String) : RuntimeException(message)
class PaymentProcessingException(message: String) : RuntimeException(message)
class SubscriptionNotFoundException(message: String) : RuntimeException(message)
class SubscriptionOperationNotAllowedException(message: String) : RuntimeException(message)
