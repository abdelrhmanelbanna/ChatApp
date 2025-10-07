package com.example.data.webservice

import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.status.SessionSource
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.resumable.SettingsResumableCache
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseClientProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val supabaseUrl: String,
    private val supabaseKey: String
) {

    val client: SupabaseClient by lazy {

        val httpClient = HttpClient(OkHttp) {
            engine {
                config {
                    followRedirects(true)
                    retryOnConnectionFailure(true)
                }
            }
        }

        createSupabaseClient(
            supabaseUrl = supabaseUrl,
            supabaseKey = supabaseKey
        ) {
            httpEngine = httpClient.engine
            install(Postgrest)
            install(Realtime)
            install(Storage){
                resumable {
                    cache = SettingsResumableCache(
                        SharedPreferencesSettings(
                            context.getSharedPreferences("supabase_cache", Context.MODE_PRIVATE)
                        )
                    )
                }
            }
        }
    }
}