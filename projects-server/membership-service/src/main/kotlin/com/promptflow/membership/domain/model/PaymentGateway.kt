package com.promptflow.membership.domain.model

/**
 * 支付网关枚举
 */
enum class PaymentGateway {
    WECHAT_PAY,     // 微信支付
    ALIPAY,         // 支付宝
    GOOGLE_PAY,     // Google Pay
    APPLE_PAY,      // Apple Pay
    STRIPE          // Stripe (国际支付)
}
