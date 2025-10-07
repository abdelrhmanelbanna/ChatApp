package com.example.chatapp.chatScreen

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.core.datastore.DataStoreManager
import com.example.domain.entity.Message
import com.example.domain.entity.MessageStatus
import com.example.domain.usecase.GetUserUseCase
import com.example.domain.usecase.ObserveMessagesUseCase
import com.example.domain.usecase.RetryMessageUseCase
import com.example.domain.usecase.SendMessageUseCase
import com.example.domain.utils.ResultWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import dagger.hilt.android.qualifiers.ApplicationContext

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val observeMessagesUseCase: ObserveMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val retryMessageUseCase: RetryMessageUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val dataStoreManager: DataStoreManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    private val _effect = Channel<ChatEffect>(Channel.BUFFERED)
    val effects = _effect.receiveAsFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    private val currentUser: User? get() = _currentUser.value

    init {
        viewModelScope.launch {
            // Get current user from DataStore + UserRepository
            dataStoreManager.getUserId().collectLatest { userId ->
                val uid = userId ?: return@collectLatest

                val user = when (val result = getUserUseCase(uid)) {
                    is ResultWrapper.Success -> result.data as? User ?: User(uid, "You", "")
                    else -> User(uid, "You", "")
                }

                _currentUser.value = user
                // Update state with current user id
                _state.update { it.copy(currentUserId = user.id) }
            }
        }

        startObservingMessages()
    }

    private fun startObservingMessages() {
        viewModelScope.launch {
            observeMessagesUseCase().collect { messages ->
                _state.update { it.copy(messages = messages) }
            }
        }
    }

    fun process(intent: ChatIntent) {
        when (intent) {
            is ChatIntent.Start -> { /* do nothing */ }

            is ChatIntent.UpdateComposingText -> {
                _state.update { it.copy(composingText = intent.text) }
            }

            is ChatIntent.PickImagesClicked -> {
                viewModelScope.launch { _effect.send(ChatEffect.OpenImagePicker) }
            }

            is ChatIntent.ImagesSelected -> {
                val limited = intent.uris.take(10)
                _state.update { it.copy(selectedMediaUris = limited) }
                if (intent.uris.size > 10) {
                    viewModelScope.launch { _effect.send(ChatEffect.ShowToast("يمكن إرسال حتى 10 صور فقط")) }
                }
            }

            is ChatIntent.SendText -> {
                val text = intent.text.trim()
                viewModelScope.launch {
                    val user = currentUser ?: run {
                        _effect.send(ChatEffect.ShowToast("User not loaded"))
                        return@launch
                    }

                    if (text.isEmpty() && _state.value.selectedMediaUris.isEmpty()) {
                        _effect.send(ChatEffect.ShowToast("لا يمكن إرسال رسالة فارغة"))
                        return@launch
                    }

                    val message = Message(
                        id = UUID.randomUUID().toString(),
                        userId = user.id,
                        username = user.username,
                        profileImage = user.profileImage,
                        content = if (text.isBlank()) null else text,
                        mediaUrls = _state.value.selectedMediaUris,
                        audioUrl = null,
                        createdAt = System.currentTimeMillis(),
                        status = MessageStatus.SENDING
                    )

                    // Optimistic UI update
                    _state.update { old ->
                        old.copy(
                            messages = old.messages + message,
                            composingText = "",
                            selectedMediaUris = emptyList()
                        )
                    }

                    MessageSendManager.enqueueSend(
                        context = context,
                        message = message,
                        mediaUris = message.mediaUrls
                    )
                }
            }

            is ChatIntent.RetryMessage -> {
                viewModelScope.launch {
                    val message = _state.value.messages.find { it.id == intent.messageId } ?: run {
                        _effect.send(ChatEffect.ShowToast("الرسالة غير موجودة"))
                        return@launch
                    }

                    _state.update { old ->
                        old.copy(messages = old.messages.map {
                            if (it.id == message.id) it.copy(status = MessageStatus.SENDING) else it
                        })
                    }

                    MessageSendManager.enqueueSend(
                        context = context,
                        message = message,
                        mediaUris = message.mediaUrls
                    )
                }
            }
        }
    }
}

// Simple User model
data class User(
    val id: String,
    val username: String,
    val profileImage: String
)
