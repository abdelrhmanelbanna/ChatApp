package com.example.domain.usecase

import com.example.domain.entity.Message
import com.example.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class ObserveMessagesUseCase (private val repository: MessageRepository) {

    operator fun invoke(ascending: Boolean = true): Flow<List<Message>> =
        repository.observeMessages().map { messages ->
            messages.sortedBy { if (ascending) it.createdAt else -it.createdAt }
        }

}