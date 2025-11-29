package com.promptmanager.app.feature.prompt.data

import com.promptmanager.app.core.database.dao.PromptDao
import com.promptmanager.app.core.database.entity.PromptEntity
import com.promptmanager.app.core.network.model.PromptDto
import com.promptmanager.app.core.network.service.PromptService
import com.promptmanager.app.feature.prompt.domain.PromptRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PromptRepositoryImpl @Inject constructor(
    private val promptService: PromptService,
    private val promptDao: PromptDao
) : PromptRepository {

    override val prompts: Flow<List<PromptEntity>> = promptDao.getAllPrompts()

    override suspend fun getPrompts(forceRefresh: Boolean): Result<Unit> {
        return try {
            val response = promptService.getPrompts()
            if (response.success && response.data != null) {
                val dtos = response.data.content
                val entities = dtos.map { it.toEntity() }
                // For simplicity, we can clear and replace, or use upsert. 
                // promptDao.insertPrompts uses OnConflictStrategy.REPLACE
                // If we want to sync deletion, we should clear prompts first if it's a full sync.
                // For now, let's just insert/update.
                promptDao.insertPrompts(entities)
                Result.success(Unit)
            } else {
                val errorMsg = response.message ?: response.error?.message ?: "Unknown error"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createPrompt(title: String, content: String): Result<Unit> {
        return try {
            val dto = PromptDto(title = title, content = content)
            val response = promptService.createPrompt(dto)
            if (response.success && response.data != null) {
                promptDao.insertPrompt(response.data.toEntity())
                Result.success(Unit)
            } else {
                val errorMsg = response.message ?: response.error?.message ?: "Unknown error"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePrompt(prompt: PromptEntity): Result<Unit> {
        return try {
            val dto = prompt.toDto()
            val response = promptService.updatePrompt(prompt.id, dto)
            if (response.success && response.data != null) {
                promptDao.insertPrompt(response.data.toEntity())
                Result.success(Unit)
            } else {
                val errorMsg = response.message ?: response.error?.message ?: "Unknown error"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deletePrompt(id: String): Result<Unit> {
        return try {
            val response = promptService.deletePrompt(id)
            if (response.success) {
                promptDao.deletePromptById(id)
                Result.success(Unit)
            } else {
                val errorMsg = response.message ?: response.error?.message ?: "Unknown error"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun PromptDto.toEntity(): PromptEntity {
        return PromptEntity(
            id = id ?: "",
            userId = userId ?: "",
            title = title,
            content = content,
            description = description,
            tags = tags,
            category = category,
            isPublic = isPublic,
            isFavorite = isFavorite,
            usageCount = usageCount,
            folderId = folderId,
            status = status ?: "ACTIVE",
            lastUsedAt = lastUsedAt,
            createdAt = createdAt ?: "",
            updatedAt = updatedAt ?: ""
        )
    }

    private fun PromptEntity.toDto(): PromptDto {
        return PromptDto(
            id = id,
            userId = userId,
            title = title,
            content = content,
            description = description,
            tags = tags,
            category = category,
            isPublic = isPublic,
            isFavorite = isFavorite,
            usageCount = usageCount,
            folderId = folderId,
            status = status,
            lastUsedAt = lastUsedAt,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
