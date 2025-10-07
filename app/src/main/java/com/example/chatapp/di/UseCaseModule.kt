package com.example.app.di

import com.example.domain.repository.MessageRepository
import com.example.domain.repository.UserRepository
import com.example.domain.usecase.*

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
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

    @Provides
    fun provideUploadProfileImageUseCase(
        userRepository: UserRepository
    ): UploadProfileImageUseCase = UploadProfileImageUseCase(userRepository)

    @Provides
    fun provideUpdateMessageStatusUseCase(
        messageRepository: MessageRepository
    ): UpdateMessageStatusUseCase = UpdateMessageStatusUseCase(messageRepository)

}
