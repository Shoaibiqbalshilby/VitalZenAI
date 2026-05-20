package com.vitalzen.ai.di

import com.vitalzen.ai.data.repository.AICoachRepositoryImpl
import com.vitalzen.ai.data.repository.AuthRepositoryImpl
import com.vitalzen.ai.data.repository.SettingsRepositoryImpl
import com.vitalzen.ai.data.repository.VitalsRepositoryImpl
import com.vitalzen.ai.domain.repository.AICoachRepository
import com.vitalzen.ai.domain.repository.AuthRepository
import com.vitalzen.ai.domain.repository.SettingsRepository
import com.vitalzen.ai.domain.repository.VitalsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindVitalsRepository(
        vitalsRepositoryImpl: VitalsRepositoryImpl
    ): VitalsRepository

    @Binds
    @Singleton
    abstract fun bindAICoachRepository(
        aiCoachRepositoryImpl: AICoachRepositoryImpl
    ): AICoachRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        settingsRepositoryImpl: SettingsRepositoryImpl
    ): SettingsRepository
}
