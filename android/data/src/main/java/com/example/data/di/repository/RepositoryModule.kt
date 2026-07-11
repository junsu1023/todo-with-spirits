package com.example.data.di.repository

import com.example.data.datasource.CheckSystemHealthRemoteDataSource
import com.example.data.datasource.LoginRemoteDataSource
import com.example.data.datasource.TaskRemoteDataSource
import com.example.data.repository.CheckSystemHealthRepositoryImpl
import com.example.data.repository.LoginRepositoryImpl
import com.example.data.repository.TaskRepositoryImpl
import com.example.domain.repository.CheckSystemHealthRepository
import com.example.domain.repository.LoginRepository
import com.example.domain.repository.TaskRepository
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

    @Provides
    @Singleton
    fun provideTaskRepository(
        taskRemoteDataSource: TaskRemoteDataSource
    ): TaskRepository = TaskRepositoryImpl(taskRemoteDataSource)

    @Provides
    @Singleton
    fun provideAuthRepository(
        loginRemoteDataSource: LoginRemoteDataSource
    ): LoginRepository = LoginRepositoryImpl(loginRemoteDataSource)
}