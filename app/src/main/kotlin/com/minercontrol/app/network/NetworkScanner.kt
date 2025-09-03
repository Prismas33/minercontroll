package com.minercontrol.app.network

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.minercontrol.app.model.Miner
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress

class NetworkScanner(private val context: Context) {
    
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private val _availableNetworks = MutableStateFlow<List<NetworkInfo>>(emptyList())
    val availableNetworks: StateFlow<List<NetworkInfo>> = _availableNetworks
    
    private val _currentNetwork = MutableStateFlow<NetworkInfo?>(null)
    val currentNetwork: StateFlow<NetworkInfo?> = _currentNetwork
    
    data class NetworkInfo(
        val ssid: String,
        val subnet: String,
        val ip: String,
        val adapter: String,
        val description: String,
        val isConnected: Boolean = false
    )
    
    fun detectNetworks() {
        scope.launch {
            try {
                val networks = mutableListOf<NetworkInfo>()
                
                // Obter rede atual
                getCurrentNetworkInfo()?.let { currentNet ->
                    networks.add(currentNet.copy(isConnected = true))
                    _currentNetwork.value = currentNet
                }
                
                // Adicionar redes padrão para miners
                networks.addAll(getDefaultMinerNetworks())
                
                _availableNetworks.value = networks
                Log.d("NetworkScanner", "Detected ${networks.size} networks")
                
            } catch (e: Exception) {
                Log.e("NetworkScanner", "Error detecting networks: ${e.message}")
                // Fallback para redes padrão
                _availableNetworks.value = getDefaultMinerNetworks()
            }
        }
    }
    
    private fun getCurrentNetworkInfo(): NetworkInfo? {
        return try {
            Log.d("NetworkScanner", "🔍 Getting current network info...")
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val connectionInfo = wifiManager.connectionInfo
            
            Log.d("NetworkScanner", "📶 WiFi enabled: ${wifiManager.isWifiEnabled}")
            Log.d("NetworkScanner", "🔗 Connection info: $connectionInfo")
            
            if (connectionInfo != null && connectionInfo.ssid != null) {
                val rawSsid = connectionInfo.ssid
                Log.d("NetworkScanner", "🔍 Raw SSID: '$rawSsid'")
                
                val ssid = when {
                    rawSsid == "<unknown ssid>" -> "Rede WiFi (Unknown)"
                    rawSsid == "\"<unknown ssid>\"" -> "Rede WiFi (Unknown)"
                    rawSsid.startsWith("\"") && rawSsid.endsWith("\"") -> {
                        val cleaned = rawSsid.substring(1, rawSsid.length - 1)
                        if (cleaned == "<unknown ssid>") "Rede WiFi (Unknown)" else cleaned
                    }
                    rawSsid.isBlank() -> "Rede WiFi (Blank)"
                    else -> rawSsid
                }
                val ip = getLocalIpAddress()
                val subnet = calculateSubnet(ip)
                
                Log.d("NetworkScanner", "📡 SSID: $ssid")
                Log.d("NetworkScanner", "🌐 Local IP: $ip")
                Log.d("NetworkScanner", "🖧 Subnet: $subnet")
                
                val network = NetworkInfo(
                    ssid = ssid,
                    subnet = subnet,
                    ip = ip,
                    adapter = "WiFi",
                    description = "Rede WiFi ativa",
                    isConnected = true
                )
                
                Log.d("NetworkScanner", "✅ Network detected: $network")
                return network
            } else {
                Log.w("NetworkScanner", "⚠️ No WiFi connection info available")
                null
            }
            
        } catch (e: Exception) {
            Log.e("NetworkScanner", "❌ Error getting current network: ${e.message}")
            null
        }
    }
    
