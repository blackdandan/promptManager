package com.promptflow.business.interfaces.rest

import com.promptflow.common.dto.ApiResponse
import com.promptflow.business.application.service.PromptService
import com.promptflow.business.domain.model.PromptStatus
import com.promptflow.business.interfaces.rest.dto.CreatePromptRequest
import com.promptflow.business.interfaces.rest.dto.PromptResponse
import com.promptflow.business.interfaces.rest.dto.UpdatePromptRequest
import org.slf4j.LoggerFactory
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/prompts")
class PromptController(
    private val promptService: PromptService
) {
    
    private val log = LoggerFactory.getLogger(this::class.java)
    
    @GetMapping
    fun getUserPrompts(
        @RequestHeader("X-User-ID") userId: String,
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) tags: List<String>?,
        @RequestParam(required = false) category: String?,
        @RequestParam(required = false) isFavorite: Boolean?,
        @RequestParam(required = false) folderId: String?,
        pageable: Pageable
    ): ResponseEntity<ApiResponse<Page<PromptResponse>>> {
        log.info("获取用户 $userId 的Prompt列表")
        try {
            val prompts = promptService.getUserPrompts(userId, search, tags, category, isFavorite, folderId, pageable)
            return ResponseEntity.ok(ApiResponse.success(prompts.map { PromptResponse.fromDomain(it) }, "获取Prompt列表成功"))
        } catch (e: Exception) {
            log.error("获取Prompt列表失败: ${e.message}")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("SYSTEM_001", e.message ?: "获取Prompt列表失败"))
        }
    }
    
    @GetMapping("/folder/{folderId}")
    fun getPromptsByFolder(
        @RequestHeader("X-User-ID") userId: String,
        @PathVariable folderId: String,
        pageable: Pageable
    ): ResponseEntity<ApiResponse<Page<PromptResponse>>> {
        log.info("获取文件夹 $folderId 中的Prompt列表")
        try {
            val prompts = promptService.getPromptsByFolder(userId, folderId, pageable)
            return ResponseEntity.ok(ApiResponse.success(prompts.map { PromptResponse.fromDomain(it) }, "获取文件夹Prompt列表成功"))
        } catch (e: Exception) {
            log.error("获取文件夹Prompt列表失败: ${e.message}")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("SYSTEM_005", e.message ?: "获取文件夹Prompt列表失败"))
        }
    }
    
    @GetMapping("/{id}")
    fun getPrompt(
        @RequestHeader("X-User-ID") userId: String,
        @PathVariable id: String
    ): ResponseEntity<ApiResponse<PromptResponse>> {
        log.info("获取Prompt: $id")
        try {
            val prompt = promptService.getPrompt(userId, id)
            return ResponseEntity.ok(ApiResponse.success(PromptResponse.fromDomain(prompt), "获取Prompt成功"))
        } catch (e: Exception) {
            log.error("获取Prompt失败: ${e.message}")
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("PROMPT_001", e.message ?: "Prompt不存在"))
        }
    }
    
    @PostMapping
    fun createPrompt(
        @RequestHeader("X-User-ID") userId: String,
        @Valid @RequestBody request: CreatePromptRequest
    ): ResponseEntity<ApiResponse<PromptResponse>> {
        log.info("创建Prompt: ${request.title}")
        try {
            val prompt = promptService.createPrompt(userId, request.toDomain(userId))
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(PromptResponse.fromDomain(prompt), "Prompt创建成功"))
        } catch (e: Exception) {
            log.error("创建Prompt失败: ${e.message}")
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("PROMPT_002", e.message ?: "创建Prompt失败"))
        }
    }
    
    @PutMapping("/{id}")
    fun updatePrompt(
        @RequestHeader("X-User-ID") userId: String,
        @PathVariable id: String,
        @Valid @RequestBody request: UpdatePromptRequest
    ): ResponseEntity<ApiResponse<PromptResponse>> {
        log.info("更新Prompt: $id")
        try {
            val existingPrompt = promptService.getPrompt(userId, id)
            
            val updatedPrompt = existingPrompt.copy(
                title = request.title ?: existingPrompt.title,
                content = request.content ?: existingPrompt.content,
                description = request.description ?: existingPrompt.description,
                tags = request.tags ?: existingPrompt.tags,
                category = request.category ?: existingPrompt.category,
                isPublic = request.isPublic ?: existingPrompt.isPublic,
                isFavorite = request.isFavorite ?: existingPrompt.isFavorite,
                folderId = request.folderId ?: existingPrompt.folderId,
                status = request.status?.let { PromptStatus.valueOf(it) } ?: existingPrompt.status
            )
            
            val savedPrompt = promptService.updatePrompt(userId, id, updatedPrompt)
            return ResponseEntity.ok(ApiResponse.success(PromptResponse.fromDomain(savedPrompt), "Prompt更新成功"))
        } catch (e: Exception) {
            log.error("更新Prompt失败: ${e.message}")
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("PROMPT_003", e.message ?: "更新Prompt失败"))
        }
    }
    
    @DeleteMapping("/{id}")
    fun deletePrompt(
        @RequestHeader("X-User-ID") userId: String,
        @PathVariable id: String
    ): ResponseEntity<ApiResponse<Void>> {
        log.info("删除Prompt: $id")
        try {
            promptService.deletePrompt(userId, id)
            return ResponseEntity.ok(ApiResponse.success(null, "Prompt删除成功"))
        } catch (e: Exception) {
            log.error("删除Prompt失败: ${e.message}")
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("PROMPT_004", e.message ?: "删除Prompt失败"))
        }
    }
    
    @PostMapping("/{id}/favorite")
    fun toggleFavorite(
        @RequestHeader("X-User-ID") userId: String,
        @PathVariable id: String
    ): ResponseEntity<ApiResponse<PromptResponse>> {
        log.info("切换收藏状态: $id")
        try {
            val prompt = promptService.toggleFavorite(userId, id)
            return ResponseEntity.ok(ApiResponse.success(PromptResponse.fromDomain(prompt), "收藏状态切换成功"))
        } catch (e: Exception) {
            log.error("切换收藏状态失败: ${e.message}")
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("PROMPT_005", e.message ?: "切换收藏状态失败"))
        }
    }
    
    @GetMapping("/public")
    fun getPublicPrompts(
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) tags: List<String>?,
        pageable: Pageable
    ): ResponseEntity<ApiResponse<Page<PromptResponse>>> {
        log.info("获取公开Prompt列表")
        try {
            val prompts = promptService.getPublicPrompts(search, tags, pageable)
            return ResponseEntity.ok(ApiResponse.success(prompts.map { PromptResponse.fromDomain(it) }, "获取公开Prompt列表成功"))
        } catch (e: Exception) {
            log.error("获取公开Prompt列表失败: ${e.message}")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("SYSTEM_002", e.message ?: "获取公开Prompt列表失败"))
        }
    }
    
    @GetMapping("/tags")
    fun getUserTags(@RequestHeader("X-User-ID") userId: String): ResponseEntity<ApiResponse<List<String>>> {
        log.info("获取用户 $userId 的标签列表")
        try {
            val tags = promptService.getUserTags(userId)
            return ResponseEntity.ok(ApiResponse.success(tags, "获取标签列表成功"))
        } catch (e: Exception) {
            log.error("获取标签列表失败: ${e.message}")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("SYSTEM_003", e.message ?: "获取标签列表失败"))
        }
    }
    
    @GetMapping("/stats")
    fun getUserStats(@RequestHeader("X-User-ID") userId: String): ResponseEntity<ApiResponse<Map<String, Any>>> {
        log.info("获取用户 $userId 的统计信息")
        try {
            val stats = promptService.getUserStats(userId)
            return ResponseEntity.ok(ApiResponse.success(stats, "获取统计信息成功"))
        } catch (e: Exception) {
            log.error("获取统计信息失败: ${e.message}")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("SYSTEM_004", e.message ?: "获取统计信息失败"))
        }
    }
}
