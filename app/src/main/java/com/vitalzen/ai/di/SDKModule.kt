package com.vitalzen.ai.di

import com.vitalzen.ai.core.sdk.SmartSpectraSDK
import com.vitalzen.ai.core.sdk.SmartSpectraSDKMock
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SDKModule {

    @Binds
    @Singleton
    abstract fun bindSmartSpectraSDK(
        sdkMock: SmartSpectraSDKMock
    ): SmartSpectraSDK
}
