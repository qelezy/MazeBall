package com.example.mazeball.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val DEVICE_ID = stringPreferencesKey("device_id")
        val NICKNAME = stringPreferencesKey("nickname")
        val SERVER_URL = stringPreferencesKey("server_url")
        fun bestTimeForLevel(levelId: Int) = longPreferencesKey("best_time_level_$levelId")
    }

    val userPreferencesFlow = context.dataStore.data
        .map {
            val deviceId = it[PreferencesKeys.DEVICE_ID] ?: generateAndStoreDeviceId()
            val nickname = it[PreferencesKeys.NICKNAME] ?: ""
            val serverUrl = it[PreferencesKeys.SERVER_URL] ?: ""
            UserPreferences(deviceId, nickname, serverUrl)
        }

    suspend fun getBestTime(levelId: Int): Long? {
        return context.dataStore.data.map {
            it[PreferencesKeys.bestTimeForLevel(levelId)]
        }.first()
    }
    
    suspend fun getAllBestTimes(): Map<Int, Long> {
        val preferences = context.dataStore.data.first()
        val bestTimes = mutableMapOf<Int, Long>()
        preferences.asMap().forEach { (key, value) ->
            if (key.name.startsWith("best_time_level_") && value is Long) {
                val levelId = key.name.substringAfter("best_time_level_").toIntOrNull()
                if (levelId != null) {
                    bestTimes[levelId] = value
                }
            }
        }
        return bestTimes
    }

    suspend fun updateBestTime(levelId: Int, timeMillis: Long): Boolean {
        val currentBest = getBestTime(levelId)
        val isNewBest = currentBest == null || timeMillis < currentBest
        if (isNewBest) {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.bestTimeForLevel(levelId)] = timeMillis
            }
        }
        return isNewBest
    }

    suspend fun updateNickname(newNickname: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NICKNAME] = newNickname
        }
    }

     suspend fun updateServerUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SERVER_URL] = url
        }
    }

    private suspend fun generateAndStoreDeviceId(): String {
        val newId = UUID.randomUUID().toString()
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEVICE_ID] = newId
        }
        return newId
    }
}

data class UserPreferences(val deviceId: String, val nickname: String, val serverUrl: String)
