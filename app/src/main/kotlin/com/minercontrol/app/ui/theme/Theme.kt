package com.minercontrol.app.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColors(
    primary = Color(0xFF4CAF50),
    secondary = Color(0xFF03DAC5),
    background = Color(0xFF0f0f0f),
    surface = Color(0xFF1a1a1a),
    error = Color(0xFFF44336),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    onError = Color.White
)

@Composable
fun MinerControlTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
