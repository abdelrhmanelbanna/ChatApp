package com.example.domain.usecase

import com.example.domain.entity.Message
import com.example.domain.repository.MessageRepository
import com.example.domain.utils.ResultWrapper
import com.example.domain.validator.MessageValidator

class RetryMessageUseCase(private val repository: MessageRepository) {

    suspend operator fun invoke(message: Message): ResultWrapper<Unit> {

        val validationResult = MessageValidator.validate(message)

        if (validationResult is ResultWrapper.Error) {
            return validationResult
        }


        return repository.retryMessage(message)
    }


}