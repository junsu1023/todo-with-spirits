package com.example.data.di.network

import com.example.core.qualifier.NoAuth
import com.example.data.api.HealthApi
import com.example.data.api.LoginApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NoAuthNetworkModule {
    @NoAuth
    @Provides
    @Singleton
    fun provideNoAuthOkHttpClient(
        httpLoggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @NoAuth
    @Provides
    @Singleton
    fun provideNoAuthRetrofit(
        baseUrl: String,
        @NoAuth okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideHealthApi(
        @NoAuth retrofit: Retrofit
    ): HealthApi = retrofit.create(HealthApi::class.java)

    @Provides
    @Singleton
    fun provideLoginApi(
        @NoAuth retrofit: Retrofit
    ): LoginApi = retrofit.create(LoginApi::class.java)
}