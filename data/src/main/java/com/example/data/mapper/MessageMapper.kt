package com.example.data.mapper

import com.example.data.model.MessageDto
import com.example.data.model.MessageStatusDto
import com.example.domain.entity.Message
import com.example.domain.entity.MessageStatus

object MessageMapper {

    fun toDomain(dto: MessageDto): Message = Message(
        id = dto.id,
        userId = dto.user_id,
        username = dto.username,
        profileImage = dto.profile_image,
        content = dto.content,
        mediaUrls = dto.media_urls,
        audioUrl = dto.audio_url,
        createdAt = dto.created_at,
        status = when (dto.status) {
            MessageStatusDto.SENDING -> MessageStatus.SENDING
            MessageStatusDto.SENT -> MessageStatus.SENT
            MessageStatusDto.FAILED -> MessageStatus.FAILED
        }
    )

    fun toDto(domain: Message): MessageDto = MessageDto(
        id = domain.id,
        user_id = domain.userId,
        username = domain.username,
        profile_image = domain.profileImage,
        content = domain.content,
        media_urls = domain.mediaUrls,
        audio_url = domain.audioUrl,
        created_at = domain.createdAt,
        status = when (domain.status) {
            MessageStatus.SENDING -> MessageStatusDto.SENDING
            MessageStatus.SENT -> MessageStatusDto.SENT
            MessageStatus.FAILED -> MessageStatusDto.FAILED
        }
    )
}