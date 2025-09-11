package com.minercontrol.app.model

import com.google.gson.annotations.SerializedName

/**
 * Mirrors the UDP JSON payload used in the legacy app (Next/Electron).
 * Keys often start with uppercase; we map them explicitly and convert to Miner.
 */
data class IncomingMinerPayload(
    @SerializedName("ip") val ipLower: String? = null,
    @SerializedName("IP") val ipUpper: String? = null,

    @SerializedName("name") val nameLower: String? = null,
    @SerializedName("Name") val nameUpper: String? = null,

    @SerializedName("BoardType") val boardType: String? = null,
    @SerializedName("HashRate") val hashRateString: String? = null, // e.g., "113.1K"
    @SerializedName("hashrate") val hashRateLower: String? = null, // alternative field name
    @SerializedName("Share") val share: String? = null, // e.g., "1/138"
    @SerializedName("shares") val sharesLower: String? = null, // alternative field name
    @SerializedName("NetDiff") val netDiff: String? = null,
    @SerializedName("PoolDiff") val poolDiff: String? = null,
    @SerializedName("LastDiff") val lastDiff: String? = null,
    @SerializedName("BestDiff") val bestDiff: String? = null,
    @SerializedName("Valid") val valid: Int? = null,
    @SerializedName("valid") val validLower: Int? = null, // alternative field name
    @SerializedName("Progress") val progress: Int? = null,
    @SerializedName("Temp") val temp: Any? = null, // number or string
    @SerializedName("temp") val tempLower: Any? = null, // alternative field name
    @SerializedName("temperature") val temperature: Any? = null, // alternative field name
    @SerializedName("RSSI") val rssi: Int? = null,
    @SerializedName("FreeHeap") val freeHeap: Any? = null, // Can be Double or Int
    @SerializedName("Uptime") val uptime: String? = null,
    @SerializedName("uptime") val uptimeLower: String? = null, // alternative field name
    @SerializedName("Version") val version: String? = null,
    @SerializedName("PoolInUse") val poolInUse: String? = null,
    @SerializedName("UpdateTime") val updateTime: String? = null,

    // Extra fields sometimes seen
    @SerializedName("Power") val power: Any? = null,
    @SerializedName("power") val powerLower: Any? = null, // alternative field name
    @SerializedName("Fan") val fan: Any? = null,
    @SerializedName("Voltage") val voltage: Any? = null,
    @SerializedName("Frequency") val frequency: Any? = null
)

fun IncomingMinerPayload.toMiner(customName: String? = null): Miner {
    val ip = (ipLower ?: ipUpper ?: "").ifBlank { "" }
    
    // Based on real miner data analysis:
    // - Field "Name" does not exist in JSON, miners only send "ip"
    // - Generate name from BoardType (e.g., "usb chain", "cyd 2.8") or use IP
    val autoName = when {
        !boardType.isNullOrBlank() -> "Miner-${boardType.replace(" ", "-")}-${ip.split('.').lastOrNull() ?: ""}"
        else -> "Miner-${ip.split('.').lastOrNull() ?: ""}"
    }
    val name = customName ?: (nameLower ?: nameUpper ?: "").ifBlank { autoName }

    // Parse temperature safely (can be number or string) - check multiple field names
    // Real data shows: field "Temp" exists, miner .111 sends 50.09999847, miner .152 sends 0
    val tempVal = when {
        temp != null -> when (temp) {
            is Number -> temp.toDouble()
            is String -> temp.toDoubleOrNull() ?: 0.0
            else -> 0.0
        }
        tempLower != null -> when (tempLower) {
            is Number -> tempLower.toDouble()
            is String -> tempLower.toDoubleOrNull() ?: 0.0
            else -> 0.0
        }
        temperature != null -> when (temperature) {
            is Number -> temperature.toDouble()
            is String -> temperature.toDoubleOrNull() ?: 0.0
            else -> 0.0
        }
        else -> 0.0
    }

    // Hashrate string often like "388.24KH/s", "1.0149MH/s" - check multiple field names
    val hashrateKH = parseHashRateKH(hashRateString ?: hashRateLower)

    // Real data analysis: Power field DOES NOT EXIST in miner JSON
    // Both miners send no power field, so this will always be 0.0
    val powerVal = when {
        power != null -> when (power) {
            is Number -> power.toDouble()
            is String -> power.toDoubleOrNull() ?: 0.0
            else -> 0.0
        }
        powerLower != null -> when (powerLower) {
            is Number -> powerLower.toDouble()
            is String -> powerLower.toDoubleOrNull() ?: 0.0
            else -> 0.0
        }
        else -> 0.0  // This will be the case for real miners - no power data
    }
    
    // FreeHeap can be Double or Int
    val freeHeapVal = when (freeHeap) {
        is Number -> freeHeap.toDouble()
        is String -> freeHeap.toDoubleOrNull()
        else -> null
    }

    // Real data: Share format is "0/0/0.0%" (accepted/rejected/percentage)
    val sharesValue = share ?: sharesLower
    
    // Real data: Valid field exists and is integer (both miners send 0)
    val validValue = valid ?: validLower
    
    // Real data: Uptime format has \r separators: "008d 18:39:01\r008d 20:43:40"
    val uptimeValue = uptime ?: uptimeLower

    return Miner(
        id = ip,
        ip = ip,
        name = name,
        hashrate = hashrateKH, // store as KH/s to match legacy
        power = powerVal,  // Will be 0.0 for real miners since they don't send power data
        temp = tempVal,
        status = "online",
        lastSeen = System.currentTimeMillis(),
        boardType = boardType,
        hashRateString = hashRateString ?: hashRateLower,
        share = sharesValue,
        netDiff = netDiff,
        poolDiff = poolDiff,
        lastDiff = lastDiff,
        bestDiff = bestDiff,
        valid = validValue,
        progress = progress,
        rssi = rssi,
        freeHeap = freeHeapVal,
        uptime = uptimeValue,
        version = version,
        poolInUse = poolInUse,
        updateTime = updateTime,
        // Parse shares: "0/0/0.0%" -> accepted=0, rejected=0
        acceptedShares = sharesValue?.split('/')?.getOrNull(0)?.toIntOrNull(),
        rejectedShares = sharesValue?.split('/')?.getOrNull(1)?.toIntOrNull(),
        hashrateKH = hashrateKH,
        customName = customName
    )
}

private fun parseHashRateKH(hr: String?): Double {
    if (hr.isNullOrBlank()) return 0.0
    // Accepted formats: "113.1K", "0.8M", "0.001G", "123" (assume KH)
    val trimmed = hr.trim().uppercase()
    return try {
        when {
            trimmed.endsWith("GH/S") || trimmed.endsWith("G") -> {
                trimmed.replace("GH/S", "").replace("G", "").trim().toDouble() * 1_000_000.0
            }
            trimmed.endsWith("MH/S") || trimmed.endsWith("M") -> {
                trimmed.replace("MH/S", "").replace("M", "").trim().toDouble() * 1_000.0
            }
            trimmed.endsWith("KH/S") || trimmed.endsWith("K") -> {
                trimmed.replace("KH/S", "").replace("K", "").trim().toDouble()
            }
            else -> trimmed.replace(Regex("[^0-9.]+"), "").toDoubleOrNull() ?: 0.0
        }
    } catch (_: Exception) {
        0.0
    }
}
