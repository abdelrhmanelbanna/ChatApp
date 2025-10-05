package com.example.app.di

import com.example.domain.repository.MessageRepository
import com.example.domain.repository.UserRepository
import com.example.domain.usecase.*

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {

    @Provides
    fun provideGetUserUseCase(
        userRepository: UserRepository
    ): GetUserUseCase = GetUserUseCase(userRepository)

    @Provides
    fun provideSaveUserUseCase(
        userRepository: UserRepository
    ): SaveUserUseCase = SaveUserUseCase(userRepository)

    @Provides
    fun provideObserveMessagesUseCase(
        messageRepository: MessageRepository
    ): ObserveMessagesUseCase = ObserveMessagesUseCase(messageRepository)

    @Provides
    fun provideSendMessageUseCase(
        messageRepository: MessageRepository
    ): SendMessageUseCase = SendMessageUseCase(messageRepository)

    @Provides
    fun provideRetryMessageUseCase(
        messageRepository: MessageRepository
    ): RetryMessageUseCase = RetryMessageUseCase(messageRepository)
}
