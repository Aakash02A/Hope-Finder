package com.tryout.hopefinder.di

import com.tryout.hopefinder.data.repository.AuthRepositoryImpl
import com.tryout.hopefinder.data.repository.MockRadarRepositoryImpl
import com.tryout.hopefinder.domain.repository.AuthRepository
import com.tryout.hopefinder.domain.repository.RadarRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for binding repository implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindRadarRepository(
        mockRadarRepositoryImpl: MockRadarRepositoryImpl
    ): RadarRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository
}
