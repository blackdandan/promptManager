package com.promptflow.business.interfaces.rest.dto

import com.promptflow.business.domain.model.Prompt
import java.time.LocalDateTime

data class PromptResponse(
    val id: String,
    val userId: String,
    val title: String,
    val content: String,
    val description: String?,
    val tags: List<String>,
    val category: String?,
    val isPublic: Boolean,
    val isFavorite: Boolean,
    val usageCount: Int,
    val folderId: String?,
    val status: String,
    val lastUsedAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun fromDomain(prompt: Prompt): PromptResponse {
            return PromptResponse(
                id = prompt.id!!,
                userId = prompt.userId,
                title = prompt.title,
                content = prompt.content,
                description = prompt.description,
                tags = prompt.tags,
                category = prompt.category,
                isPublic = prompt.isPublic,
                isFavorite = prompt.isFavorite,
                usageCount = prompt.usageCount,
                folderId = prompt.folderId,
                status = prompt.status.name,
                lastUsedAt = prompt.lastUsedAt,
                createdAt = prompt.createdAt,
                updatedAt = prompt.updatedAt
            )
        }
    }
}
