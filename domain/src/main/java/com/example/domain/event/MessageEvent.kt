package com.example.domain.event

import com.example.domain.entity.Message
import com.example.domain.exceptions.DomainException

sealed class MessageEvent {
    data class Sent(val message: Message) : MessageEvent()
    data class Failed(val message: Message, val reason: DomainException) : MessageEvent()
}