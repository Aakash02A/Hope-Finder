package com.hope_finder.di

import com.hope_finder.data.repository.AlertRepository
import com.hope_finder.data.repository.AlertRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AlertModule {

    @Provides
    @Singleton
    fun provideAlertRepository(impl: AlertRepositoryImpl): AlertRepository = impl
}
