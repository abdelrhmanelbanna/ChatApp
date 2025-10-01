package com.example.domain.utils

import com.example.domain.exceptions.DomainException

sealed class ResultWrapper<out T> {
    data class Success<T>(val data: T) : ResultWrapper<T>()
    data class Error(val exception: DomainException) : ResultWrapper<Nothing>()
    object Loading : ResultWrapper<Nothing>()
}