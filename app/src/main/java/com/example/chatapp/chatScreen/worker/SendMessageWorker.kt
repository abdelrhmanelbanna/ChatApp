package com.example.chatapp.chatScreen.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.example.chatapp.chatScreen.NotificationHelper
import com.example.domain.entity.Message
import com.example.domain.entity.MessageStatus
import com.example.domain.usecase.SendMessageUseCase
import com.example.domain.utils.ResultWrapper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.coroutineScope

@HiltWorker
class SendMessageWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val sendMessageUseCase: SendMessageUseCase
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val KEY_MEDIA_URIS = "media_uris"
    }

    override suspend fun doWork(): Result = coroutineScope {
        // read input
        val id = inputData.getString("message_id") ?: return@coroutineScope Result.failure()
        val userId = inputData.getString("user_id") ?: ""
        val username = inputData.getString("username") ?: ""
        val profileImage = inputData.getString("profile_image") ?: ""
        val content = inputData.getString("content")
        val createdAt = inputData.getLong("created_at", System.currentTimeMillis())
        val mediaUris = inputData.getStringArray(KEY_MEDIA_URIS)?.toList()

        // build message domain object
        val message = Message(
            id = id,
            userId = userId,
            username = username,
            profileImage = profileImage,
            content = content,
            mediaUrls = emptyList(),
            audioUrl = null,
            createdAt = createdAt,
            status = MessageStatus.SENDING
        )

        // show foreground notification
        val notification = NotificationHelper.createUploadingNotification(applicationContext, id)
        setForeground(ForegroundInfo(NotificationHelper.UPLOAD_NOTIFICATION_ID, notification))

        // call use case
        return@coroutineScope when (val result = sendMessageUseCase(message, mediaUris)) {
            is ResultWrapper.Success -> {
                NotificationHelper.showCompleted(applicationContext, id, success = true)
                Result.success()
            }
            is ResultWrapper.Error -> {
                NotificationHelper.showCompleted(applicationContext, id, success = false)
                if (result.exception.code == 6) {
                    Result.retry()
                } else {
                    Result.failure()
                }
            }
            is ResultWrapper.Loading -> {
                Result.retry()
            }
        }
    }
}
