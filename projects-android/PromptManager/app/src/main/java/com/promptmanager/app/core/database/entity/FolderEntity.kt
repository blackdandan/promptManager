package com.promptmanager.app.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey
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
)
