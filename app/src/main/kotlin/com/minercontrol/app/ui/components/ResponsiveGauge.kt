package com.minercontrol.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*

@Composable
fun ResponsiveGauge(
    value: Double,
    max: Double,
    unit: String,
    label: String,
    dangerAt: Double = max * 0.8,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1a1a1a)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label.uppercase(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF888888)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                GaugeCanvas(
                    value = value,
                    max = max,
                    dangerAt = dangerAt
                )
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = formatValue(value, unit),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = unit,
                        fontSize = 12.sp,
                        color = Color(0xFF888888)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Barra de progresso linear como backup
            LinearProgressIndicator(
                progress = { (value / max).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = getGaugeColor(value, dangerAt, max),
                trackColor = Color(0xFF2a2a2a)
            )
        }
    }
}

@Composable
private fun GaugeCanvas(
    value: Double,
    max: Double,
    dangerAt: Double
) {
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val center = center
        val radius = size.minDimension / 2 - 20.dp.toPx()
        val strokeWidth = 8.dp.toPx()
        
        val startAngle = 135f
        val sweepAngle = 270f
        val progress = (value / max).toFloat().coerceIn(0f, 1f)
        val progressAngle = sweepAngle * progress
        
        // Fundo do gauge
        drawArc(
            color = Color(0xFF2a2a2a),
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round
            ),
            topLeft = androidx.compose.ui.geometry.Offset(
                center.x - radius,
                center.y - radius
            ),
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
        )
        
        // Progresso do gauge
        if (progress > 0f) {
            val gaugeColor = getGaugeColor(value, dangerAt, max)
            drawArc(
                color = gaugeColor,
                startAngle = startAngle,
                sweepAngle = progressAngle,
                useCenter = false,
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round
                ),
                topLeft = androidx.compose.ui.geometry.Offset(
                    center.x - radius,
                    center.y - radius
                ),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
            )
        }
        
        // Marcadores de escala
        for (i in 0..10) {
            val angle = startAngle + (sweepAngle * i / 10)
            val radian = angle * PI / 180
            val startRadius = radius - 15.dp.toPx()
            val endRadius = radius - 5.dp.toPx()
            
            val startX = center.x + startRadius * cos(radian).toFloat()
            val startY = center.y + startRadius * sin(radian).toFloat()
            val endX = center.x + endRadius * cos(radian).toFloat()
            val endY = center.y + endRadius * sin(radian).toFloat()
            
            drawLine(
                color = Color(0xFF555555),
                start = androidx.compose.ui.geometry.Offset(startX, startY),
                end = androidx.compose.ui.geometry.Offset(endX, endY),
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}

private fun getGaugeColor(value: Double, dangerAt: Double, max: Double): Color {
    return when {
        value >= dangerAt -> Color(0xFFF44336) // Vermelho
        value >= max * 0.6 -> Color(0xFFFF9800) // Laranja  
        else -> Color(0xFF4CAF50) // Verde
    }
}

private fun formatValue(value: Double, unit: String): String {
    return when (unit) {
        "hash" -> {
            // Value is in KH/s, scale appropriately
            if (value >= 1_000_000) {
                "${String.format("%.1f", value / 1_000_000)} T"
            } else if (value >= 1000) {
                "${String.format("%.1f", value / 1000)} G"
            } else {
                "${String.format("%.1f", value)} K"
            }
        }
        "°C" -> String.format("%.0f", value)
        "W" -> String.format("%.0f", value)
        "" -> String.format("%.0f", value)
        else -> String.format("%.1f", value)
    }
}
