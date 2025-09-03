package com.minercontrol.app.network

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.minercontrol.app.model.IncomingMinerPayload
import com.minercontrol.app.model.Miner
import com.minercontrol.app.model.SettingsRepository
import com.minercontrol.app.model.toMiner
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.SocketException

class UdpListener(private val context: Context? = null) {
    private val gson = Gson()
    private var socket: DatagramSocket? = null
    private var listenerJob: Job? = null
    private var healthJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val settingsRepo: SettingsRepository? = context?.let { SettingsRepository.getInstance(it) }

    private val minerMap = LinkedHashMap<String, Miner>() // keep insertion order
    private val lastSeenMap = HashMap<String, Long>()
    private var cachedCustomNames = emptyMap<String, String>()
    private var currentPort: Int = 12345

    private val _miners = MutableStateFlow<List<Miner>>(emptyList())
    val miners: StateFlow<List<Miner>> = _miners

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening

    fun start(port: Int) {
        if (_isListening.value && port == currentPort) return
        stop()
        currentPort = port
        
        // Load cached custom names
        settingsRepo?.let { repo ->
            scope.launch {
                repo.getMinerNamesFlow().collect { names ->
                    cachedCustomNames = names
                    Log.d("UdpListener", "📝 Loaded ${names.size} custom miner names from cache")
                }
            }
        }

        listenerJob = scope.launch {
            try {
                socket = DatagramSocket(port)
                _isListening.value = true
                Log.d("UdpListener", "✅ Started listening on UDP $port")

                startHealthMonitoring()

                val buffer = ByteArray(4096)
                while (isActive && socket?.isClosed == false) {
                    val packet = DatagramPacket(buffer, buffer.size)
                    try {
                        socket?.receive(packet)
                        val data = String(packet.data, 0, packet.length)
                        val raddr = packet.address?.hostAddress ?: ""
                        Log.d("UdpListener", "📩 ${raddr} -> ${data}")

                        handleIncoming(data, raddr)
                    } catch (e: SocketException) {
                        if (isActive) Log.e("UdpListener", "Socket error: ${e.message}")
                        break
                    } catch (e: Exception) {
                        Log.e("UdpListener", "Parse error: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e("UdpListener", "❌ Failed to bind UDP: ${e.message}")
            } finally {
                _isListening.value = false
                socket?.close()
            }
        }
    }

    private fun handleIncoming(json: String, raddr: String?) {
        try {
            // Filter out non-JSON messages (like "DISCOVER")
            val trimmed = json.trim()
            if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) {
                Log.d("UdpListener", "🔍 Ignoring non-JSON message: $trimmed")
                return
            }
            
            Log.d("UdpListener", "📦 Processing JSON from $raddr: $trimmed")
            
            val payload = gson.fromJson(json, IncomingMinerPayload::class.java)
            val miner = payload.toMiner(cachedCustomNames[payload.ipLower ?: payload.ipUpper ?: ""]) 
            val ip = miner.ip.ifBlank { raddr ?: "" }

            if (ip.isBlank()) {
                Log.w("UdpListener", "⚠️ Cannot determine IP for miner")
                return
            }

            // ensure ip/id
            val finalMiner = miner.copy(
                id = ip,
                ip = ip,
                lastSeen = System.currentTimeMillis(),
                status = "online"
            )

            minerMap[ip] = finalMiner
            lastSeenMap[ip] = System.currentTimeMillis()
            
            Log.i("UdpListener", "✅ Added/updated miner: $ip (${finalMiner.name}) - ${finalMiner.hashrate} KH/s")
            Log.d("UdpListener", "📊 Current miners: ${minerMap.size}")
            
            publish()
        } catch (e: Exception) {
            Log.e("UdpListener", "Failed to parse/update miner: ${e.message}")
        }
    }

    private fun publish() {
        // sort: online first then by IP
        val list = minerMap.values.sortedWith(compareByDescending<Miner> { it.isOnline }.thenBy { it.ip })
        val onlineCount = list.count { it.isOnline }
        _miners.value = list
        
        Log.d("UdpListener", "📡 Published ${list.size} miners (${onlineCount} online) to UI")
    }

    private fun startHealthMonitoring() {
        healthJob?.cancel()
        healthJob = scope.launch {
            while (isActive) {
                try {
                    val now = System.currentTimeMillis()
                    var changed = false
                    for ((ip, last) in lastSeenMap) {
                        if (now - last > 30_000) { // 30s timeout
                            val m = minerMap[ip]
                            if (m != null && m.status == "online") {
                                minerMap[ip] = m.copy(status = "offline")
                                changed = true
                            }
                        }
                    }
                    if (changed) publish()
                } catch (_: Exception) {}
                delay(10_000) // check every 10s
            }
        }
    }

    fun stop() {
        listenerJob?.cancel()
        healthJob?.cancel()
        try { socket?.close() } catch (_: Exception) {}
        _isListening.value = false
        Log.d("UdpListener", "🛑 UDP listener stopped")
    }

    fun clearMiners() {
        minerMap.clear()
        lastSeenMap.clear()
        publish()
    }

    fun setCustomName(ip: String, name: String) {
        // Update local cache
        cachedCustomNames = cachedCustomNames + (ip to name)
        // Save to persistent storage
        settingsRepo?.let { repo ->
            scope.launch {
                repo.setCustomName(ip, name)
                Log.d("UdpListener", "💾 Saved custom name for $ip: $name")
            }
        }
        // Update current miner
        minerMap[ip]?.let { minerMap[ip] = it.copy(customName = name) }
        publish()
    }

    fun getCustomName(ip: String): String? = cachedCustomNames[ip]

    fun restart(port: Int) {
        stop()
        start(port)
    }
}
