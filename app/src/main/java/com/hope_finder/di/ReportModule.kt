package com.hope_finder.di

import com.hope_finder.data.repository.ReportRepository
import com.hope_finder.data.repository.ReportRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ReportModule {

    @Provides
    @Singleton
    fun provideReportRepository(impl: ReportRepositoryImpl): ReportRepository = impl
}
