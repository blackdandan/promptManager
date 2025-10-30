package com.promptflow.business.interfaces.rest.dto

import com.promptflow.business.domain.model.Prompt
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreatePromptRequest(
    @field:NotBlank(message = "标题不能为空")
    @field:Size(max = 200, message = "标题长度不能超过200个字符")
    val title: String,
    
    @field:NotBlank(message = "内容不能为空")
    val content: String,
    
    val description: String? = null,
    
    val tags: List<String> = emptyList(),
    
    val category: String? = null,
    
    val isPublic: Boolean = false,
    
    val folderId: String? = null
) {
    fun toDomain(userId: String): Prompt {
        return Prompt(
            userId = userId,
            title = title,
            content = content,
            description = description,
            tags = tags,
            category = category,
            isPublic = isPublic,
            folderId = folderId,
        )
    }
}
