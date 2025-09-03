package com.minercontrol.app.model

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

private val Context.dataStore by preferencesDataStore(name = "minercontrol_settings")

class SettingsRepository private constructor(private val context: Context) {
    private val gson = Gson()

    companion object {
        private val KEY_DISCOVERY_PORT = intPreferencesKey("discovery_port")
        private val KEY_MINER_NAMES = stringPreferencesKey("miner_names_json")
        private val KEY_LANGUAGE = stringPreferencesKey("language")
        private val KEY_APP_NAME = stringPreferencesKey("app_name")

        @Volatile private var INSTANCE: SettingsRepository? = null

        fun getInstance(context: Context): SettingsRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SettingsRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    fun getDiscoveryPortFlow(defaultPort: Int = 12345): Flow<Int> =
        context.dataStore.data.map { prefs ->
            prefs[KEY_DISCOVERY_PORT] ?: defaultPort
        }

    suspend fun setDiscoveryPort(port: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DISCOVERY_PORT] = port
        }
    }

    fun getLanguageFlow(default: String = "pt"): Flow<String> =
        context.dataStore.data.map { prefs ->
            prefs[KEY_LANGUAGE] ?: default
        }

    suspend fun setLanguage(language: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LANGUAGE] = language
        }
    }

    fun getAppNameFlow(default: String = "MinerControl Dashboard"): Flow<String> =
        context.dataStore.data.map { prefs ->
            prefs[KEY_APP_NAME] ?: default
        }

    suspend fun setAppName(name: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_APP_NAME] = name
        }
    }

    fun getMinerNamesFlow(): Flow<Map<String, String>> =
        context.dataStore.data.map { prefs ->
            val json = prefs[KEY_MINER_NAMES] ?: "{}"
            val type = object : TypeToken<Map<String, String>>() {}.type
            try {
                Gson().fromJson<Map<String, String>>(json, type) ?: emptyMap()
            } catch (_: Exception) {
                emptyMap()
            }
        }

    suspend fun setCustomName(ip: String, name: String?) {
        context.dataStore.edit { prefs ->
            val type = object : TypeToken<MutableMap<String, String>>() {}.type
            val current = try {
                Gson().fromJson<MutableMap<String, String>>(prefs[KEY_MINER_NAMES] ?: "{}", type) ?: mutableMapOf()
            } catch (_: Exception) {
                mutableMapOf()
            }
            if (name == null || name.isBlank()) current.remove(ip) else current[ip] = name
            prefs[KEY_MINER_NAMES] = Gson().toJson(current)
        }
    }

    suspend fun clearMinerNames() {
        context.dataStore.edit { prefs ->
            prefs[KEY_MINER_NAMES] = "{}"
        }
    }
}
