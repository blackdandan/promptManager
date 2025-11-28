package com.promptflow.business.interfaces.rest

import com.promptflow.common.dto.ApiResponse
import com.promptflow.business.application.service.CategoryService
import com.promptflow.business.domain.model.Category
import com.promptflow.business.interfaces.rest.dto.CreateCategoryRequest
import com.promptflow.business.interfaces.rest.dto.UpdateCategoryRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/categories")
class CategoryController(
    private val categoryService: CategoryService
) {
    
    @GetMapping
    fun getCategories(@RequestHeader("X-User-Id") userId: String): ResponseEntity<ApiResponse<List<Category>>> {
        val categories = categoryService.getUserCategories(userId)
        return ResponseEntity.ok(ApiResponse.success(categories, "获取分类列表成功"))
    }
    
    @PostMapping
    fun createCategory(
        @RequestHeader("X-User-Id") userId: String,
        @RequestBody request: CreateCategoryRequest
    ): ResponseEntity<ApiResponse<Category>> {
        return try {
            val category = categoryService.createCategory(userId, request.name)
            ResponseEntity.ok(ApiResponse.success(category, "创建分类成功"))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("CATEGORY_CREATE_FAILED", e.message ?: "创建分类失败"))
        }
    }
    
    @PutMapping("/{id}")
    fun updateCategory(
        @RequestHeader("X-User-Id") userId: String,
        @PathVariable id: String,
        @RequestBody request: UpdateCategoryRequest
    ): ResponseEntity<ApiResponse<Category>> {
        return try {
            val category = categoryService.updateCategory(userId, id, request.name)
            ResponseEntity.ok(ApiResponse.success(category, "更新分类成功"))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("CATEGORY_UPDATE_FAILED", e.message ?: "更新分类失败"))
        }
    }
    
    @DeleteMapping("/{id}")
    fun deleteCategory(
        @RequestHeader("X-User-Id") userId: String,
        @PathVariable id: String
    ): ResponseEntity<ApiResponse<Void>> {
        return try {
            categoryService.deleteCategory(userId, id)
            ResponseEntity.ok(ApiResponse.success(null, "删除分类成功"))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("CATEGORY_DELETE_FAILED", e.message ?: "删除分类失败"))
        }
    }
}
