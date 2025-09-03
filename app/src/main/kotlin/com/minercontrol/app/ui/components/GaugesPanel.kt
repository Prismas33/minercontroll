package com.minercontrol.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.minercontrol.app.model.Miner

@Composable
fun GaugesPanel(
    miners: List<Miner>,
    modifier: Modifier = Modifier
) {
    // Calcular estatísticas dos miners
    val stats by remember(miners) {
        derivedStateOf {
            val onlineMiners = miners.filter { it.isOnline }
            Stats(
                totalHashrate = onlineMiners.sumOf { it.hashrate },
                avgTemperature = if (onlineMiners.isNotEmpty()) {
                    onlineMiners.sumOf { it.temp } / onlineMiners.size
                } else 0.0,
                minersOnlineCount = onlineMiners.size.toDouble(),
                totalMiners = miners.size.toDouble()
            )
        }
    }
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Gauge de Hashrate Total
        ResponsiveGauge(
            value = stats.totalHashrate,
            max = maxOf(stats.totalHashrate * 1.2, 1000.0), // Dynamic max based on actual hashrate
            unit = "hash",
            label = "Total Hashrate",
            dangerAt = stats.totalHashrate * 0.8,
            modifier = Modifier.weight(1f)
        )
        
        // Gauge de Temperatura Média
        ResponsiveGauge(
            value = stats.avgTemperature,
            max = 100.0,
            unit = "°C",
            label = "Avg Temperature",
            dangerAt = 70.0,
            modifier = Modifier.weight(1f)
        )
        
        // Gauge de Miners Online
        ResponsiveGauge(
            value = stats.minersOnlineCount,
            max = maxOf(stats.totalMiners, 10.0),
            unit = "",
            label = "Miners Online",
            dangerAt = maxOf(stats.totalMiners * 0.8, 8.0),
            modifier = Modifier.weight(1f)
        )
    }
}

private data class Stats(
    val totalHashrate: Double,
    val avgTemperature: Double, 
    val minersOnlineCount: Double,
    val totalMiners: Double
)
