package com.promptmanager.app.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prompts")
data class PromptEntity(
    @PrimaryKey
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
    val lastUsedAt: String?,
    val createdAt: String,
    val updatedAt: String
)
