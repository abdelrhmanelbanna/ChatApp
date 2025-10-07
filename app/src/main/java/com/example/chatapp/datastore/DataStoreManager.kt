package com.example.chatapp.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


val Context.dataStore by preferencesDataStore("user_prefs")

class DataStoreManager(private val context: Context) {

    companion object {
        val USER_ID_KEY = stringPreferencesKey("user_id")
        val USERNAME_KEY = stringPreferencesKey("username")

        val IMAGE_KEY = stringPreferencesKey("image")
    }

    suspend fun saveImage(imageUri: String) {
        context.dataStore.edit { prefs ->
            prefs[IMAGE_KEY] = imageUri
        }
    }

     suspend fun getImage(): Flow<String?> {
        return context.dataStore.data.map { prefs ->
            prefs[IMAGE_KEY]
        }
    }


            suspend fun saveUserId(userId: String) {
                context.dataStore.edit { prefs ->
                    prefs[USER_ID_KEY] = userId
                }
            }

            fun getUserId(): Flow<String?> {
                return context.dataStore.data.map { prefs ->
                    prefs[USER_ID_KEY]
                }
            }

            suspend fun saveUsername(username: String) {
                context.dataStore.edit { prefs ->
                    prefs[USERNAME_KEY] = username
                }
            }

            fun getUsername(): Flow<String?> {
                return context.dataStore.data.map { prefs ->
                    prefs[USERNAME_KEY]
                }
            }

        }


