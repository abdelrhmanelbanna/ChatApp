package com.example.domain.repository

import com.example.domain.entity.Message
import kotlinx.coroutines.flow.Flow

interface MessageRepository {

    suspend fun sendMessage(message: Message): Result<Unit>
    fun observeMessages(): Flow<List<Message>>
    suspend fun retryMessage(message: Message): Result<Unit>

}