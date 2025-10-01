package com.example.domain.usecase

import com.example.domain.entity.User
import com.example.domain.repository.UserRepository
import com.example.domain.utils.ResultWrapper

class SaveUserUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(user: User): ResultWrapper<Unit> = repository.saveUser(user)
}