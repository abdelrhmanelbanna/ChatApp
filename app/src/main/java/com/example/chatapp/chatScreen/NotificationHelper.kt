package com.example.chatapp.chatScreen

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.chatapp.R

object NotificationHelper {
    const val CHANNEL_ID_UPLOAD = "chat_upload_channel"
    const val UPLOAD_NOTIFICATION_ID = 2001

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Uploads"
            val desc = "Notifications for message uploads"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID_UPLOAD, name, importance)
            channel.description = desc
            val nm = context.getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }

    fun createUploadingNotification(context: Context, messageId: String): Notification {
        ensureChannel(context)
        return NotificationCompat.Builder(context, CHANNEL_ID_UPLOAD)
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setContentTitle("Sending Message")
            .setContentText("Uploading message...")
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .build()
    }

    fun showCompleted(context: Context, messageId: String, success: Boolean) {
        ensureChannel(context)
        val nm = context.getSystemService(NotificationManager::class.java)
        val b = NotificationCompat.Builder(context, CHANNEL_ID_UPLOAD)
            .setSmallIcon(
                if (success) android.R.drawable.stat_sys_upload_done
                else android.R.drawable.stat_notify_error
            )
            .setContentTitle(if (success) "Sent" else "Failed to Send")
            .setContentText(if (success) "Message sent successfully" else "Message sending failed")
            .setAutoCancel(true)
        nm.notify(UPLOAD_NOTIFICATION_ID, b.build())
    }
}
