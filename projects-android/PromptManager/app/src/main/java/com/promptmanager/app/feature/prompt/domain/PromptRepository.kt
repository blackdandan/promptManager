package com.promptmanager.app.feature.prompt.domain

import com.promptmanager.app.core.database.entity.PromptEntity
import kotlinx.coroutines.flow.Flow

interface PromptRepository {
    val prompts: Flow<List<PromptEntity>>

    suspend fun getPrompts(forceRefresh: Boolean = false): Result<Unit>
    suspend fun createPrompt(title: String, content: String): Result<Unit>
    suspend fun updatePrompt(prompt: PromptEntity): Result<Unit>
    suspend fun deletePrompt(id: String): Result<Unit>
}