    private fun getLocalIpAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (!address.isLoopbackAddress && address.address.size == 4) {
                        return address.hostAddress ?: ""
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("NetworkScanner", "Error getting local IP: ${e.message}")
        }
        return ""
    }
    
    private fun calculateSubnet(ip: String): String {
        return try {
            val parts = ip.split(".")
            if (parts.size == 4) {
                "${parts[0]}.${parts[1]}.${parts[2]}.0/24"
            } else {
                "192.168.1.0/24"
            }
        } catch (e: Exception) {
            "192.168.1.0/24"
        }
    }
    
    private fun getDefaultMinerNetworks(): List<NetworkInfo> {
        return listOf(
            NetworkInfo(
                ssid = "MNdisp",
                subnet = "192.168.25.0/24",
                ip = "192.168.25.1",
                adapter = "WiFi",
                description = "Rede específica para miners"
            ),
            NetworkInfo(
                ssid = "Casa",
                subnet = "192.168.1.0/24", 
                ip = "192.168.1.1",
                adapter = "WiFi",
                description = "Rede doméstica"
            ),
            NetworkInfo(
                ssid = "Router",
                subnet = "192.168.0.0/24",
                ip = "192.168.0.1", 
                adapter = "WiFi",
                description = "Rede do router"
            )
        )
    }
    
    /**
     * Real scan via UDP broadcast: send a small probe to subnet broadcast on the given port
     * and wait briefly; then count miners from the provided snapshot function filtered by subnet.
     * If no snapshot provider is given, returns 0 (no simulation).
     */
    fun scanNetwork(
        subnet: String,
        port: Int = 12345,
        getMinersSnapshot: (() -> List<Miner>)? = null,
        callback: (Int) -> Unit
    ) {
        scope.launch {
            try {
                Log.d("NetworkScanner", "🔍 Scanning network via UDP: $subnet on port $port")
                val broadcastIp = subnetToBroadcast(subnet)
                Log.d("NetworkScanner", "📡 Broadcasting to: $broadcastIp")
                
                val socket = DatagramSocket(null)
                try {
                    socket.reuseAddress = true
                    socket.broadcast = true
                    socket.bind(InetSocketAddress(0))
                    val payload = "DISCOVER".toByteArray()
                    val packet = DatagramPacket(payload, payload.size, InetAddress.getByName(broadcastIp), port)
                    
                    Log.d("NetworkScanner", "📤 Sending discovery packets...")
                    // send a couple of probes to improve chances
                    repeat(2) { 
                        socket.send(packet)
                        Log.d("NetworkScanner", "📤 Sent discovery packet ${it + 1}/2 to $broadcastIp:$port")
                        delay(150) 
                    }
                } finally {
                    try { socket.close() } catch (_: Exception) {}
                }

                Log.d("NetworkScanner", "⏳ Waiting for UDP responses...")
                // wait for UDP listener to ingest responses
                delay(1800)

                val miners = getMinersSnapshot?.invoke() ?: emptyList()
                val subnetPrefix = subnetPrefix(subnet)
                val matchingMiners = miners.filter { it.ip.startsWith(subnetPrefix) }
                val count = matchingMiners.size
                
                Log.d("NetworkScanner", "📊 Scan results:")
                Log.d("NetworkScanner", "   - Total miners: ${miners.size}")
                Log.d("NetworkScanner", "   - Subnet prefix: $subnetPrefix")
                Log.d("NetworkScanner", "   - Matching miners: $count")
                matchingMiners.forEach { miner ->
                    Log.d("NetworkScanner", "   - Found miner: ${miner.ip} (${miner.displayName})")
                }

                callback(count)
                Log.d("NetworkScanner", "UDP scan complete: $count miners found in $subnet")
                callback(count)
            } catch (e: Exception) {
                Log.e("NetworkScanner", "Error scanning network: ${e.message}")
                callback(0)
            }
        }
    }

    private fun subnetPrefix(subnet: String): String {
        // Accept formats like 192.168.25.0/24 or 192.168.25.0 or 192.168.25
        return try {
            val base = subnet.substringBefore('/')
            val parts = base.split('.')
            if (parts.size >= 3) "${parts[0]}.${parts[1]}.${parts[2]}." else ""
        } catch (_: Exception) { "" }
    }

    private fun subnetToBroadcast(subnet: String): String {
        val pref = subnetPrefix(subnet)
        return if (pref.isNotBlank()) pref + "255" else "255.255.255.255"
    }
}
