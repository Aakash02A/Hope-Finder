package com.hope_finder.di

import com.hope_finder.data.repository.RadarRepository
import com.hope_finder.data.repository.RadarRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RadarModule {

    @Provides
    @Singleton
    fun provideRadarRepository(impl: RadarRepositoryImpl): RadarRepository = impl
}
