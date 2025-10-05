package com.example.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String,
    val username: String,
    val profile_image: String
)