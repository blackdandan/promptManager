package com.promptmanager.app.feature.prompt.domain

import com.promptmanager.app.core.database.entity.PromptEntity
import kotlinx.coroutines.flow.Flow

interface PromptRepository {
    val prompts: Flow<List<PromptEntity>>

    fun getPromptsByFolder(folderId: String?): Flow<List<PromptEntity>>

    suspend fun getPrompts(folderId: String? = null, page: Int = 0, size: Int = 20, forceRefresh: Boolean = false): Result<Unit>
    suspend fun createPrompt(title: String, content: String): Result<Unit>
    suspend fun updatePrompt(prompt: PromptEntity): Result<Unit>
    suspend fun deletePrompt(id: String): Result<Unit>
}
