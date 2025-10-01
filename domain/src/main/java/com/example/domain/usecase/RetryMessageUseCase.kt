package com.example.domain.usecase

import com.example.domain.entity.Message
import com.example.domain.repository.MessageRepository

class RetryMessageUseCase(private val repository: MessageRepository) {
    suspend operator fun invoke(message: Message): Result<Unit> =
        repository.retryMessage(message)
}