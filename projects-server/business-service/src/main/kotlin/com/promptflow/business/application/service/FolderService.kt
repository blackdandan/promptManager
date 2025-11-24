package com.promptflow.business.application.service

import com.promptflow.business.domain.model.Folder
import com.promptflow.business.infrastructure.repository.FolderRepository
import com.promptflow.business.infrastructure.repository.PromptRepository
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class FolderService(
    private val folderRepository: FolderRepository,
    private val promptRepository: PromptRepository
) {
    
    private val log = LoggerFactory.getLogger(this::class.java)
    
    fun createFolder(userId: ObjectId, name: String, parentId: ObjectId? = null): Folder {
        log.info("创建文件夹: userId=$userId, name=$name, parentId=$parentId")
        
        // 检查文件夹名称是否重复
        val existingFolder = folderRepository.findByUserIdAndNameAndIsDeletedFalse(userId, name)
        if (existingFolder != null) {
            throw IllegalArgumentException("文件夹名称已存在: $name")
        }
        
        // 获取排序序号
        val order = folderRepository.countByUserIdAndParentIdAndIsDeletedFalse(userId, parentId)
        
        val folder = Folder(
            userId = userId,
            name = name,
            parentId = parentId,
            order = order,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
        
        return folderRepository.save(folder)
    }
    
    fun getUserFolders(userId: ObjectId): List<Folder> {
        log.info("获取用户文件夹列表: userId=$userId")
        return folderRepository.findByUserIdAndIsDeletedFalseOrderByOrderAsc(userId)
    }
    
    fun getFolderById(userId: ObjectId, folderId: ObjectId): Folder {
        log.info("获取文件夹: userId=$userId, folderId=$folderId")
        return folderRepository.findByUserIdAndIdAndIsDeletedFalse(userId, folderId)
            ?: throw IllegalArgumentException("文件夹不存在: $folderId")
    }
    
    fun getFoldersByParentId(userId: ObjectId, parentId: ObjectId?): List<Folder> {
        log.info("获取子文件夹列表: userId=$userId, parentId=$parentId")
        return folderRepository.findByUserIdAndParentIdAndIsDeletedFalse(userId, parentId)
    }
    
    fun updateFolder(userId: ObjectId, folderId: ObjectId, name: String? = null, parentId: ObjectId? = null, order: Int? = null): Folder {
        log.info("更新文件夹: userId=$userId, folderId=$folderId, name=$name, parentId=$parentId, order=$order")
        
        val existingFolder = getFolderById(userId, folderId)
        
        // 检查文件夹名称是否重复（排除当前文件夹）
        if (name != null && name != existingFolder.name) {
            val duplicateFolder = folderRepository.findByUserIdAndNameAndIsDeletedFalse(userId, name)
            if (duplicateFolder != null && duplicateFolder.id != folderId) {
                throw IllegalArgumentException("文件夹名称已存在: $name")
            }
        }
        
        val updatedFolder = existingFolder.copy(
            name = name ?: existingFolder.name,
            parentId = parentId ?: existingFolder.parentId,
            order = order ?: existingFolder.order,
            updatedAt = Instant.now()
        )
        
        return folderRepository.save(updatedFolder)
    }
    
    fun deleteFolder(userId: ObjectId, folderId: ObjectId) {
        log.info("删除文件夹: userId=$userId, folderId=$folderId")
        
        val folder = getFolderById(userId, folderId)
        
        // 检查是否有子文件夹
        val childFolders = getFoldersByParentId(userId, folderId)
        if (childFolders.isNotEmpty()) {
            throw IllegalArgumentException("无法删除包含子文件夹的文件夹")
        }
        
        // 检查文件夹中是否有Prompt
        val prompts = promptRepository.findByUserIdAndFolderId(userId.toString(), folderId.toString())
        if (prompts.isNotEmpty()) {
            throw IllegalArgumentException("无法删除包含Prompt的文件夹")
        }
        
        // 软删除
        val deletedFolder = folder.copy(
            isDeleted = true,
            updatedAt = Instant.now()
        )
        
        folderRepository.save(deletedFolder)
    }
    
    fun searchFolders(userId: ObjectId, search: String): List<Folder> {
        log.info("搜索文件夹: userId=$userId, search=$search")
        return folderRepository.findByUserIdAndNameContainingIgnoreCaseAndIsDeletedFalse(userId, search)
    }
    
    fun updateFolderOrder(userId: ObjectId, folderId: ObjectId, order: Int): Folder {
        log.info("更新文件夹排序: userId=$userId, folderId=$folderId, order=$order")
        
        val folder = getFolderById(userId, folderId)
        val updatedFolder = folder.copy(
            order = order,
            updatedAt = Instant.now()
        )
        
        return folderRepository.save(updatedFolder)
    }
    
    fun getFolderStats(userId: ObjectId, folderId: ObjectId): Map<String, Any> {
        log.info("获取文件夹统计: userId=$userId, folderId=$folderId")
        
        val folder = getFolderById(userId, folderId)
        val prompts = promptRepository.findByUserIdAndFolderId(userId.toString(), folderId.toString())
        val childFolderCount = folderRepository.countByUserIdAndParentIdAndIsDeletedFalse(userId, folderId)
        
        return mapOf(
            "folderId" to folderId.toString(),
            "name" to folder.name,
            "promptCount" to prompts.size,
            "childFolderCount" to childFolderCount,
            "createdAt" to folder.createdAt,
            "updatedAt" to folder.updatedAt
        )
    }
}
