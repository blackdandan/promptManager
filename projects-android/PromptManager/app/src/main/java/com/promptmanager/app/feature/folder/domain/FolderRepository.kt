package com.promptmanager.app.feature.folder.domain

import com.promptmanager.app.core.database.entity.FolderEntity
import kotlinx.coroutines.flow.Flow

interface FolderRepository {
    val folders: Flow<List<FolderEntity>>

    suspend fun refreshFolders(): Result<Unit>
}
