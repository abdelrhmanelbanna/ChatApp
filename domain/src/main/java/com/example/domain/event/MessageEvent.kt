package com.example.domain.event

import com.example.domain.entity.Message
import com.example.domain.exceptions.DomainException

sealed class MessageEvent {
    data class Sending(val message: Message, val notificationId: Int? = null) : MessageEvent()
    data class Sent(val message: Message) : MessageEvent()
    data class Failed(val message: Message, val reason: DomainException) : MessageEvent()
}