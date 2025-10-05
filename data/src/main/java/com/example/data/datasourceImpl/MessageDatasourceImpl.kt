package com.example.data.datasourceImpl

import android.content.Context
import android.net.Uri
import com.example.data.datasource.MessageDatasource
import com.example.data.model.MessageDto
import com.example.data.model.MessageStatusDto
import com.example.data.webservice.SupabaseClientProvider
import com.example.domain.exceptions.DomainException
import com.example.domain.utils.ResultWrapper
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import io.github.jan.supabase.postgrest.query.Columns
import java.io.ByteArrayOutputStream
import java.io.InputStream
import javax.inject.Inject

class MessageDatasourceImpl @Inject constructor(
    private val supabaseClientProvider: SupabaseClientProvider,
    private val context: Context
) : MessageDatasource {

    private val client: Postgrest by lazy { supabaseClientProvider.client.postgrest }
    private val storage by lazy { supabaseClientProvider.client.storage }

    override suspend fun sendMessage(message: MessageDto, mediaUris: List<String>?): ResultWrapper<Unit> {
        return try {
            if (mediaUris != null && mediaUris.size > 10) {
                return ResultWrapper.Error(DomainException.MediaLimitExceededException())
            }

            val updatedMessage = if (mediaUris != null && mediaUris.isNotEmpty()) {
                val uploadedUrls = uploadMedia(mediaUris)
                message.copy(media_urls = uploadedUrls)
            } else {
                message
            }

            client.from("messages").insert(updatedMessage)
            ResultWrapper.Success(Unit)
        } catch (e: Exception) {
            ResultWrapper.Error(DomainException.MessageSendFailedException(e.message ?: "Failed to send message"))
        }
    }

    private suspend fun uploadMedia(mediaUris: List<String>): List<String> {
        return runCatching {
            mediaUris.mapNotNull { uriString ->
                runCatching {
                    val uri = Uri.parse(uriString)
                    if (uri.scheme == null || !isUriValid(uri)) {
                        null
                    } else {
                        val inputStream = context.contentResolver.openInputStream(uri)
                        if (inputStream == null) {
                            null
                        } else {
                            inputStream.use { stream ->
                                val byteArray = streamToByteArray(stream)
                                val path = "uploads/${uri.lastPathSegment ?: uriString.hashCode()}.jpg"
                                storage.from("media").upload(
                                    path = path,
                                    data = byteArray
                                )
                                val baseUrl = "https://yanmwvuaweddcsvewmln.supabase.co"
                                if (baseUrl.isNotEmpty()) {
                                    "$baseUrl/storage/v1/object/public/media/$path"
                                } else {
                                    throw DomainException.UnknownException("Supabase URL is not configured")
                                }
                            }
                        }
                    }
                }.getOrNull()
            }
        }.getOrNull() ?: emptyList()
    }

    private fun streamToByteArray(inputStream: InputStream): ByteArray {
        return ByteArrayOutputStream().use { outputStream ->
            inputStream.copyTo(outputStream)
            outputStream.toByteArray()
        }
    }

    private fun isUriValid(uri: Uri): Boolean {
        return runCatching {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                cursor.moveToFirst()
                cursor.count > 0
            } ?: false
        }.getOrDefault(false)
    }

    override fun observeMessages(): Flow<List<MessageDto>> = flow {
        val channel: RealtimeChannel = supabaseClientProvider.client.realtime.channel("public:messages")

        val changes = channel.postgresChangeFlow<PostgresAction>(
            schema = "public"
        ) {
            table = "messages"
        }

        channel.subscribe()

        // Emit initial snapshot
        try {
            val initial = client.from("messages").select().decodeList<MessageDto>()
            emit(initial)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Collect realtime changes
        changes.collect { action ->
            when (action) {
                is PostgresAction.Insert,
                is PostgresAction.Update -> {
                    try {
                        val messages = client.from("messages").select().decodeList<MessageDto>()
                        emit(messages)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                else -> Unit
            }
        }
    }


    override suspend fun retryMessage(message: MessageDto): ResultWrapper<Unit> {
        return try {
            client.from("messages").update({
                set("status", MessageStatusDto.SENDING.name)
            }) {
                filter {
                    MessageDto::id eq message.id
                }
            }

            sendMessage(message, null)

            ResultWrapper.Success(Unit)
        } catch (e: Exception) {
            ResultWrapper.Error(
                DomainException.MessageRetryFailedException(
                    e.message ?: "Failed to retry message"
                )
            )
        }
    }



    override fun observeMessageStatus(messageId: String): Flow<MessageStatusDto> = flow {
        val channel: RealtimeChannel = supabaseClientProvider.client.realtime.channel("public:messages")

        val changes = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "messages"
        }

        channel.subscribe()

        // Emit initial status once
        runCatching {
            val initial = client.from("messages")
                .select(Columns.list("id", "status")) {
                    filter {
                        eq("id", messageId)
                    }
                }
                .decodeSingleOrNull<MessageDto>()
                ?.status

            if (initial != null) emit(initial)
        }

        // Listen for realtime updates
        changes.collect { action ->


            if (action is PostgresAction.Update) {
                runCatching {
                    val updated = client.from("messages")
                        .select(Columns.list("id", "status")) {
                            filter {
                                eq("id", messageId)
                            }
                        }
                        .decodeSingleOrNull<MessageDto>()
                    updated?.status?.let { emit(it) }
                }
            }
        }
    }








}
