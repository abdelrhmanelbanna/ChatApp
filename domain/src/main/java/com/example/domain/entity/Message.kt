package com.example.domain.entity

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val id: String,
    val userId: String,
    val username: String,
    val profileImage: String,
    val content: String? = null,
    val mediaUrls: List<String> = emptyList(),
    val audioUrl: String?=null,
    val createdAt: Long,
    val status: MessageStatus = MessageStatus.SENDING
)
@Serializable
enum class MessageStatus {
    SENDING, SENT, FAILED
}