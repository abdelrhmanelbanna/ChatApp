package com.example.domain.repository

import com.example.domain.entity.User
import com.example.domain.utils.ResultWrapper

interface UserRepository {
    suspend fun saveUser(user: User): ResultWrapper<Unit>
    suspend fun getUser(): ResultWrapper<User>
}