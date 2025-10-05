package com.example.data.webservice

import com.example.data.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.status.SessionSource
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseClientProvider @Inject constructor(
    private val supabaseUrl: String,
    private val supabaseKey: String
) {

    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = supabaseUrl,
            supabaseKey = supabaseKey
        ) {
            install(Postgrest)
            install(Realtime)
            install(Storage)
        }
    }
}
