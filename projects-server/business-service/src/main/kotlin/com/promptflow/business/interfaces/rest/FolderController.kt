package com.promptflow.business.interfaces.rest

import com.promptflow.common.dto.ApiResponse
import com.promptflow.business.application.service.FolderService
import com.promptflow.business.domain.model.Folder
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/folders")
class FolderController(
    private val folderService: FolderService
) {
    
    private val log = LoggerFactory.getLogger(this::class.java)
    
    @PostMapping
    fun createFolder(
        @RequestHeader("X-User-ID") userId: String,
        @RequestBody request: CreateFolderRequest
    ): ResponseEntity<ApiResponse<FolderResponse>> {
        log.info("创建文件夹: userId=$userId, name=${request.name}")
        
        try {
            val userObjectId = ObjectId(userId)
            val parentObjectId = request.parentId?.let { ObjectId(it) }
            
            val folder = folderService.createFolder(userObjectId, request.name, parentObjectId)
            val response = FolderResponse.fromDomain(folder)
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "文件夹创建成功"))
                
        } catch (e: IllegalArgumentException) {
            log.error("创建文件夹失败: ${e.message}")
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("FOLDER_001", e.message ?: "创建文件夹失败"))
        } catch (e: Exception) {
            log.error("创建文件夹失败: ${e.message}")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("SYSTEM_001", "创建文件夹失败"))
        }
    }
    
    @GetMapping
    fun getUserFolders(@RequestHeader("X-User-ID") userId: String): ResponseEntity<ApiResponse<List<FolderResponse>>> {
        log.info("获取用户文件夹列表: userId=$userId")
        
        try {
            val userObjectId = ObjectId(userId)
            val folders = folderService.getUserFolders(userObjectId)
            val response = folders.map { FolderResponse.fromDomain(it) }
            
            return ResponseEntity.ok(ApiResponse.success(response, "获取文件夹列表成功"))
            
        } catch (e: Exception) {
            log.error("获取文件夹列表失败: ${e.message}")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("SYSTEM_002", "获取文件夹列表失败"))
        }
    }
    
    @GetMapping("/{id}")
    fun getFolder(
        @RequestHeader("X-User-ID") userId: String,
        @PathVariable id: String
    ): ResponseEntity<ApiResponse<FolderResponse>> {
        log.info("获取文件夹: userId=$userId, folderId=$id")
        
        try {
            val userObjectId = ObjectId(userId)
            val folderObjectId = ObjectId(id)
            
            val folder = folderService.getFolderById(userObjectId, folderObjectId)
            val response = FolderResponse.fromDomain(folder)
            
            return ResponseEntity.ok(ApiResponse.success(response, "获取文件夹成功"))
            
        } catch (e: IllegalArgumentException) {
            log.error("获取文件夹失败: ${e.message}")
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("FOLDER_002", e.message ?: "文件夹不存在"))
        } catch (e: Exception) {
            log.error("获取文件夹失败: ${e.message}")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("SYSTEM_003", "获取文件夹失败"))
        }
    }
    
    @PutMapping("/{id}")
    fun updateFolder(
        @RequestHeader("X-User-ID") userId: String,
        @PathVariable id: String,
        @RequestBody request: UpdateFolderRequest
    ): ResponseEntity<ApiResponse<FolderResponse>> {
        log.info("更新文件夹: userId=$userId, folderId=$id, name=${request.name}, parentId=${request.parentId}, order=${request.order}")
        
        try {
            val userObjectId = ObjectId(userId)
            val folderObjectId = ObjectId(id)
            val parentObjectId = request.parentId?.let { ObjectId(it) }
            
            // 如果order字段有值，则更新排序，否则更新其他字段
            val folder = if (request.order != null) {
                folderService.updateFolderOrder(userObjectId, folderObjectId, request.order)
            } else {
                folderService.updateFolder(userObjectId, folderObjectId, request.name, parentObjectId)
            }
            
            val response = FolderResponse.fromDomain(folder)
            val message = if (request.order != null) "文件夹排序更新成功" else "文件夹更新成功"
            
            return ResponseEntity.ok(ApiResponse.success(response, message))
            
        } catch (e: IllegalArgumentException) {
            log.error("更新文件夹失败: ${e.message}")
            val errorCode = if (request.order != null) "FOLDER_006" else "FOLDER_003"
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(errorCode, e.message ?: "更新文件夹失败"))
        } catch (e: Exception) {
            log.error("更新文件夹失败: ${e.message}")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("SYSTEM_004", "更新文件夹失败"))
        }
    }
    
    @DeleteMapping("/{id}")
    fun deleteFolder(
        @RequestHeader("X-User-ID") userId: String,
        @PathVariable id: String
    ): ResponseEntity<ApiResponse<Void>> {
        log.info("删除文件夹: userId=$userId, folderId=$id")
        
        try {
            val userObjectId = ObjectId(userId)
            val folderObjectId = ObjectId(id)
            
            folderService.deleteFolder(userObjectId, folderObjectId)
            
            return ResponseEntity.ok(ApiResponse.success(null, "文件夹删除成功"))
            
        } catch (e: IllegalArgumentException) {
            log.error("删除文件夹失败: ${e.message}")
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("FOLDER_004", e.message ?: "删除文件夹失败"))
        } catch (e: Exception) {
            log.error("删除文件夹失败: ${e.message}")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("SYSTEM_005", "删除文件夹失败"))
        }
    }
    
    @GetMapping("/parent/{parentId}")
    fun getFoldersByParentId(
        @RequestHeader("X-User-ID") userId: String,
        @PathVariable parentId: String
    ): ResponseEntity<ApiResponse<List<FolderResponse>>> {
        log.info("获取子文件夹列表: userId=$userId, parentId=$parentId")
        
        try {
            val userObjectId = ObjectId(userId)
            val parentObjectId = if (parentId == "null") null else ObjectId(parentId)
            
            val folders = folderService.getFoldersByParentId(userObjectId, parentObjectId)
            val response = folders.map { FolderResponse.fromDomain(it) }
            
            return ResponseEntity.ok(ApiResponse.success(response, "获取子文件夹列表成功"))
            
        } catch (e: Exception) {
            log.error("获取子文件夹列表失败: ${e.message}")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("SYSTEM_006", "获取子文件夹列表失败"))
        }
    }
    
    @GetMapping("/search")
    fun searchFolders(
        @RequestHeader("X-User-ID") userId: String,
        @RequestParam search: String
    ): ResponseEntity<ApiResponse<List<FolderResponse>>> {
        log.info("搜索文件夹: userId=$userId, search=$search")
        
        try {
            val userObjectId = ObjectId(userId)
            val folders = folderService.searchFolders(userObjectId, search)
            val response = folders.map { FolderResponse.fromDomain(it) }
            
            return ResponseEntity.ok(ApiResponse.success(response, "搜索文件夹成功"))
            
        } catch (e: Exception) {
            log.error("搜索文件夹失败: ${e.message}")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("SYSTEM_007", "搜索文件夹失败"))
        }
    }
    
    @GetMapping("/{id}/stats")
    fun getFolderStats(
        @RequestHeader("X-User-ID") userId: String,
        @PathVariable id: String
    ): ResponseEntity<ApiResponse<Map<String, Any>>> {
        log.info("获取文件夹统计: userId=$userId, folderId=$id")
        
        try {
            val userObjectId = ObjectId(userId)
            val folderObjectId = ObjectId(id)
            
            val stats = folderService.getFolderStats(userObjectId, folderObjectId)
            
            return ResponseEntity.ok(ApiResponse.success(stats, "获取文件夹统计成功"))
            
        } catch (e: IllegalArgumentException) {
            log.error("获取文件夹统计失败: ${e.message}")
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("FOLDER_005", e.message ?: "文件夹不存在"))
        } catch (e: Exception) {
            log.error("获取文件夹统计失败: ${e.message}")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("SYSTEM_008", "获取文件夹统计失败"))
        }
    }
}

data class CreateFolderRequest(
    val name: String,
    val parentId: String? = null
)

data class UpdateFolderRequest(
    val name: String? = null,
    val parentId: String? = null,
    val order: Int? = null
)

data class FolderResponse(
    val id: String,
    val userId: String,
    val name: String,
    val parentId: String?,
    val order: Int,
    val color: String?,
    val icon: String?,
    val description: String?,
    val promptCount: Int,
    val createdAt: String,
    val updatedAt: String
) {
    companion object {
        fun fromDomain(folder: Folder): FolderResponse {
            return FolderResponse(
                id = folder.id?.toString() ?: "",
                userId = folder.userId.toString(),
                name = folder.name,
                parentId = folder.parentId?.toString(),
                order = folder.order,
                color = folder.color,
                icon = folder.icon,
                description = folder.description,
                promptCount = folder.promptCount,
                createdAt = folder.createdAt.toString(),
                updatedAt = folder.updatedAt.toString()
            )
        }
    }
}
