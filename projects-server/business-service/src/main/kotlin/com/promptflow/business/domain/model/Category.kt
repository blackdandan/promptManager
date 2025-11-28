package com.promptflow.business.domain.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.LocalDateTime

@Document(collection = "categories")
data class Category(
    @Id
    val id: String? = null,
    
    @Field("user_id")
    val userId: String,
    
    @Field("name")
    val name: String,
    
    @Field("color")
    val color: String? = null,
    
    @Field("sort_order")
    val sortOrder: Int = 0,
    
    @Field("is_system")
    val isSystem: Boolean = false, // 系统预置分类，不可删除
    
    @Field("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Field("updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
