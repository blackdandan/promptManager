package com.promptmanager.app.core.database.di

import android.content.Context
import androidx.room.Room
import com.promptmanager.app.core.database.AppDatabase
import com.promptmanager.app.core.database.dao.PromptDao
import com.promptmanager.app.core.database.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "prompt_manager.db"
        ).build()
    }

    @Provides
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    fun providePromptDao(database: AppDatabase): PromptDao {
        return database.promptDao()
    }
}
