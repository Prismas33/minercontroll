package com.minercontrol.app.utils

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.*

private val Context.languageDataStore by preferencesDataStore(name = "language_settings")

class LanguageManager private constructor(private val context: Context) {
    
    companion object {
        private val KEY_LANGUAGE = stringPreferencesKey("selected_language")
        const val LANGUAGE_PT = "pt"
        const val LANGUAGE_EN = "en"
        
        @Volatile private var INSTANCE: LanguageManager? = null
        
        fun getInstance(context: Context): LanguageManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LanguageManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    fun getCurrentLanguageFlow(): Flow<String> =
        context.languageDataStore.data.map { prefs ->
            prefs[KEY_LANGUAGE] ?: getSystemLanguage()
        }
    
    suspend fun setLanguage(languageCode: String) {
        context.languageDataStore.edit { prefs ->
            prefs[KEY_LANGUAGE] = languageCode
        }
    }
    
    private fun getSystemLanguage(): String {
        return when (Locale.getDefault().language) {
            "pt" -> LANGUAGE_PT
            else -> LANGUAGE_EN
        }
    }
    
    fun getStrings(languageCode: String): AppStrings {
        return when (languageCode) {
            LANGUAGE_PT -> AppStrings.Portuguese
            else -> AppStrings.English
        }
    }
}

sealed class AppStrings {
    abstract val dashboardTitle: String
    abstract val settingsTitle: String
    abstract val totalHashrate: String
    abstract val avgTemperature: String
    abstract val minersOnline: String
    abstract val realtimeMonitoring: String
    abstract val miners: String
    abstract val hashrate: String
    abstract val networkSettings: String
    abstract val udpPort: String
    abstract val appSettings: String
    abstract val language: String
    abstract val portuguese: String
    abstract val english: String
    abstract val autoScanNetwork: String
    abstract val aboutApp: String
    abstract val version: String
    abstract val monitoring: String
    abstract val saveSettings: String
    abstract val currentNetwork: String
    abstract val ip: String
    abstract val subnet: String
    abstract val appName: String
    abstract val appNamePlaceholder: String
    abstract val back: String
    abstract val refresh: String
    abstract val online: String
    abstract val temp: String
    abstract val power: String
    abstract val uptime: String
    abstract val shares: String
    
    object Portuguese : AppStrings() {
        override val dashboardTitle = "MinerControl Dashboard"
        override val settingsTitle = "Configurações"
        override val totalHashrate = "Total Hashrate"
        override val avgTemperature = "Temperatura Média"
        override val minersOnline = "Miners Online"
        override val realtimeMonitoring = "Monitorização em tempo real dos teus solo miners de lotaria"
        override val miners = "Miners"
        override val hashrate = "Hashrate"
        override val networkSettings = "Configurações de Rede"
        override val udpPort = "Porta UDP"
        override val appSettings = "Configurações da App"
        override val language = "Idioma"
        override val portuguese = "🇵🇹 Português"
        override val english = "🇬🇧 English"
        override val autoScanNetwork = "Auto-scan da rede"
        override val aboutApp = "Sobre a App"
        override val version = "Versão: 1.0.0"
        override val monitoring = "Monitorização de miners via UDP (porta"
        override val saveSettings = "Guardar Configurações"
        override val currentNetwork = "Rede Atual"
        override val ip = "IP"
        override val subnet = "Subnet"
        override val appName = "Nome da App"
        override val appNamePlaceholder = "MinerControl Dashboard"
        override val back = "Voltar"
        override val refresh = "Atualizar redes"
        override val online = "Online"
        override val temp = "Temp"
        override val power = "Potência"
        override val uptime = "Uptime"
        override val shares = "Shares"
    }
    
    object English : AppStrings() {
        override val dashboardTitle = "MinerControl Dashboard"
        override val settingsTitle = "Settings"
        override val totalHashrate = "Total Hashrate"
        override val avgTemperature = "Avg Temperature"
        override val minersOnline = "Miners Online"
        override val realtimeMonitoring = "Real-time monitoring of your solo lottery miners"
        override val miners = "Miners"
        override val hashrate = "Hashrate"
        override val networkSettings = "Network Settings"
        override val udpPort = "UDP Port"
        override val appSettings = "App Settings"
        override val language = "Language"
        override val portuguese = "🇵🇹 Português"
        override val english = "🇬🇧 English"
        override val autoScanNetwork = "Auto-scan network"
        override val aboutApp = "About App"
        override val version = "Version: 1.0.0"
        override val monitoring = "UDP miners monitoring (port"
        override val saveSettings = "Save Settings"
        override val currentNetwork = "Current Network"
        override val ip = "IP"
        override val subnet = "Subnet"
        override val appName = "App Name"
        override val appNamePlaceholder = "MinerControl Dashboard"
        override val back = "Back"
        override val refresh = "Refresh networks"
        override val online = "Online"
        override val temp = "Temp"
        override val power = "Power"
        override val uptime = "Uptime"
        override val shares = "Shares"
    }
}
