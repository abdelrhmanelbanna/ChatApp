package com.example.domain.validator

import com.example.domain.entity.Message
import com.example.domain.exceptions.DomainException
import com.example.domain.utils.ResultWrapper

object MessageValidator {

    fun validate(message: Message): ResultWrapper<Unit> {

        if (message.mediaUrls.size > 10) {
            return ResultWrapper.Error(DomainException.MediaLimitExceededException())
        }

        if (message.content.isNullOrBlank() && message.mediaUrls.isEmpty()) {
            return ResultWrapper.Error(DomainException.EmptyMessageException())
        }

        return ResultWrapper.Success(Unit)
    }


}