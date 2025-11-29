package com.promptmanager.app.core.network.service

import com.promptmanager.app.core.network.model.ApiResponse
import com.promptmanager.app.core.network.model.FolderDto
import retrofit2.http.GET

interface FolderService {
    @GET("folders")
    suspend fun getUserFolders(): ApiResponse<List<FolderDto>>
}
