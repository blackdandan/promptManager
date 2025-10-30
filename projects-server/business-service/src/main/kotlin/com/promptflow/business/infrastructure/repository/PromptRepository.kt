package com.promptflow.business.infrastructure.repository

import com.promptflow.business.domain.model.Prompt
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface PromptRepository : MongoRepository<Prompt, String> {
    
    fun findByUserId(userId: String): List<Prompt>
    
    fun findByUserIdAndStatus(userId: String, status: String): List<Prompt>
    
    fun findByUserIdAndIsFavorite(userId: String, isFavorite: Boolean): List<Prompt>
    
    fun findByUserIdAndFolderId(userId: String, folderId: String): List<Prompt>
    
    @Query("{ 'user_id': ?0, 'tags': { \$in: ?1 } }")
    fun findByUserIdAndTagsIn(userId: String, tags: List<String>): List<Prompt>
    
    @Query("{ 'user_id': ?0, 'title': { \$regex: ?1, \$options: 'i' } }")
    fun findByUserIdAndTitleContaining(userId: String, title: String): List<Prompt>
    
    @Query("{ 'user_id': ?0, 'content': { \$regex: ?1, \$options: 'i' } }")
    fun findByUserIdAndContentContaining(userId: String, content: String): List<Prompt>
    
    @Query("{ 'is_public': true, 'status': 'ACTIVE' }")
    fun findPublicPrompts(): List<Prompt>
    
    @Query("{ 'user_id': ?0, 'status': 'ACTIVE' }")
    fun findActivePromptsByUserId(userId: String): List<Prompt>
    
    @Query("{ 'user_id': ?0, 'category': ?1 }")
    fun findByUserIdAndCategory(userId: String, category: String): List<Prompt>
    
    fun countByUserId(userId: String): Long
    
    fun countByUserIdAndIsFavorite(userId: String, isFavorite: Boolean): Long
    
    @Query("{ 'user_id': ?0, 'tags': ?1 }")
    fun countByUserIdAndTag(userId: String, tag: String): Long
}
