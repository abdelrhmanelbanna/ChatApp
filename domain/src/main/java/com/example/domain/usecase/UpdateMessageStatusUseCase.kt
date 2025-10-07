package com.example.domain.usecase

import com.example.domain.entity.MessageStatus
import com.example.domain.repository.MessageRepository

class UpdateMessageStatusUseCase(
    private val repository: MessageRepository
) {
    suspend operator fun invoke(messageId: String, status: MessageStatus) {
        repository.updateMessageStatus(messageId, status)
    }
}