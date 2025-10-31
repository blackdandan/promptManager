package com.promptflow.business.interfaces.rest

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
        pageable: Pageable
    ): ResponseEntity<Page<PromptResponse>> {
        log.info("获取用户 $userId 的Prompt列表")
        val prompts = promptService.getUserPrompts(userId, search, tags, category, isFavorite, pageable)
        return ResponseEntity.ok(prompts.map { PromptResponse.fromDomain(it) })
    }
    
    @GetMapping("/{id}")
    fun getPrompt(
        @RequestHeader("X-User-ID") userId: String,
        @PathVariable id: String
    ): ResponseEntity<PromptResponse> {
        log.info("获取Prompt: $id")
        val prompt = promptService.getPrompt(userId, id)
        return ResponseEntity.ok(PromptResponse.fromDomain(prompt))
    }
    
    @PostMapping
    fun createPrompt(
        @RequestHeader("X-User-ID") userId: String,
        @Valid @RequestBody request: CreatePromptRequest
    ): ResponseEntity<PromptResponse> {
        log.info("创建Prompt: ${request.title}")
        val prompt = promptService.createPrompt(userId, request.toDomain(userId))
        return ResponseEntity.status(HttpStatus.CREATED).body(PromptResponse.fromDomain(prompt))
    }
    
    @PutMapping("/{id}")
    fun updatePrompt(
        @RequestHeader("X-User-ID") userId: String,
        @PathVariable id: String,
        @Valid @RequestBody request: UpdatePromptRequest
    ): ResponseEntity<PromptResponse> {
        log.info("更新Prompt: $id")
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
        return ResponseEntity.ok(PromptResponse.fromDomain(savedPrompt))
    }
    
    @DeleteMapping("/{id}")
    fun deletePrompt(
        @RequestHeader("X-User-ID") userId: String,
        @PathVariable id: String
    ): ResponseEntity<Void> {
        log.info("删除Prompt: $id")
        promptService.deletePrompt(userId, id)
        return ResponseEntity.noContent().build()
    }
    
    @PostMapping("/{id}/favorite")
    fun toggleFavorite(
        @RequestHeader("X-User-ID") userId: String,
        @PathVariable id: String
    ): ResponseEntity<PromptResponse> {
        log.info("切换收藏状态: $id")
        val prompt = promptService.toggleFavorite(userId, id)
        return ResponseEntity.ok(PromptResponse.fromDomain(prompt))
    }
    
    @GetMapping("/public")
    fun getPublicPrompts(
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) tags: List<String>?,
        pageable: Pageable
    ): ResponseEntity<Page<PromptResponse>> {
        log.info("获取公开Prompt列表")
        val prompts = promptService.getPublicPrompts(search, tags, pageable)
        return ResponseEntity.ok(prompts.map { PromptResponse.fromDomain(it) })
    }
    
    @GetMapping("/tags")
    fun getUserTags(@RequestHeader("X-User-ID") userId: String): ResponseEntity<List<String>> {
        log.info("获取用户 $userId 的标签列表")
        val tags = promptService.getUserTags(userId)
        return ResponseEntity.ok(tags)
    }
    
    @GetMapping("/stats")
    fun getUserStats(@RequestHeader("X-User-ID") userId: String): ResponseEntity<Map<String, Any>> {
        log.info("获取用户 $userId 的统计信息")
        val stats = promptService.getUserStats(userId)
        return ResponseEntity.ok(stats)
    }
}
