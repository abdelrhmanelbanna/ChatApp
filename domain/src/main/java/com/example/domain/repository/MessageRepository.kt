package com.example.domain.repository

import com.example.domain.entity.Message
import com.example.domain.entity.MessageStatus
import com.example.domain.utils.ResultWrapper
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    suspend fun sendMessage(message: Message, mediaUris: List<String>? = null): ResultWrapper<Unit>
    fun observeMessages(): Flow<List<Message>>
    suspend fun retryMessage(message: Message): ResultWrapper<Unit>

    fun observeMessageStatus(messageId: String): Flow<MessageStatus>


    suspend fun updateMessageStatus(messageId: String, status: MessageStatus)

}