package com.example.domain.validator

import com.example.domain.entity.Message
import com.example.domain.exceptions.DomainException
import com.example.domain.utils.ResultWrapper

object MessageValidator {

    fun validate(message: Message, mediaUris: List<String>? = null): ResultWrapper<Unit> {
        if (mediaUris != null && mediaUris.size > 10) {
            return ResultWrapper.Error(DomainException.MediaLimitExceededException())
        }
        if (message.content.isNullOrBlank() && message.mediaUrls.isEmpty() && (mediaUris == null || mediaUris.isEmpty())) {
            return ResultWrapper.Error(DomainException.EmptyMessageException())
        }
        return ResultWrapper.Success(Unit)
    }


}