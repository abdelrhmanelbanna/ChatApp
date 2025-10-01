package com.example.domain.usecase

import com.example.domain.entity.User
import com.example.domain.repository.UserRepository
import com.example.domain.utils.ResultWrapper

class GetUserUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(): ResultWrapper<User> = repository.getUser()
}