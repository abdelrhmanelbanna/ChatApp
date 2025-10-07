package com.example.data.datasource

import com.example.data.model.MessageDto
import com.example.data.model.MessageStatusDto
import com.example.domain.entity.MessageStatus
import com.example.domain.utils.ResultWrapper
import kotlinx.coroutines.flow.Flow

interface MessageDatasource {

    suspend fun sendMessage(message: MessageDto, mediaUris: List<String>?): ResultWrapper<Unit>
    fun observeMessages(): Flow<List<MessageDto>>
    suspend fun retryMessage(message: MessageDto): ResultWrapper<Unit>
    fun observeMessageStatus(messageId: String): Flow<MessageStatusDto>

    suspend fun updateMessageStatus(messageId: String, status: MessageStatus)


}