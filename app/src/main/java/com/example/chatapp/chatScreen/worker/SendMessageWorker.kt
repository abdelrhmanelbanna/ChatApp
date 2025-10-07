package com.example.chatapp.chatScreen.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.example.chatapp.chatScreen.NotificationHelper
import com.example.domain.entity.Message
import com.example.domain.entity.MessageStatus
import com.example.domain.repository.MessageRepository
import com.example.domain.usecase.SendMessageUseCase
import com.example.domain.utils.ResultWrapper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.coroutineScope

@HiltWorker
class SendMessageWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val sendMessageUseCase: SendMessageUseCase,
    private val messageRepository: MessageRepository
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val KEY_MEDIA_URIS = "media_uris"
    }

    override suspend fun doWork(): Result = try {
        coroutineScope {
            val id = inputData.getString("message_id") ?: return@coroutineScope Result.failure()
            val userId = inputData.getString("user_id") ?: ""
            val username = inputData.getString("username") ?: ""
            val profileImage = inputData.getString("profile_image") ?: ""
            val content = inputData.getString("content") ?: ""
            val createdAt = inputData.getLong("created_at", System.currentTimeMillis())
            val mediaUris = inputData.getStringArray(KEY_MEDIA_URIS)?.toList() ?: emptyList()

            val message = Message(
                id = id,
                userId = userId,
                username = username,
                profileImage = profileImage,
                content = if (content.isBlank()) null else content,
                mediaUrls = mediaUris,
                audioUrl = null,
                createdAt = createdAt,
                status = MessageStatus.SENDING
            )

            val notification = NotificationHelper.createUploadingNotification(appContext, id)
            setForeground(ForegroundInfo(NotificationHelper.UPLOAD_NOTIFICATION_ID, notification))

            Log.d("SendMessageWorker", "Sending message id=$id")
            val result = sendMessageUseCase(message, mediaUris)
            Log.d("SendMessageWorker", "SendMessageUseCase result: $result")

            when (result) {
                is ResultWrapper.Success -> {
                    messageRepository.updateMessageStatus(id, MessageStatus.SENT)
                    NotificationHelper.showCompleted(appContext, id, success = true)
                    Log.d("SendMessageWorker", "Message sent successfully: $id")
                    Result.success()
                }
                is ResultWrapper.Error -> {
                    messageRepository.updateMessageStatus(id, MessageStatus.FAILED)
                    NotificationHelper.showCompleted(appContext, id, success = false)
                    Log.e("SendMessageWorker", "Message failed: ${result.exception.message}")
                    if (result.exception.code == 6) Result.retry() else Result.failure()
                }
                is ResultWrapper.Loading -> Result.retry()
            }
        }
    } catch (e: Exception) {
        Log.e("SendMessageWorker", "Unexpected error", e)
        Result.failure()
    }
}
