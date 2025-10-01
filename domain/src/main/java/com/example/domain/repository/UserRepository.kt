package com.example.domain.repository

import com.example.domain.entity.User

interface UserRepository {

    suspend fun saveUser(user: User)
    suspend fun getUser(): User?

}