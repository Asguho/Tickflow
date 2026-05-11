package com.tickflow.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = TickflowBlue,
    secondary = TickflowMint,
    background = TickflowSurface,
    surface = TickflowSurface,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    onBackground = TickflowInk,
    onSurface = TickflowInk,
)

@Composable
fun TickflowTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = TickflowTypography,
        content = content,
    )
}
