package com.promptflow.business.domain.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "folders")
data class Folder(
    @Id val id: ObjectId? = null,
    val userId: ObjectId,                // 用户ID
    val name: String,                    // 文件夹名称
    val parentId: ObjectId? = null,      // 父文件夹ID（支持多级文件夹）
    val order: Int = 0,                  // 排序序号
    val color: String? = null,           // 文件夹颜色
    val icon: String? = null,            // 文件夹图标
    val description: String? = null,     // 文件夹描述
    val promptCount: Int = 0,            // 包含的prompt数量
    val isDeleted: Boolean = false,      // 是否删除
    val syncStatus: SyncStatus = SyncStatus.SYNCED, // 同步状态
    val createdAt: Instant = Instant.now(), // 创建时间
    val updatedAt: Instant = Instant.now()  // 更新时间
)

enum class SyncStatus {
    SYNCED,             // 已同步
    PENDING_CREATE,     // 待创建
    PENDING_UPDATE,     // 待更新
    PENDING_DELETE      // 待删除
}
