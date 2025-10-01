package com.example.domain.entity

import java.time.LocalDateTime

data class Message(
    val id: String,
    val userId: String,
    val username: String,
    val profileImage: String,
    val content: String? = null,
    val mediaUrls: List<String> = emptyList(),
    val createdAt: LocalDateTime,
    val status: MessageStatus = MessageStatus.SENDING
)
enum class MessageStatus {
    SENDING, SENT, FAILED
}