package com.promptflow.membership.domain.model

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

/**
 * 支付网关配置类
 * 支持微信支付、支付宝、Google Pay、Apple Pay
 */
@Component
@ConfigurationProperties(prefix = "payment")
data class PaymentGatewayConfig(
    var wechat: WechatConfig = WechatConfig(),
    var alipay: AlipayConfig = AlipayConfig(),
    var googlePay: GooglePayConfig = GooglePayConfig(),
    var applePay: ApplePayConfig = ApplePayConfig(),
    var stripe: StripeConfig = StripeConfig()
)

/**
 * 微信支付配置
 */
data class WechatConfig(
    var enabled: Boolean = false,
    var appId: String = "",
    var mchId: String = "",
    var apiKey: String = "",
    var certPath: String = "",
    var notifyUrl: String = "",
    var refundNotifyUrl: String = "",
    var sandbox: Boolean = false
)

/**
 * 支付宝配置
 */
data class AlipayConfig(
    var enabled: Boolean = false,
    var appId: String = "",
    var privateKey: String = "",
    var publicKey: String = "",
    var notifyUrl: String = "",
    var returnUrl: String = "",
    var sandbox: Boolean = false,
    var gatewayUrl: String = "https://openapi.alipay.com/gateway.do"
)

/**
 * Google Pay配置
 */
data class GooglePayConfig(
    var enabled: Boolean = false,
    var merchantId: String = "",
    var merchantName: String = "",
    var environment: String = "TEST", // TEST or PRODUCTION
    var gatewayMerchantId: String = "",
    var allowedCardNetworks: List<String> = listOf("AMEX", "DISCOVER", "JCB", "MASTERCARD", "VISA"),
    var allowedAuthMethods: List<String> = listOf("PAN_ONLY", "CRYPTOGRAM_3DS")
)

/**
 * Apple Pay配置
 */
data class ApplePayConfig(
    var enabled: Boolean = false,
    var merchantIdentifier: String = "",
    var displayName: String = "",
    var initiative: String = "web",
    var initiativeContext: String = "",
    var merchantCapabilities: List<String> = listOf("supports3DS", "supportsCredit", "supportsDebit"),
    var supportedNetworks: List<String> = listOf("amex", "discover", "masterCard", "visa")
)

/**
 * Stripe配置 (国际支付)
 */
data class StripeConfig(
    var enabled: Boolean = false,
    var publishableKey: String = "",
    var secretKey: String = "",
    var webhookSecret: String = "",
    var successUrl: String = "",
    var cancelUrl: String = "",
    var currency: String = "usd"
)
