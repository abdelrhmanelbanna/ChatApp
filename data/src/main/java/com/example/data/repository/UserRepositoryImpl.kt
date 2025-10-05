package com.example.data.repository

import com.example.data.datasource.UserDatasource
import com.example.data.mapper.UserMapper
import com.example.domain.entity.User
import com.example.domain.repository.UserRepository
import com.example.domain.utils.ResultWrapper
import javax.inject.Inject


class UserRepositoryImpl @Inject constructor(
    private val userDatasource: UserDatasource
) : UserRepository {

    override suspend fun saveUser(user: User): ResultWrapper<Unit> {
        val userDto = UserMapper.toDto(user)
        return userDatasource.saveUser(userDto)
    }

    override suspend fun getUser(): ResultWrapper<User> {

        return when (val result = userDatasource.getUser()) {
            is ResultWrapper.Success -> ResultWrapper.Success(UserMapper.toDomain(result.data))
            is ResultWrapper.Error -> ResultWrapper.Error(result.exception)
            ResultWrapper.Loading -> ResultWrapper.Loading
        }
    }
}
