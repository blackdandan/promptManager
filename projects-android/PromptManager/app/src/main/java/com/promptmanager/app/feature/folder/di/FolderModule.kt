package com.promptmanager.app.feature.folder.di

import com.promptmanager.app.core.network.service.FolderService
import com.promptmanager.app.feature.folder.data.FolderRepositoryImpl
import com.promptmanager.app.feature.folder.domain.FolderRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FolderModule {

    @Binds
    abstract fun bindFolderRepository(
        folderRepositoryImpl: FolderRepositoryImpl
    ): FolderRepository

    companion object {
        @Provides
        @Singleton
        fun provideFolderService(retrofit: Retrofit): FolderService {
            return retrofit.create(FolderService::class.java)
        }
    }
}
