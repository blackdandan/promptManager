package com.promptflow.business.interfaces.rest.dto

import jakarta.validation.constraints.NotBlank

data class SubmitFeedbackRequest(
    @field:NotBlank(message = "反馈类型不能为空")
    val type: String,
    
    @field:NotBlank(message = "反馈内容不能为空")
    val content: String,
    
    val contact: String? = null
)
