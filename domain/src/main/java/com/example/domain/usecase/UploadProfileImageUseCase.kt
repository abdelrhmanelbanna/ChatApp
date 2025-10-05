package com.example.domain.usecase

import com.example.domain.repository.UserRepository
import com.example.domain.utils.ResultWrapper

class UploadProfileImageUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(uri: String): ResultWrapper<String> {
        return repository.uploadProfileImage(uri)
    }
}