package com.example.domain.usecase

import com.example.domain.entity.User
import com.example.domain.repository.UserRepository

class SaveUserUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(user: User) = repository.saveUser(user)
}