package com.promptmanager.app.core.network.service

import com.promptmanager.app.core.network.model.ApiResponse
import com.promptmanager.app.core.network.model.Page
import com.promptmanager.app.core.network.model.PromptDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface PromptService {
    @GET("prompts")
    suspend fun getPrompts(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("search") search: String? = null,
        @Query("tags") tags: List<String>? = null,
        @Query("folderId") folderId: String? = null
    ): ApiResponse<Page<PromptDto>>

    @GET("prompts/{id}")
    suspend fun getPrompt(@Path("id") id: String): ApiResponse<PromptDto>

    @POST("prompts")
    suspend fun createPrompt(@Body prompt: PromptDto): ApiResponse<PromptDto>

    @PUT("prompts/{id}")
    suspend fun updatePrompt(@Path("id") id: String, @Body prompt: PromptDto): ApiResponse<PromptDto>

    @DELETE("prompts/{id}")
    suspend fun deletePrompt(@Path("id") id: String): ApiResponse<Unit>
}
