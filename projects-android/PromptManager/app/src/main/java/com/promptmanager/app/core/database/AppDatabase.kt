package com.promptmanager.app.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.promptmanager.app.core.database.dao.PromptDao
import com.promptmanager.app.core.database.dao.UserDao
import com.promptmanager.app.core.database.entity.PromptEntity
import com.promptmanager.app.core.database.entity.UserEntity

@Database(
    entities = [UserEntity::class, PromptEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun promptDao(): PromptDao
}
