package com.example.domain.usecase

import com.example.domain.repository.UserRepository
import com.example.domain.utils.ResultWrapper

class UploadProfileImageUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(uri: String): ResultWrapper<String> {
        println("UploadProfileImageUseCase: Invoking with uri=$uri")
        println("UploadProfileImageUseCase: Repository result -> ${repository.uploadProfileImage(uri)}")
        return repository.uploadProfileImage(uri)
    }
}