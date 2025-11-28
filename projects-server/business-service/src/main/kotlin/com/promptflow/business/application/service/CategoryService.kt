package com.promptflow.business.application.service

import com.promptflow.business.domain.model.Category
import com.promptflow.business.infrastructure.repository.CategoryRepository
import com.promptflow.business.infrastructure.repository.PromptRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class CategoryService(
    private val categoryRepository: CategoryRepository,
    private val promptRepository: PromptRepository
) {
    
    // 初始化默认分类
    fun initDefaultCategories(userId: String) {
        val defaultCategories = listOf("通用", "写作", "编程", "分析", "创意", "营销")
        defaultCategories.forEachIndexed { index, name ->
            if (categoryRepository.findByUserIdAndName(userId, name) == null) {
                categoryRepository.save(
                    Category(
                        userId = userId,
                        name = name,
                        sortOrder = index,
                        isSystem = name == "通用" // "通用"设为系统分类
                    )
                )
            }
        }
    }
    
    fun createCategory(userId: String, name: String): Category {
        if (categoryRepository.findByUserIdAndName(userId, name) != null) {
            throw IllegalArgumentException("分类名称已存在")
        }
        
        // 获取当前最大排序值
        val categories = categoryRepository.findByUserIdOrderBySortOrderAsc(userId)
        val maxSortOrder = categories.maxOfOrNull { it.sortOrder } ?: -1
        
        return categoryRepository.save(
            Category(
                userId = userId,
                name = name,
                sortOrder = maxSortOrder + 1
            )
        )
    }
    
    fun getUserCategories(userId: String): List<Category> {
        val categories = categoryRepository.findByUserIdOrderBySortOrderAsc(userId)
        if (categories.isEmpty()) {
            initDefaultCategories(userId)
            return categoryRepository.findByUserIdOrderBySortOrderAsc(userId)
        }
        return categories
    }
    
    fun updateCategory(userId: String, categoryId: String, name: String): Category {
        val category = categoryRepository.findById(categoryId).orElseThrow { 
            IllegalArgumentException("分类不存在") 
        }
        
        if (category.userId != userId) {
            throw IllegalArgumentException("无权操作此分类")
        }
        
        if (category.isSystem && name != category.name) {
            throw IllegalArgumentException("系统分类不允许修改名称")
        }
        
        // 检查重名
        val existing = categoryRepository.findByUserIdAndName(userId, name)
        if (existing != null && existing.id != categoryId) {
            throw IllegalArgumentException("分类名称已存在")
        }
        
        return categoryRepository.save(category.copy(name = name, updatedAt = LocalDateTime.now()))
    }
    
    @Transactional
    fun deleteCategory(userId: String, categoryId: String) {
        val category = categoryRepository.findById(categoryId).orElseThrow {
            IllegalArgumentException("分类不存在")
        }
        
        if (category.userId != userId) {
            throw IllegalArgumentException("无权操作此分类")
        }
        
        if (category.isSystem) {
            throw IllegalArgumentException("系统分类不允许删除")
        }
        
        // 查找"通用"分类作为转移目标
        val generalCategory = categoryRepository.findByUserIdAndName(userId, "通用")
            ?: throw IllegalStateException("未找到通用分类，无法转移Prompt")
            
        // 转移Prompt
        // 1. 通过ID查找（针对新创建的Prompt）
        val promptsById = promptRepository.findByUserIdAndCategoryId(userId, categoryId)
        promptsById.forEach { prompt ->
            promptRepository.save(prompt.copy(
                categoryId = generalCategory.id,
                category = generalCategory.name,
                updatedAt = LocalDateTime.now()
            ))
        }
        
        // 2. 通过名称查找（针对旧数据的兼容处理）
        val promptsByName = promptRepository.findByUserIdAndCategory(userId, category.name)
        promptsByName.forEach { prompt ->
            // 避免重复处理
            if (prompt.categoryId != generalCategory.id) {
                promptRepository.save(prompt.copy(
                    categoryId = generalCategory.id,
                    category = generalCategory.name,
                    updatedAt = LocalDateTime.now()
                ))
            }
        }
        
        categoryRepository.delete(category)
    }
}
