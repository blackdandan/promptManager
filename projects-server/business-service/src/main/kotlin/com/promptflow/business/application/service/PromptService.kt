package com.promptflow.business.application.service

import com.promptflow.business.domain.model.Prompt
import com.promptflow.business.domain.model.PromptStatus
import com.promptflow.business.infrastructure.repository.PromptRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.support.PageableExecutionUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class PromptService(
    private val promptRepository: PromptRepository,
    private val mongoTemplate: MongoTemplate
) {
    
    private val log = LoggerFactory.getLogger(this::class.java)
    
    fun getUserPrompts(
        userId: String,
        search: String?,
        tags: List<String>?,
        category: String?,
        isFavorite: Boolean?,
        pageable: Pageable
    ): Page<Prompt> {
        val query = Query().addCriteria(Criteria.where("user_id").`is`(userId))
        
        // 搜索条件
        if (!search.isNullOrBlank()) {
            val searchCriteria = Criteria().orOperator(
                Criteria.where("title").regex(search, "i"),
                Criteria.where("content").regex(search, "i"),
                Criteria.where("description").regex(search, "i")
            )
            query.addCriteria(searchCriteria)
        }
        
        // 标签过滤
        if (!tags.isNullOrEmpty()) {
            query.addCriteria(Criteria.where("tags").`in`(tags))
        }
        
        // 分类过滤
        if (!category.isNullOrBlank()) {
            query.addCriteria(Criteria.where("category").`is`(category))
        }
        
        // 收藏过滤
        if (isFavorite != null) {
            query.addCriteria(Criteria.where("is_favorite").`is`(isFavorite))
        }
        
        // 只查询活跃的Prompt
        query.addCriteria(Criteria.where("status").`is`(PromptStatus.ACTIVE.name))
        
        // 分页
        query.with(pageable)
        
        val prompts = mongoTemplate.find(query, Prompt::class.java)
        val count = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), Prompt::class.java)
        
        return PageableExecutionUtils.getPage(prompts, pageable) { count }
    }
    
    fun getPrompt(userId: String, id: String): Prompt {
        return promptRepository.findById(id)
            .orElseThrow { PromptNotFoundException("Prompt不存在: $id") }
            .also { prompt ->
                if (prompt.userId != userId) {
                    throw PromptAccessDeniedException("无权访问此Prompt")
                }
            }
    }
    
    fun createPrompt(userId: String, prompt: Prompt): Prompt {
        val newPrompt = prompt.copy(
            userId = userId,
            status = PromptStatus.ACTIVE,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        return promptRepository.save(newPrompt).also {
            log.info("用户 $userId 创建Prompt: ${it.id}")
        }
    }
    
    fun updatePrompt(userId: String, id: String, prompt: Prompt): Prompt {
        val existingPrompt = getPrompt(userId, id)
        
        val updatedPrompt = existingPrompt.copy(
            title = prompt.title,
            content = prompt.content,
            description = prompt.description,
            tags = prompt.tags,
            category = prompt.category,
            isPublic = prompt.isPublic,
            folderId = prompt.folderId,
            updatedAt = LocalDateTime.now()
        )
        
        return promptRepository.save(updatedPrompt).also {
            log.info("用户 $userId 更新Prompt: $id")
        }
    }
    
    fun deletePrompt(userId: String, id: String) {
        val prompt = getPrompt(userId, id)
        val deletedPrompt = prompt.copy(
            status = PromptStatus.DELETED,
            updatedAt = LocalDateTime.now()
        )
        promptRepository.save(deletedPrompt)
        log.info("用户 $userId 删除Prompt: $id")
    }
    
    fun toggleFavorite(userId: String, id: String): Prompt {
        val prompt = getPrompt(userId, id)
        val updatedPrompt = prompt.copy(
            isFavorite = !prompt.isFavorite,
            updatedAt = LocalDateTime.now()
        )
        return promptRepository.save(updatedPrompt).also {
            log.info("用户 $userId 切换Prompt收藏状态: $id -> ${it.isFavorite}")
        }
    }
    
    fun getPublicPrompts(search: String?, tags: List<String>?, pageable: Pageable): Page<Prompt> {
        val query = Query().addCriteria(
            Criteria.where("is_public").`is`(true)
                .and("status").`is`(PromptStatus.ACTIVE.name)
        )
        
        // 搜索条件
        if (!search.isNullOrBlank()) {
            val searchCriteria = Criteria().orOperator(
                Criteria.where("title").regex(search, "i"),
                Criteria.where("content").regex(search, "i"),
                Criteria.where("description").regex(search, "i")
            )
            query.addCriteria(searchCriteria)
        }
        
        // 标签过滤
        if (!tags.isNullOrEmpty()) {
            query.addCriteria(Criteria.where("tags").`in`(tags))
        }
        
        query.with(pageable)
        
        val prompts = mongoTemplate.find(query, Prompt::class.java)
        val count = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), Prompt::class.java)
        
        return PageableExecutionUtils.getPage(prompts, pageable) { count }
    }
    
    fun getUserTags(userId: String): List<String> {
        val query = Query().addCriteria(
            Criteria.where("user_id").`is`(userId)
                .and("status").`is`(PromptStatus.ACTIVE.name)
        )
        
        return mongoTemplate.findDistinct(query, "tags", Prompt::class.java, String::class.java)
            .filterNotNull()
            .distinct()
            .sorted()
    }
    
    fun getUserStats(userId: String): Map<String, Any> {
        val totalCount = promptRepository.countByUserId(userId)
        val favoriteCount = promptRepository.countByUserIdAndIsFavorite(userId, true)
        
        return mapOf(
            "totalPrompts" to totalCount,
            "favoritePrompts" to favoriteCount,
            "publicPrompts" to promptRepository.findByUserIdAndStatus(userId, PromptStatus.ACTIVE.name)
                .count { it.isPublic }
        )
    }
    
    fun incrementUsageCount(userId: String, id: String) {
        val query = Query().addCriteria(
            Criteria.where("_id").`is`(id)
                .and("user_id").`is`(userId)
        )
        
        val update = Update()
            .inc("usage_count", 1)
            .set("last_used_at", LocalDateTime.now())
            .set("updated_at", LocalDateTime.now())
        
        mongoTemplate.updateFirst(query, update, Prompt::class.java)
    }
}

class PromptNotFoundException(message: String) : RuntimeException(message)

class PromptAccessDeniedException(message: String) : RuntimeException(message)
