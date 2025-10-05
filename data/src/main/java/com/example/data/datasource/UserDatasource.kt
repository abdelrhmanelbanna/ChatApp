package com.example.data.datasource

import com.example.data.model.UserDto
import com.example.domain.utils.ResultWrapper

interface UserDatasource {

    suspend fun saveUser(user: UserDto): ResultWrapper<Unit>

    suspend fun getUser(): ResultWrapper<UserDto>

    suspend fun uploadProfileImage(uri: String): ResultWrapper<String>

}