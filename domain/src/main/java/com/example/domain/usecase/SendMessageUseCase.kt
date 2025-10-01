package com.example.domain.usecase

import com.example.domain.entity.Message
import com.example.domain.repository.MessageRepository

class SendMessageUseCase(private val repository: MessageRepository) {
    suspend operator fun invoke(message: Message): Result<Unit> {

        if (message.mediaUrls.size > 10) {
            return Result.failure(IllegalArgumentException("Cannot send more than 10 media items"))
        }
        return repository.sendMessage(message)
    }
}