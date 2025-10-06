// package com.example.chatapp.chatScreen
package com.example.chatapp.chatScreen

import com.example.domain.entity.Message

sealed interface ChatIntent {
    object Start : ChatIntent
    data class SendText(val text: String) : ChatIntent
    object PickImagesClicked : ChatIntent
    data class ImagesSelected(val uris: List<String>) : ChatIntent
    data class RetryMessage(val messageId: String) : ChatIntent
    data class UpdateComposingText(val text: String) : ChatIntent
}


data class ChatState(
    val currentUserId: String = "",
    val messages: List<Message> = emptyList(),
    val composingText: String = "",
    val selectedMediaUris: List<String> = emptyList()
)

sealed interface ChatEffect {
    object OpenImagePicker : ChatEffect
    data class ShowToast(val message: String) : ChatEffect
}
