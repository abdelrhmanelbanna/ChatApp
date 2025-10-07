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
            val mediaUris = inputData.getStringArray(KEY_MEDIA_URIS)?.toList()

            Log.d("SendMessageWorker", "Worker input: id=$id, userId=$userId, username=$username, content=$content, mediaUris=$mediaUris")


            val message = Message(
                id = id,
                userId = userId,
                username = username,
                profileImage = profileImage,
                content = if (content.isBlank()) null else content,
                mediaUrls = mediaUris ?: emptyList(),
                audioUrl = null,
                createdAt = createdAt,
                status = MessageStatus.SENDING
            )

            val notification = NotificationHelper.createUploadingNotification(applicationContext, id)
            setForeground(ForegroundInfo(NotificationHelper.UPLOAD_NOTIFICATION_ID, notification))

            Log.d("SendMessageWorker", "Calling SendMessageUseCase for message id=$id")
            val result = sendMessageUseCase(message, mediaUris)
            Log.d("SendMessageWorker", "SendMessageUseCase result: $result")

            when (result) {
                is ResultWrapper.Success -> {
                    Log.d("SendMessageWorker", "Message sent successfully: $id")
                    NotificationHelper.showCompleted(applicationContext, id, success = true)
                    Result.success()
                }
                is ResultWrapper.Error -> {
                    Log.e("SendMessageWorker", "Message failed to send: ${result.exception.message}, code=${result.exception.code}")
                    NotificationHelper.showCompleted(applicationContext, id, success = false)
                    if (result.exception.code == 6) {
                        Log.d("SendMessageWorker", "Retrying due to transient error (code 6)")
                        Result.retry()
                    } else {
                        Result.failure()
                    }
                }
                is ResultWrapper.Loading -> {
                    Log.d("SendMessageWorker", "Message sending in progress, retrying: $id")
                    Result.retry()
                }
            }
        }
    } catch (e: Exception) {
        Log.e("SendMessageWorker", "Unexpected error in doWork()", e)
        Result.failure()
    }
}
