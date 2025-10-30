package com.promptflow.business.domain.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.LocalDateTime

@Document(collection = "prompts")
data class Prompt(
    @Id
    val id: String? = null,
    
    @Field("user_id")
    val userId: String,
    
    @Field("title")
    val title: String,
    
    @Field("content")
    val content: String,
    
    @Field("description")
    val description: String? = null,
    
    @Field("tags")
    val tags: List<String> = emptyList(),
    
    @Field("category")
    val category: String? = null,
    
    @Field("is_public")
    val isPublic: Boolean = false,
    
    @Field("is_favorite")
    val isFavorite: Boolean = false,
    
    @Field("usage_count")
    val usageCount: Int = 0,
    
    @Field("folder_id")
    val folderId: String? = null,
    
    @Field("status")
    val status: PromptStatus = PromptStatus.ACTIVE,
    
    @Field("last_used_at")
    val lastUsedAt: LocalDateTime? = null,
    
    @Field("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Field("updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class PromptStatus {
    ACTIVE,
    ARCHIVED,
    DELETED
}
