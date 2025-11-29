package com.promptmanager.app.feature.prompt.di

import com.promptmanager.app.core.network.service.PromptService
import com.promptmanager.app.feature.prompt.data.PromptRepositoryImpl
import com.promptmanager.app.feature.prompt.domain.PromptRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PromptModule {

    @Binds
    abstract fun bindPromptRepository(
        promptRepositoryImpl: PromptRepositoryImpl
    ): PromptRepository

    companion object {
        @Provides
        @Singleton
        fun providePromptService(retrofit: Retrofit): PromptService {
            return retrofit.create(PromptService::class.java)
        }
    }
}
