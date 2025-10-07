package com.example.chatapp.chatScreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.domain.entity.Message
import com.example.domain.entity.MessageStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatMessageItem(
    message: Message,
    isMine: Boolean,
    onRetry: () -> Unit
) {

    val timeFormat = remember {
        SimpleDateFormat("HH:mm", Locale.getDefault())
    }
    val timeText = timeFormat.format(Date(message.createdAt))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        if (!isMine) {
            AsyncImage(
                model = message.profileImage,
                contentDescription = "avatar",
                modifier = Modifier
                    .size(36.dp)
                    .padding(end = 6.dp)
            )
        }

        Column(
            modifier = Modifier
                .widthIn(max = 250.dp)
                .background(if (isMine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                .padding(8.dp)
        ) {
            if (!isMine) {
                Text(text = message.username, style = MaterialTheme.typography.labelSmall)
            }

            message.content?.let {
                Text(text = it, style = MaterialTheme.typography.bodyMedium)
            }

            if (message.mediaUrls.isNotEmpty()) {
                // show a thumbnail (first)
                AsyncImage(model = message.mediaUrls.first(), contentDescription = "media", modifier = Modifier.height(120.dp).fillMaxWidth())
            }

            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End) {
                Text(
                    text = timeText,
                    style = MaterialTheme.typography.labelSmall
                )

                if (message.status == MessageStatus.FAILED) {
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = onRetry) {
                        Text("Retry") }

                    }
                }

        }
    }
}
