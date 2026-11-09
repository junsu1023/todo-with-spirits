package com.example.data.di.repository

import com.example.core.auth.TokenStorage
import com.example.data.datasource.AuthRemoteDataSource
import com.example.data.datasource.CheckSystemHealthRemoteDataSource
import com.example.data.datasource.NotificationRemoteDataSource
import com.example.data.datasource.SettingRemoteDataSource
import com.example.data.datasource.SpiritRemoteDataSource
import com.example.data.datasource.TaskRemoteDataSource
import com.example.data.repository.AuthRepositoryImpl
import com.example.data.repository.CheckSystemHealthRepositoryImpl
import com.example.data.repository.CurrencyRepositoryImpl
import com.example.data.repository.DecorationRepositoryImpl
import com.example.data.repository.DisplaySettingRepositoryImpl
import com.example.data.repository.NotificationRepositoryImpl
import com.example.data.repository.NotificationSettingRepositoryImpl
import com.example.data.repository.SpiritRepositoryImpl
import com.example.data.repository.TaskRepositoryImpl
import com.example.domain.repository.AuthRepository
import com.example.domain.repository.CheckSystemHealthRepository
import com.example.domain.repository.CurrencyRepository
import com.example.domain.repository.DecorationRepository
import com.example.domain.repository.DisplaySettingRepository
import com.example.domain.repository.NotificationRepository
import com.example.domain.repository.NotificationSettingRepository
import com.example.domain.repository.SpiritRepository
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
        authRemoteDataSource: AuthRemoteDataSource,
        tokenStorage: TokenStorage
    ): AuthRepository = AuthRepositoryImpl(authRemoteDataSource, tokenStorage)

    @Provides
    @Singleton
    fun provideNotificationRepository(
        notificationRemoteDataSource: NotificationRemoteDataSource
    ): NotificationRepository = NotificationRepositoryImpl(notificationRemoteDataSource)

    @Provides
    @Singleton
    fun provideDisplaySettingRepository(
        settingRemoteDataSource: SettingRemoteDataSource
    ): DisplaySettingRepository = DisplaySettingRepositoryImpl(settingRemoteDataSource)

    @Provides
    @Singleton
    fun provideNotificationSettingRepository(
        settingRemoteDataSource: SettingRemoteDataSource
    ): NotificationSettingRepository = NotificationSettingRepositoryImpl(settingRemoteDataSource)

    @Provides
    @Singleton
    fun provideSpiritRepository(
        spiritRemoteDataSource: SpiritRemoteDataSource
    ): SpiritRepository = SpiritRepositoryImpl(spiritRemoteDataSource)

    @Provides
    @Singleton
    fun provideDecorationRepository(
        spiritRemoteDataSource: SpiritRemoteDataSource
    ): DecorationRepository = DecorationRepositoryImpl(spiritRemoteDataSource)

    @Provides
    @Singleton
    fun provideCurrencyRepository(
        spiritRemoteDataSource: SpiritRemoteDataSource
    ): CurrencyRepository = CurrencyRepositoryImpl(spiritRemoteDataSource)
}