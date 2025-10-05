package com.example.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class MessageStatusDto {
    SENDING, SENT, FAILED
}
