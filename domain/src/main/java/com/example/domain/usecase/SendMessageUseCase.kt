package com.example.domain.usecase

import com.example.domain.entity.Message
import com.example.domain.repository.MessageRepository
import com.example.domain.utils.ResultWrapper
import com.example.domain.validator.MessageValidator



class SendMessageUseCase(private val repository: MessageRepository) {

    suspend operator fun invoke(message: Message, mediaUris: List<String>? = null): ResultWrapper<Unit> {
        println("SendMessageUseCase: Invoking with message id=${message.id}, content=${message.content}, mediaUris=$mediaUris")

        val validation = MessageValidator.validate(message)
        if (validation is ResultWrapper.Error) {
            println("SendMessageUseCase: Validation failed for message id=${message.id}")
            return validation
        }

        val result = repository.sendMessage(message, mediaUris)
        println("SendMessageUseCase: Repository result for message id=${message.id} -> $result")
        return result
    }
}

