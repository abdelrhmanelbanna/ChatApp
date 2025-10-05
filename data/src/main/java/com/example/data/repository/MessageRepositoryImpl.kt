package com.example.data.repository

import com.example.data.datasource.MessageDatasource
import com.example.data.mapper.MessageMapper
import com.example.data.model.MessageStatusDto
import com.example.domain.entity.Message
import com.example.domain.entity.MessageStatus
import com.example.domain.exceptions.DomainException
import com.example.domain.repository.MessageRepository
import com.example.domain.utils.ResultWrapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

class MessageRepositoryImpl @Inject constructor(
    private val messageDatasource: MessageDatasource
) : MessageRepository {

    override suspend fun sendMessage(message: Message, mediaUris: List<String>?): ResultWrapper<Unit> {
        val messageDto = MessageMapper.toDto(message)
        return messageDatasource.sendMessage(messageDto, mediaUris)
    }

    override fun observeMessages(): Flow<List<Message>> {
        return messageDatasource.observeMessages().map { dtoList ->
            dtoList.map { MessageMapper.toDomain(it) }
        }
    }

    override suspend fun retryMessage(message: Message): ResultWrapper<Unit> {
        val messageDto = MessageMapper.toDto(message)
        return messageDatasource.retryMessage(messageDto)
    }

    override fun observeMessageStatus(messageId: String): Flow<MessageStatus> {
        return messageDatasource.observeMessageStatus(messageId).map { statusDto ->
            when (statusDto) {
                MessageStatusDto.SENDING -> MessageStatus.SENDING
                MessageStatusDto.SENT -> MessageStatus.SENT
                MessageStatusDto.FAILED -> MessageStatus.FAILED
            }
        }
    }
}