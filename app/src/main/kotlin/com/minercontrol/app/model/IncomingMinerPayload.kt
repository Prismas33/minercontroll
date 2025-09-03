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
    @SerializedName("Share") val share: String? = null, // e.g., "1/138"
    @SerializedName("NetDiff") val netDiff: String? = null,
    @SerializedName("PoolDiff") val poolDiff: String? = null,
    @SerializedName("LastDiff") val lastDiff: String? = null,
    @SerializedName("BestDiff") val bestDiff: String? = null,
    @SerializedName("Valid") val valid: Int? = null,
    @SerializedName("Progress") val progress: Int? = null,
    @SerializedName("Temp") val temp: Any? = null, // number or string
    @SerializedName("RSSI") val rssi: Int? = null,
    @SerializedName("FreeHeap") val freeHeap: Any? = null, // Can be Double or Int
    @SerializedName("Uptime") val uptime: String? = null,
    @SerializedName("Version") val version: String? = null,
    @SerializedName("PoolInUse") val poolInUse: String? = null,
    @SerializedName("UpdateTime") val updateTime: String? = null,

    // Extra fields sometimes seen
    @SerializedName("Power") val power: Any? = null,
    @SerializedName("Fan") val fan: Any? = null,
    @SerializedName("Voltage") val voltage: Any? = null,
    @SerializedName("Frequency") val frequency: Any? = null
)

fun IncomingMinerPayload.toMiner(customName: String? = null): Miner {
    val ip = (ipLower ?: ipUpper ?: "").ifBlank { "" }
    val name = (nameLower ?: nameUpper ?: "").ifBlank { "Miner-${ip.split('.').lastOrNull() ?: ""}" }

    // Parse temperature safely (can be number or string)
    val tempVal = when (temp) {
        is Number -> temp.toDouble()
        is String -> temp.toDoubleOrNull() ?: 0.0
        else -> 0.0
    }

    // Hashrate string often like "113.1K" -> KH/s
    val hashrateKH = parseHashRateKH(hashRateString)

    // Power can be string or number
    val powerVal = when (power) {
        is Number -> power.toDouble()
        is String -> power.toDoubleOrNull() ?: 0.0
        else -> 0.0
    }
    
    // FreeHeap can be Double or Int
    val freeHeapVal = when (freeHeap) {
        is Number -> freeHeap.toDouble()
        is String -> freeHeap.toDoubleOrNull()
        else -> null
    }

    return Miner(
        id = ip,
        ip = ip,
        name = name,
        hashrate = hashrateKH, // store as KH/s to match legacy
        power = powerVal,
        temp = tempVal,
        status = "online",
        lastSeen = System.currentTimeMillis(),
        boardType = boardType,
        hashRateString = hashRateString,
        share = share,
        netDiff = netDiff,
        poolDiff = poolDiff,
        lastDiff = lastDiff,
        bestDiff = bestDiff,
        valid = valid,
        progress = progress,
        rssi = rssi,
        freeHeap = freeHeapVal,
        uptime = uptime,
        version = version,
        poolInUse = poolInUse,
        updateTime = updateTime,
        acceptedShares = share?.split('/')?.getOrNull(1)?.toIntOrNull(),
        rejectedShares = share?.split('/')?.getOrNull(0)?.toIntOrNull(),
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
