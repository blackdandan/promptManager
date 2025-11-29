package com.promptmanager.app.feature.folder.data

import com.promptmanager.app.core.database.dao.FolderDao
import com.promptmanager.app.core.database.entity.FolderEntity
import com.promptmanager.app.core.network.model.FolderDto
import com.promptmanager.app.core.network.service.FolderService
import com.promptmanager.app.feature.folder.domain.FolderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FolderRepositoryImpl @Inject constructor(
    private val folderService: FolderService,
    private val folderDao: FolderDao
) : FolderRepository {

    override val folders: Flow<List<FolderEntity>> = folderDao.getAllFolders()

    override suspend fun refreshFolders(): Result<Unit> {
        return try {
            val response = folderService.getUserFolders()
            if (response.success && response.data != null) {
                val dtos = response.data
                val entities = dtos.map { it.toEntity() }
                // Clear and replace folders
                // Note: If we want to keep offline changes we should be careful, but for now full sync.
                folderDao.clearFolders()
                folderDao.insertFolders(entities)
                Result.success(Unit)
            } else {
                val errorMsg = response.message ?: response.error?.message ?: "Unknown error"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun FolderDto.toEntity(): FolderEntity {
        return FolderEntity(
            id = id,
            userId = userId,
            name = name,
            parentId = parentId,
            order = order,
            color = color,
            icon = icon,
            description = description,
            promptCount = promptCount,
            createdAt = createdAt ?: "",
            updatedAt = updatedAt ?: ""
        )
    }
}
