package com.minercontrol.app.model

data class Miner(
    val id: String,
    val ip: String,
    val name: String,
    // We treat hashrate as KH/s to match legacy collector
    val hashrate: Double,
    val power: Double,
    val temp: Double,
    val status: String,
    val lastSeen: Long = System.currentTimeMillis(),
    
    // Campos adicionais do protocolo original
    val boardType: String? = null,
    val hashRateString: String? = null, // e.g., '113.13K'
    val share: String? = null, // '1/138'
    val netDiff: String? = null,
    val poolDiff: String? = null,
    val lastDiff: String? = null,
    val bestDiff: String? = null,
    val valid: Int? = null,
    val progress: Int? = null,
    val rssi: Int? = null,
    val freeHeap: Double? = null, // Changed from Int to Double
    val uptime: String? = null, // '000d 01:23:46'
    val version: String? = null,
    val poolInUse: String? = null,
    val updateTime: String? = null,
    
    // Campos computados
    val acceptedShares: Int? = null,
    val rejectedShares: Int? = null,
    val hashrateKH: Double? = null,
    
    // Nome customizado pelo usuário
    val customName: String? = null
) {
    val isOnline: Boolean
        get() = status.lowercase() == "online" && 
                (System.currentTimeMillis() - lastSeen) < 30000 // 30 segundos
                
    val displayName: String
        get() = customName ?: name
                
    val hashrateFormatted: String
        get() {
            // hashrate (KH/s) -> show GH/s or TH/s where sensible
            return "${String.format("%.2f", hashrate)} KH/s"
        }
        
    val powerFormatted: String
        get() = if (power > 0) "${String.format("%.0f", power)} W" else "N/A"
        
    val tempFormatted: String
        get() = if (temp > 0) "${String.format("%.0f", temp)}°C" else "N/A"
        
    val sharesFormatted: String
        get() = when {
            acceptedShares != null && rejectedShares != null -> "${acceptedShares}/${rejectedShares}"
            share != null -> share
            else -> "N/A"
        }
        
    val uptimeFormatted: String
        get() = uptime?.let { formatUptime(it) } ?: "-"
        
    private fun formatUptime(raw: String): String {
        // Handle common uptime formats:
        // "000d 01:23:46" -> "0d 1h 23m"
        // "01:23:46" -> "1h 23m"
        // "123456" -> Convert seconds to readable format
        // "000d 06:33:43\r000d 08:38:22" -> Take the last value (most recent)
        return try {
            // First, handle duplicated values separated by \r or \n
            val cleanValue = raw.split(Regex("[\r\n]")).lastOrNull()?.trim() ?: raw.trim()
            
            when {
                cleanValue.contains("d ") -> {
                    // Format: "000d 01:23:46"
                    val parts = cleanValue.split("d ")
                    val days = parts[0].toIntOrNull() ?: 0
                    val time = parts.getOrNull(1) ?: ""
                    val timeParts = time.split(":")
                    if (timeParts.size >= 2) {
                        val hours = timeParts[0].toIntOrNull() ?: 0
                        val minutes = timeParts[1].toIntOrNull() ?: 0
                        when {
                            days > 0 -> "${days}d ${hours}h"
                            hours > 0 -> "${hours}h ${minutes}m"
                            else -> "${minutes}m"
                        }
                    } else "${days}d"
                }
                cleanValue.contains(":") -> {
                    // Format: "01:23:46"
                    val parts = cleanValue.split(":")
                    if (parts.size >= 2) {
                        val hours = parts[0].toIntOrNull() ?: 0
                        val minutes = parts[1].toIntOrNull() ?: 0
                        when {
                            hours > 0 -> "${hours}h ${minutes}m"
                            else -> "${minutes}m"
                        }
                    } else cleanValue
                }
                cleanValue.all { it.isDigit() } -> {
                    // Assume seconds
                    val seconds = cleanValue.toLongOrNull() ?: 0
                    val hours = seconds / 3600
                    val minutes = (seconds % 3600) / 60
                    when {
                        hours > 24 -> "${hours/24}d ${hours%24}h"
                        hours > 0 -> "${hours}h ${minutes}m"
                        else -> "${minutes}m"
                    }
                }
                else -> cleanValue
            }
        } catch (_: Exception) {
            // If all else fails, try to extract the last meaningful part
            raw.split(Regex("[\r\n]")).lastOrNull()?.trim() ?: raw
        }
    }
}
