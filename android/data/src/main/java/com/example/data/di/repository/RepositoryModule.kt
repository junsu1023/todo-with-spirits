package com.example.data.di.repository

import com.example.data.datasource.CheckSystemHealthRemoteDataSource
import com.example.data.repository.CheckSystemHealthRepositoryImpl
import com.example.domain.repository.CheckSystemHealthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideHealthCheckRepository(
        checkSystemHealthRemoteDataSource: CheckSystemHealthRemoteDataSource
    ): CheckSystemHealthRepository = CheckSystemHealthRepositoryImpl(checkSystemHealthRemoteDataSource)
}