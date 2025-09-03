package com.minercontrol.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minercontrol.app.model.Miner
import com.minercontrol.app.model.SettingsRepository
import com.minercontrol.app.network.UdpListener
import com.minercontrol.app.ui.components.MinerCard
import com.minercontrol.app.ui.components.StatsCard
import com.minercontrol.app.ui.components.GaugesPanel
import com.minercontrol.app.utils.AppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    udpListener: UdpListener,
    onNavigateToSettings: () -> Unit,
    settingsRepository: SettingsRepository
) {
    val miners by udpListener.miners.collectAsState()
    val isListening by udpListener.isListening.collectAsState()
    val currentLanguage by settingsRepository.getLanguageFlow().collectAsState(initial = "pt")
    val appName by settingsRepository.getAppNameFlow().collectAsState(initial = "MinerControl Dashboard")
    val strings = if (currentLanguage == "en") AppStrings.English else AppStrings.Portuguese
    
    var editingMiner by remember { mutableStateOf<String?>(null) }
    var editName by remember { mutableStateOf("") }
    
    // Calcular estatísticas totais
    val totalMiners = miners.size
    val onlineMiners = miners.count { it.isOnline }
    val totalHashrate = miners.filter { it.isOnline }.sumOf { it.hashrate }
    val totalPower = miners.filter { it.isOnline }.sumOf { it.power }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🔥",
                                fontSize = 20.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                appName,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = strings.realtimeMonitoring,
                            fontSize = 12.sp,
                            color = Color(0xFFBBBBBB),
                            fontWeight = FontWeight.Normal
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = strings.settingsTitle)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1a1a1a),
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF0f0f0f)
    ) { paddingValues ->
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Gauges Panel (estilo corrida como no original)
            item {
                GaugesPanel(miners = miners)
            }
            
            // Estatísticas gerais
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatsCard(
                        title = strings.miners,
                        value = "$onlineMiners/$totalMiners",
                        modifier = Modifier.weight(1f)
                    )
                    StatsCard(
                        title = strings.hashrate,
                        value = if (totalHashrate >= 1_000_000) {
                            "${String.format("%.1f", totalHashrate / 1_000_000)} TH/s"
                        } else if (totalHashrate >= 1000) {
                            "${String.format("%.1f", totalHashrate / 1000)} GH/s"
                        } else {
                            "${String.format("%.1f", totalHashrate)} KH/s"
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatsCard(
                        title = strings.power,
                        value = "${String.format("%.0f", totalPower)} W",
                        modifier = Modifier.weight(1f)
                    )
                    StatsCard(
                        title = "Status",
                        value = if (onlineMiners == totalMiners && totalMiners > 0) "OK" else if (currentLanguage == "en") "ALERT" else "ALERTA",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Tabela de miners (estilo horizontal como no app original)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161616)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column {
                        // Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${strings.miners} ${strings.online}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(if (isListening) Color(0xFF4CAF50) else Color(0xFFF44336), RoundedCornerShape(5.dp))
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = if (isListening) "Live (UDP 12345)" else "Offline",
                                    color = Color(0xFFAAAAAA),
                                    fontSize = 12.sp
                                )
                            }
                        }

                        // Header row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1E1E1E))
                                .padding(vertical = 8.dp, horizontal = 12.dp)
                        ) {
                            HeaderCell("Miner", 1.6f)
                            HeaderCell("Status", 0.9f)
                            HeaderCell("Pool", 1.4f)
                            HeaderCell("Hash", 1.0f)
                            HeaderCell(strings.temp, 0.8f)
                            HeaderCell("Blocks", 0.9f)
                            HeaderCell("Diff", 0.9f)
                            HeaderCell(strings.shares, 0.9f)
                            HeaderCell(strings.power, 0.9f)
                            HeaderCell(strings.uptime, 1.0f)
                        }

                        if (miners.isEmpty()) {
                            Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        if (currentLanguage == "en") "No miners found" else "Nenhum miner encontrado", 
                                        color = Color(0xFFAAAAAA)
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        if (currentLanguage == "en") "Check UDP on port 12345" else "Verifique UDP na porta 12345", 
                                        color = Color(0xFF777777), 
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        } else {
                            miners.forEach { miner ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 10.dp, horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Miner (name editable) + IP
                                    Box(modifier = Modifier.weight(1.6f)) {
                                        if (editingMiner == miner.ip) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                OutlinedTextField(
                                                    value = editName,
                                                    onValueChange = { editName = it },
                                                    modifier = Modifier.widthIn(min = 120.dp).weight(1f),
                                                    singleLine = true,
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedTextColor = Color.White,
                                                        unfocusedTextColor = Color.White,
                                                        focusedBorderColor = Color(0xFF4CAF50),
                                                        unfocusedBorderColor = Color(0xFF555555)
                                                    )
                                                )
                                                IconButton(onClick = {
                                                    if (editName.isNotBlank()) {
                                                        udpListener.setCustomName(miner.ip, editName)
                                                        editingMiner = null
                                                    }
                                                }) {
                                                    Icon(Icons.Default.Check, contentDescription = "Salvar", tint = Color(0xFF4CAF50))
                                                }
                                            }
                                        } else {
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(miner.displayName, color = Color.White, fontWeight = FontWeight.SemiBold)
                                                    Spacer(Modifier.width(6.dp))
                                                    IconButton(onClick = {
                                                        editingMiner = miner.ip
                                                        editName = miner.displayName
                                                    }) {
                                                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color(0xFF888888))
                                                    }
                                                }
                                                Text(miner.ip, color = Color(0xFF888888), fontSize = 12.sp)
                                            }
                                        }
                                    }

                                    // Status
                                    Box(modifier = Modifier.weight(0.9f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .background(if (miner.isOnline) Color(0xFF4CAF50) else Color(0xFFF44336), RoundedCornerShape(4.dp))
                                            )
                                            Spacer(Modifier.width(6.dp))
                                            Text(
                                                if (miner.isOnline) strings.online else "offline", 
                                                color = Color(0xFFCCCCCC), 
                                                fontSize = 12.sp
                                            )
                                        }
                                    }

                                    // Pool
                                    TableCell(miner.poolInUse ?: "-", 1.4f)

                                    // Hash
                                    TableCell(miner.hashrateFormatted, 1.0f)

                                    // Temp (colorized)
                                    Box(modifier = Modifier.weight(0.8f)) {
                                        val tempColor = when {
                                            miner.temp >= 70 -> Color(0xFFF44336)
                                            miner.temp >= 45 -> Color(0xFFFFC107)
                                            else -> Color(0xFF4CAF50)
                                        }
                                        Text(miner.tempFormatted, color = tempColor)
                                    }

                                    // Blocks
                                    TableCell((miner.valid ?: 0).toString(), 0.9f)

                                    // Difficulty (prefer PoolDiff/LastDiff)
                                    TableCell(miner.poolDiff ?: miner.lastDiff ?: "0", 0.9f)

                                    // Shares
                                    TableCell(miner.sharesFormatted, 0.9f)

                                    // Power
                                    TableCell(miner.powerFormatted, 0.9f)

                                    // Uptime
                                    TableCell(miner.uptimeFormatted, 1.0f)
                                }

                                HorizontalDivider(color = Color(0xFF222222))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.HeaderCell(text: String, weight: Float) {
    Text(
        text = text,
        color = Color(0xFFBBBBBB),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.weight(weight)
    )
}

@Composable
private fun RowScope.TableCell(text: String, weight: Float) {
    Text(
        text = text,
        color = Color(0xFFDDDDDD),
        fontSize = 13.sp,
        modifier = Modifier.weight(weight)
    )
}
