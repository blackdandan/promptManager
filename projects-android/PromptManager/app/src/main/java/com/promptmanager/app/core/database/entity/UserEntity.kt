package com.promptmanager.app.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val userId: String,
    val username: String,
    val email: String,
    val displayName: String,
    val userType: String,
    // Store roles as JSON string or simpler format if needed, 
    // but for simple cases maybe just store main role or ignore if not critical for offline
    // For now, let's keep it simple. If we need List<String>, we need a TypeConverter.
    // I'll skip roles for now or add TypeConverter later.
    // Let's create a TypeConverter for List<String> to be safe.
    val roles: List<String>
)
