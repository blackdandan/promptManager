package com.promptflow.membership.domain.exception

/**
 * 会员服务错误代码枚举
 */
enum class ErrorCode(
    val code: String,
    val message: String
) {
    // 会员相关错误
    MEMBERSHIP_001("MEMBERSHIP_001", "会员信息不存在"),
    MEMBERSHIP_002("MEMBERSHIP_002", "会员信息已存在"),
    MEMBERSHIP_003("MEMBERSHIP_003", "会员状态无效"),
    MEMBERSHIP_004("MEMBERSHIP_004", "会员等级无效"),
    MEMBERSHIP_005("MEMBERSHIP_005", "会员权益配置错误"),
    
    // 支付相关错误
    PAYMENT_001("PAYMENT_001", "支付订单不存在"),
    PAYMENT_002("PAYMENT_002", "支付金额无效"),
    PAYMENT_003("PAYMENT_003", "支付状态无效"),
    PAYMENT_004("PAYMENT_004", "支付网关配置错误"),
    
    // 订阅相关错误
    SUBSCRIPTION_001("SUBSCRIPTION_001", "订阅信息不存在"),
    SUBSCRIPTION_002("SUBSCRIPTION_002", "订阅状态无效"),
    SUBSCRIPTION_003("SUBSCRIPTION_003", "订阅套餐无效"),
    SUBSCRIPTION_004("SUBSCRIPTION_004", "订阅续期失败"),
    
    // 订单相关错误
    ORDER_001("ORDER_001", "订单信息不存在"),
    ORDER_002("ORDER_002", "订单状态无效"),
    ORDER_003("ORDER_003", "订单金额不匹配"),
    ORDER_004("ORDER_004", "订单支付方式不支持"),
    
    // 通用错误
    VALIDATION_ERROR("VALIDATION_ERROR", "参数验证失败"),
    DATABASE_ERROR("DATABASE_ERROR", "数据库操作失败"),
    EXTERNAL_SERVICE_ERROR("EXTERNAL_SERVICE_ERROR", "外部服务调用失败"),
    UNKNOWN_ERROR("UNKNOWN_ERROR", "未知错误")
}
