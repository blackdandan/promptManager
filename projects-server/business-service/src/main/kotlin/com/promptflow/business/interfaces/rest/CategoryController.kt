package com.promptflow.business.interfaces.rest

import com.promptflow.business.application.service.CategoryService
import com.promptflow.business.domain.model.Category
import com.promptflow.business.interfaces.rest.dto.CreateCategoryRequest
import com.promptflow.business.interfaces.rest.dto.UpdateCategoryRequest
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/categories")
class CategoryController(
    private val categoryService: CategoryService
) {
    
    @GetMapping
    fun getCategories(@RequestHeader("X-User-Id") userId: String): List<Category> {
        return categoryService.getUserCategories(userId)
    }
    
    @PostMapping
    fun createCategory(
        @RequestHeader("X-User-Id") userId: String,
        @RequestBody request: CreateCategoryRequest
    ): Category {
        return categoryService.createCategory(userId, request.name)
    }
    
    @PutMapping("/{id}")
    fun updateCategory(
        @RequestHeader("X-User-Id") userId: String,
        @PathVariable id: String,
        @RequestBody request: UpdateCategoryRequest
    ): Category {
        return categoryService.updateCategory(userId, id, request.name)
    }
    
    @DeleteMapping("/{id}")
    fun deleteCategory(
        @RequestHeader("X-User-Id") userId: String,
        @PathVariable id: String
    ) {
        categoryService.deleteCategory(userId, id)
    }
}
