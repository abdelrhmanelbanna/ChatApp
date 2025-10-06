package com.example.chatapp.profileScreen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.core.datastore.DataStoreManager
import com.example.domain.entity.User
import com.example.domain.usecase.SaveUserUseCase
import com.example.domain.usecase.UploadProfileImageUseCase
import com.example.domain.utils.ResultWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val saveUserUseCase: SaveUserUseCase,
    private val uploadProfileImageUseCase: UploadProfileImageUseCase,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state

    fun handleIntent(intent: ProfileIntent) {
        when (intent) {
            is ProfileIntent.EnterUsername -> _state.update { it.copy(username = intent.username) }
            is ProfileIntent.PickImage -> uploadImage(intent.uri)
            is ProfileIntent.SaveUser -> saveUser()
        }
    }

    private fun uploadImage(uri: String) {
        viewModelScope.launch {

            Log.e("ProfileViewModel", "Uploading image with URI: $uri")

            _state.update { it.copy(isLoading = true, error = null) }

            when (val result = uploadProfileImageUseCase(uri)) {
                is ResultWrapper.Success -> {
                    Log.e("ProfileViewModel", "Image uploaded successfully")
                    _state.update { it.copy(imageUri = result.data, isLoading = false) }
                }
                is ResultWrapper.Error -> {
                    Log.e("ProfileViewModel", "Error uploading image: ${result.exception.message}")
                    _state.update { it.copy(isLoading = false, error = result.exception.message) }
                }
                else -> {}
            }
        }
    }

    private fun saveUser() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val userId = UUID.randomUUID().toString()
            val user = User(
                id = userId,
                username = _state.value.username,
                profileImage = _state.value.imageUri ?: ""
            )

            when (val result = saveUserUseCase(user)) {
                is ResultWrapper.Success -> {
                    dataStoreManager.saveUserId(userId)
                    _state.update { it.copy(isLoading = false, isSaved = true) }
                }
                is ResultWrapper.Error -> {
                    _state.update { it.copy(isLoading = false, error = result.exception.message) }
                }
                else -> {}
            }
        }
    }
}
