package com.example.data.mapper

import com.example.data.model.UserDto
import com.example.domain.entity.User

object UserMapper {

    fun toDomain(dto: UserDto): User = User(
        id = dto.id,
        username = dto.username,
        profileImage = dto.profile_image
    )

    fun toDto(domain: User): UserDto = UserDto(
        id = domain.id,
        username = domain.username,
        profile_image = domain.profileImage
    )
}