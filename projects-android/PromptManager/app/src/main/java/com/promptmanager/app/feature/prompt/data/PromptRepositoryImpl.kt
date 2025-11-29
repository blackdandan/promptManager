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

    override fun getPromptsByFolder(folderId: String?): Flow<List<PromptEntity>> {
        return if (folderId == null || folderId == "all") {
            promptDao.getAllPrompts()
        } else {
            promptDao.getPromptsByFolder(folderId)
        }
    }

    override suspend fun getPrompts(folderId: String?, page: Int, size: Int, forceRefresh: Boolean): Result<Unit> {
        return try {
            val apiFolderId = if (folderId == "all") null else folderId
            val response = promptService.getPrompts(
                page = page,
                size = size,
                folderId = apiFolderId
            )
            if (response.success && response.data != null) {
                val dtos = response.data.content
                val entities = dtos.map { it.toEntity() }
                
                // If refreshing (page 0), we might want to clear existing prompts for this folder?
                // But Room's Flow will update UI automatically.
                // For a proper sync, we should probably delete prompts that are not in the list if we fetched everything.
                // But with pagination, we can't delete everything.
                // A simple strategy: if page == 0 and forceRefresh is true, maybe clear cache for this folder?
                // But `promptDao` doesn't have `deleteByFolder`.
                // Let's keep it additive for now, relying on `insertPrompts` (REPLACE).
                
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
