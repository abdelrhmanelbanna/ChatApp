package com.example.data.model

import kotlinx.serialization.Serializable


@Serializable
data class MessageDto(
    val id: String,
    val user_id: String,
    val username: String,
    val profile_image: String,
    val content: String? = null,
    val media_urls: List<String> = emptyList(),
    val audio_url: String? = null,
    val created_at: Long,
    val status: MessageStatusDto = MessageStatusDto.SENDING
)
