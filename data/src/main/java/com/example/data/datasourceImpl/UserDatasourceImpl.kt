package com.example.data.datasourceImpl

import android.content.Context
import com.example.data.datasource.UserDatasource
import com.example.data.model.UserDto
import com.example.data.webservice.SupabaseClientProvider
import com.example.domain.exceptions.DomainException
import com.example.domain.utils.ResultWrapper
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import javax.inject.Inject



class UserDatasourceImpl @Inject constructor(
    private val supabaseClientProvider: SupabaseClientProvider,
    context: Context
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
}