package com.promptflow.business.interfaces.rest.dto

import jakarta.validation.constraints.Size

data class UpdatePromptRequest(
    @field:Size(max = 200, message = "标题长度不能超过200个字符")
    val title: String? = null,
    
    val content: String? = null,
    
    val description: String? = null,
    
    val tags: List<String>? = null,
    
    val category: String? = null,
    
    val isPublic: Boolean? = null,
    
    val isFavorite: Boolean? = null,
    
    val folderId: String? = null,
    
    val status: String? = null
)
