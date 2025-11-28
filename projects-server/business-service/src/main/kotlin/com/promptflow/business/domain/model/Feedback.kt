package com.promptflow.business.domain.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.LocalDateTime

@Document(collection = "feedbacks")
data class Feedback(
    @Id
    val id: String? = null,
    
    @Field("user_id")
    val userId: String,
    
    @Field("type")
    val type: String, // bug, suggestion, other
    
    @Field("content")
    val content: String,
    
    @Field("contact")
    val contact: String? = null,
    
    @Field("status")
    val status: String = "PENDING", // PENDING, PROCESSED
    
    @Field("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
)
