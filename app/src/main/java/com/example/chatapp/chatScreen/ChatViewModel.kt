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
            val storedUsername = dataStoreManager.getUsername().firstOrNull() ?: "User"
            val storedImage = dataStoreManager.getImage().firstOrNull() ?: ""


            dataStoreManager.getUserId().collectLatest { userId ->
                val uid = userId ?: UUID.randomUUID().toString()

                val user = when (val result = getUserUseCase(uid)) {
                    is ResultWrapper.Success -> {
                        val fetchedUser = result.data as? User
                        fetchedUser ?: User(uid, storedUsername, storedImage)
                    }
                    else -> User(uid, storedUsername, storedImage)
                }

                _currentUser.value = user
                _state.update { it.copy(currentUserId = user.id) }
            }
        }

        observeMessages()
    }

    private fun observeMessages() {
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
                    viewModelScope.launch { _effect.send(ChatEffect.ShowToast("You can send up to 10 photos only")) }
                }
            }

            is ChatIntent.SendText -> sendMessage(intent.text)

            is ChatIntent.RetryMessage -> retryMessage(intent.messageId)
        }
    }

    private fun sendMessage(text: String) {
        val user = currentUser ?: run {
            viewModelScope.launch { _effect.send(ChatEffect.ShowToast("User not loaded")) }
            return
        }

        if (text.isEmpty() && _state.value.selectedMediaUris.isEmpty()) {
            viewModelScope.launch { _effect.send(ChatEffect.ShowToast("Empty message cannot be sent")) }
            return
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

        // Enqueue send with callback to update status
        MessageSendManager.enqueueSend(
            context = context,
            message = message,
            mediaUris = message.mediaUrls
        ) { updatedMessage ->
            _state.update { old ->
                old.copy(
                    messages = old.messages.map { if (it.id == updatedMessage.id) updatedMessage else it }
                )
            }
        }
    }

    public fun retryMessage(messageId: String) {
        val message = _state.value.messages.find { it.id == messageId } ?: run {
            viewModelScope.launch { _effect.send(ChatEffect.ShowToast("Message not found")) }
            return
        }

        val updated = message.copy(status = MessageStatus.SENDING)
        _state.update { old ->
            old.copy(messages = old.messages.map { if (it.id == messageId) updated else it })
        }

        MessageSendManager.enqueueSend(
            context = context,
            message = updated,
            mediaUris = updated.mediaUrls
        ) { updatedMessage ->
            _state.update { old ->
                old.copy(
                    messages = old.messages.map { if (it.id == updatedMessage.id) updatedMessage else it }
                )
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
