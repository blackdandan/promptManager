package com.promptflow.business.infrastructure.repository

import com.promptflow.business.domain.model.Folder
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface FolderRepository : MongoRepository<Folder, ObjectId> {
    
    fun findByUserIdAndIsDeletedFalse(userId: ObjectId): List<Folder>
    
    fun findByUserIdAndParentIdAndIsDeletedFalse(userId: ObjectId, parentId: ObjectId?): List<Folder>
    
    fun findByUserIdAndNameAndIsDeletedFalse(userId: ObjectId, name: String): Folder?
    
    fun findByUserIdAndNameAndParentIdAndIsDeletedFalse(userId: ObjectId, name: String, parentId: ObjectId?): Folder?

    fun findByUserIdAndIdAndIsDeletedFalse(userId: ObjectId, id: ObjectId): Folder?
    
    @Query("{ 'userId': ?0, 'isDeleted': false, 'name': { \$regex: ?1, \$options: 'i' } }")
    fun findByUserIdAndNameContainingIgnoreCaseAndIsDeletedFalse(userId: ObjectId, name: String): List<Folder>
    
    fun countByUserIdAndParentIdAndIsDeletedFalse(userId: ObjectId, parentId: ObjectId?): Int
    
    fun findByUserIdAndIsDeletedFalseOrderByOrderAsc(userId: ObjectId): List<Folder>
}
