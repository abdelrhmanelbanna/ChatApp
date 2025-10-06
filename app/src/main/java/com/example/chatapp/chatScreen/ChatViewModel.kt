// package com.example.chatapp.chatScreen
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
import android.content.Context as AppContext
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

    // current user as StateFlow to always get latest value
    private val _currentUser = MutableStateFlow<User?>(null)
    private val currentUser: User? get() = _currentUser.value

    init {
        viewModelScope.launch {
            // collect user id from DataStore and fetch user details
            dataStoreManager.getUserId().collect { id ->
                val safeId = id ?: UUID.randomUUID().toString()

                val user: User = when (val r = getUserUseCase()) {
                    is ResultWrapper.Success -> r.data as? User ?: User(safeId, "You", "")
                    else -> User(safeId, "You", "")
                }

                _currentUser.value = user
            }


            startObservingMessages()
        }
    }

    private fun startObservingMessages() {
        viewModelScope.launch {
            observeMessagesUseCase().collect { list ->
                _state.update { it.copy(messages = list) }
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
                    if (text.isEmpty() && _state.value.selectedMediaUris.isEmpty()) {
                        _effect.send(ChatEffect.ShowToast("لا يمكن إرسال رسالة فارغة"))
                        return@launch
                    }

                    val user = currentUser ?: User(UUID.randomUUID().toString(), "You", "")

                    val message = Message(
                        id = UUID.randomUUID().toString(),
                        userId = user.id,
                        username = user.username,
                        profileImage = user.profileImage,
                        content = if (text.isBlank()) null else text,
                        mediaUrls = emptyList(),
                        audioUrl = null,
                        createdAt = System.currentTimeMillis(),
                        status = MessageStatus.SENDING
                    )

                    // Optimistic UI
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
                        mediaUris = _state.value.selectedMediaUris
                    )
                }
            }

            is ChatIntent.RetryMessage -> {
                viewModelScope.launch {
                    val message = _state.value.messages.find { it.id == intent.messageId } ?: run {
                        _effect.send(ChatEffect.ShowToast("الرسالة غير موجودة"))
                        return@launch
                    }

                    val updated = _state.value.messages.map {
                        if (it.id == message.id) it.copy(status = MessageStatus.SENDING) else it
                    }
                    _state.update { it.copy(messages = updated) }

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
