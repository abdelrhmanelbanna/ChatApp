package com.example.data.datasourceImpl

import android.content.Context
import android.net.Uri
import com.example.data.datasource.UserDatasource
import com.example.data.model.UserDto
import com.example.data.webservice.SupabaseClientProvider
import com.example.domain.exceptions.DomainException
import com.example.domain.utils.ResultWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.Storage
import java.util.UUID
import javax.inject.Inject



class UserDatasourceImpl @Inject constructor(
    private val supabaseClientProvider: SupabaseClientProvider,
    private val storage: Storage,
    @ApplicationContext private val context: Context
) : UserDatasource {

    private val client: Postgrest by lazy { supabaseClientProvider.client.postgrest }

    override suspend fun saveUser(user: UserDto): ResultWrapper<Unit> {
        return try {
            client.from("users").insert(user)
            ResultWrapper.Success(Unit)
        } catch (e: Exception) {
            ResultWrapper.Error(DomainException.UserNotFoundException(e.message ?: "Save failed"))
        }
    }

    override suspend fun getUser(): ResultWrapper<UserDto> {
        return try {
            val user = client.from("users").select().decodeSingle<UserDto>()
            ResultWrapper.Success(user)
        } catch (e: Exception) {
            ResultWrapper.Error(DomainException.UserNotFoundException(e.message ?: "User not found"))
        }
    }

    override suspend fun uploadProfileImage(uri: String): ResultWrapper<String> {
        return try {
            val parsedUri = Uri.parse(uri)
            val inputStream = context.contentResolver.openInputStream(parsedUri)
                ?: return ResultWrapper.Error(DomainException.UnknownException("Invalid image"))

            val bytes = inputStream.use { it.readBytes() }


            val path = "profile_${UUID.randomUUID()}.jpg"
            println("Uploading image -> path=$path, bytes=${bytes.size}")

            storage.from("profile").upload(path, bytes)

            val baseUrl = "https://yanmwvuaweddcsvewmln.supabase.co"
            val url = "$baseUrl/storage/v1/object/public/profile/$path"

            ResultWrapper.Success(url)
        } catch (e: Exception) {
            e.printStackTrace()
            ResultWrapper.Error(DomainException.UnknownException(e.message ?: "Upload failed"))
        }
    }


}