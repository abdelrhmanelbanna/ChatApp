package com.example.chatapp.chatScreen

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.chatapp.chatScreen.worker.SendMessageWorker
import com.example.domain.entity.Message
import java.util.concurrent.TimeUnit

object MessageSendManager {
    private const val KEY_ID = "message_id"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USERNAME = "username"
    private const val KEY_PROFILE_IMAGE = "profile_image"
    private const val KEY_CONTENT = "content"
    private const val KEY_CREATED_AT = "created_at"
    private const val KEY_MEDIA_URIS = "media_uris" // string array

    fun enqueueSend(context: Context, message: Message, mediaUris: List<String>?) {
        val data = Data.Builder().apply {
            putString(KEY_ID, message.id)
            putString(KEY_USER_ID, message.userId)
            putString(KEY_USERNAME, message.username)
            putString(KEY_PROFILE_IMAGE, message.profileImage)
            putString(KEY_CONTENT, message.content ?: "")
            putLong(KEY_CREATED_AT, message.createdAt)
            if (mediaUris != null && mediaUris.isNotEmpty()) {
                putStringArray(KEY_MEDIA_URIS, mediaUris.toTypedArray())
            }
        }.build()


        Log.d("MessageSendManager", "EnqueueSend with data: $data")

        val request = OneTimeWorkRequestBuilder<SendMessageWorker>()
            .setInputData(data)
            .setBackoffCriteria(BackoffPolicy.LINEAR, 5, TimeUnit.SECONDS)
            .addTag("send_message_${message.id}")
            .build()

        val appContext = context.applicationContext
        WorkManager.getInstance(appContext).enqueue(request)
    }
}
