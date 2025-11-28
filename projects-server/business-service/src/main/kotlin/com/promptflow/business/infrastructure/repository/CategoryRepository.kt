package com.promptflow.business.infrastructure.repository

import com.promptflow.business.domain.model.Category
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface CategoryRepository : MongoRepository<Category, String> {
    
    fun findByUserId(userId: String): List<Category>
    
    fun findByUserIdAndName(userId: String, name: String): Category?
    
    fun findByUserIdOrderBySortOrderAsc(userId: String): List<Category>
}
