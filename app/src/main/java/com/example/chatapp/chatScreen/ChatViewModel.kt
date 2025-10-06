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
    @ApplicationContext private val context: AppContext
) : ViewModel() {

    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    private val _effect = Channel<ChatEffect>(Channel.BUFFERED)
    val effects = _effect.receiveAsFlow()

    // cache current user info (loaded at start)
    private var currentUserId: String? = null
    private var currentUserName: String = "You"
    private var currentUserProfileImage: String = ""

    init {
        viewModelScope.launch {
            // try to fetch the stored user (if saved in your backend) or at least the dataStore id
            currentUserId = dataStoreManager.getUserId().first()
            // attempt to load user details
            when (val r = getUserUseCase()) {
                is ResultWrapper.Success -> {
                    currentUserId = r.data.id
                    currentUserName = r.data.username
                    currentUserProfileImage = r.data.profileImage
                }
                else -> {
                    // keep what we have (dataStore id)
                }
            }

            startObservingMessages()
        }
    }

    private fun startObservingMessages() {
        viewModelScope.launch {
            observeMessagesUseCase().collect { list ->
                // sort or map if needed (usecase already sorts)
                _state.update { it.copy(messages = list) }
            }
        }
    }

    fun process(intent: ChatIntent) {
        when (intent) {
            is ChatIntent.Start -> { /* nothing else here */ }
            is ChatIntent.UpdateComposingText -> {
                _state.update { it.copy(composingText = intent.text) }
            }
            is ChatIntent.PickImagesClicked -> {
                viewModelScope.launch { _effect.send(ChatEffect.OpenImagePicker) }
            }
            is ChatIntent.ImagesSelected -> {
                val limited = if (intent.uris.size > 10) intent.uris.take(10) else intent.uris
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
                    // build Message domain object (optimistic)
                    val message = Message(
                        id = UUID.randomUUID().toString(),
                        userId = currentUserId ?: UUID.randomUUID().toString(),
                        username = currentUserName,
                        profileImage = currentUserProfileImage,
                        content = if (text.isBlank()) null else text,
                        mediaUrls = emptyList(),
                        audioUrl = null,
                        createdAt = System.currentTimeMillis(),
                        status = MessageStatus.SENDING
                    )

                    // optimistic UI update
                    _state.update { old ->
                        old.copy(
                            messages = old.messages + message,
                            composingText = "",
                            selectedMediaUris = emptyList()
                        )
                    }

                    // enqueue background send via WorkManager helper
                    MessageSendManager.enqueueSend(
                        context = context,
                        message = message,
                        mediaUris = _state.value.selectedMediaUris // already cleared above but we passed earlier state captured
                    )
                }
            }
            is ChatIntent.RetryMessage -> {
                viewModelScope.launch {
                    val message = _state.value.messages.find { it.id == intent.messageId }
                    if (message == null) {
                        _effect.send(ChatEffect.ShowToast("الرسالة غير موجودة"))
                        return@launch
                    }

                    // optimistic mark sending
                    val updated = _state.value.messages.map {
                        if (it.id == message.id) it.copy(status = MessageStatus.SENDING) else it
                    }
                    _state.update { it.copy(messages = updated) }

                    // Enqueue worker again (use mediaUrls if applicable)
                    MessageSendManager.enqueueSend(context = context, message = message, mediaUris = message.mediaUrls)
                }
            }
        }
    }
}
