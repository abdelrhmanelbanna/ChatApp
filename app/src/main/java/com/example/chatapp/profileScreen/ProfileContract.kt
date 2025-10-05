package com.example.chatapp.profileScreen

sealed class ProfileIntent {
    data class EnterUsername(val username: String) : ProfileIntent()
    data class PickImage(val uri: String) : ProfileIntent()
    object SaveUser : ProfileIntent()
}

data class ProfileState(
    val username: String = "",
    val imageUri: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false
)