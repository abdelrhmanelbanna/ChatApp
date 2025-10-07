package com.example.data.di

import android.content.Context
import com.example.data.datasource.MessageDatasource
import com.example.data.datasource.UserDatasource
import com.example.data.datasourceImpl.MessageDatasourceImpl
import com.example.data.datasourceImpl.UserDatasourceImpl
import com.example.data.repository.MessageRepositoryImpl
import com.example.data.repository.UserRepositoryImpl
import com.example.data.webservice.SupabaseClientProvider
import com.example.domain.repository.MessageRepository
import com.example.domain.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.status.SessionSource
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {


    @Provides
    @Singleton
    fun provideStorage(supabaseClient: SupabaseClient): Storage {
        return supabaseClient.storage
    }




    @Provides
    @Singleton
    @Named("supabaseUrl")
    fun provideSupabaseUrl(): String = "https://yanmwvuaweddcsvewmln.supabase.co"

    @Provides
    @Singleton
    @Named("supabaseKey")
    fun provideSupabaseKey(): String = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inlhbm13dnVhd2VkZGNzdmV3bWxuIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTkyNTA3MTIsImV4cCI6MjA3NDgyNjcxMn0.NKgxhcs5XWz4S4wZM_6YLwu2mtfkdU7Pkoy1g8gjZvs"

    @Provides
    @Singleton
    fun provideSupabaseClientProvider(
        @ApplicationContext context: Context,
        @Named("supabaseUrl") supabaseUrl: String,
        @Named("supabaseKey") supabaseKey: String
    ): SupabaseClientProvider {
        return SupabaseClientProvider(context, supabaseUrl, supabaseKey)
    }

    @Provides
    @Singleton
    fun provideSupabaseClient(supabaseClientProvider: SupabaseClientProvider): SupabaseClient {
        return supabaseClientProvider.client
    }

    @Provides
    @Singleton
    fun provideMessageDatasource(
        supabaseClientProvider: SupabaseClientProvider,
        @ApplicationContext context: Context
    ): MessageDatasource {
        return MessageDatasourceImpl(supabaseClientProvider, context)
    }

    @Provides
    @Singleton
    fun provideUserDatasource(
        supabaseClientProvider: SupabaseClientProvider,
        storage: Storage,
        @ApplicationContext context: Context
    ): UserDatasource {
        return UserDatasourceImpl(supabaseClientProvider, storage , context)
    }

    @Provides
    @Singleton
    fun provideMessageRepository(messageDatasource: MessageDatasource): MessageRepository {
        return MessageRepositoryImpl(messageDatasource)
    }

    @Provides
    @Singleton
    fun provideUserRepository(userDatasource: UserDatasource): UserRepository {
        return UserRepositoryImpl(userDatasource)
    }
}