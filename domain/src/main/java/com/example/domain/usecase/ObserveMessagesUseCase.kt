package com.example.domain.usecase

import com.example.domain.entity.Message
import com.example.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow

class ObserveMessagesUseCase(private val repository: MessageRepository) {
    operator fun invoke(): Flow<List<Message>> = repository.observeMessages()
}