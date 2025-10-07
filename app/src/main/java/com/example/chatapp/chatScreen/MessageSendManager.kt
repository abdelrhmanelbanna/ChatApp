package com.example.chatapp.chatScreen

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.chatapp.chatScreen.worker.SendMessageWorker
import com.example.domain.entity.Message
import com.example.domain.entity.MessageStatus
import java.util.concurrent.TimeUnit

object MessageSendManager {

    fun enqueueSend(
        context: Context,
        message: Message,
        mediaUris: List<String>,
        onComplete: (Message) -> Unit
    ) {
        val data = workDataOf(
            "message_id" to message.id,
            "user_id" to message.userId,
            "username" to message.username,
            "profile_image" to message.profileImage,
            "content" to (message.content ?: ""),
            "created_at" to message.createdAt,
            "media_uris" to mediaUris.toTypedArray()
        )

        val request = OneTimeWorkRequestBuilder<SendMessageWorker>()
            .setInputData(data)
            .setBackoffCriteria(BackoffPolicy.LINEAR, 5, TimeUnit.SECONDS)
            .addTag("send_message_${message.id}")
            .build()

        WorkManager.getInstance(context).enqueue(request)

        // Observe worker state to update UI
        WorkManager.getInstance(context)
            .getWorkInfoByIdLiveData(request.id)
            .observeForever { workInfo ->
                workInfo?.let {
                    val status = when (it.state) {
                        WorkInfo.State.SUCCEEDED -> MessageStatus.SENT
                        WorkInfo.State.FAILED -> MessageStatus.FAILED
                        WorkInfo.State.RUNNING, WorkInfo.State.ENQUEUED -> MessageStatus.SENDING
                        else -> message.status
                    }
                    onComplete(message.copy(status = status))
                }
            }
    }
}
