package com.example.domain.usecase

import com.example.domain.entity.User
import com.example.domain.repository.UserRepository

class GetUserUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(): User? = repository.getUser()
}