package com.hope_finder.di

import com.hope_finder.data.repository.ProbeRepository
import com.hope_finder.data.repository.ProbeRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ProbeModule {

    @Provides
    @Singleton
    fun provideProbeRepository(impl: ProbeRepositoryImpl): ProbeRepository = impl
}
