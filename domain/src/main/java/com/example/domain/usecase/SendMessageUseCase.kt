package com.example.domain.usecase

import com.example.domain.entity.Message
import com.example.domain.exceptions.DomainException
import com.example.domain.repository.MessageRepository
import com.example.domain.utils.ResultWrapper
import com.example.domain.validator.MessageValidator

class SendMessageUseCase(private val repository: MessageRepository) {


    suspend operator fun invoke(message: Message): ResultWrapper<Unit> {

        val validation = MessageValidator.validate(message)
        if (validation is ResultWrapper.Error) return validation

        return repository.sendMessage(message)
    }


    }


