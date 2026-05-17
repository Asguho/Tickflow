package com.tickflow.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val Display = FontFamily.Default

private val baseline = Typography()

val TickflowTypography = Typography(
    displayLarge = baseline.displayLarge.copy(
        fontFamily = Display,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.5).sp,
    ),
    displayMedium = baseline.displayMedium.copy(
        fontFamily = Display,
        fontWeight = FontWeight.SemiBold,
    ),
    displaySmall = baseline.displaySmall.copy(
        fontFamily = Display,
        fontWeight = FontWeight.SemiBold,
    ),
    headlineLarge = baseline.headlineLarge.copy(
        fontFamily = Display,
        fontWeight = FontWeight.Bold,
    ),
    headlineMedium = baseline.headlineMedium.copy(
        fontFamily = Display,
        fontWeight = FontWeight.SemiBold,
    ),
    headlineSmall = baseline.headlineSmall.copy(
        fontFamily = Display,
        fontWeight = FontWeight.SemiBold,
    ),
    titleLarge = baseline.titleLarge.copy(
        fontFamily = Display,
        fontWeight = FontWeight.SemiBold,
    ),
    titleMedium = baseline.titleMedium.copy(
        fontFamily = Display,
        fontWeight = FontWeight.Medium,
    ),
    labelLarge = baseline.labelLarge.copy(
        fontFamily = Display,
        fontWeight = FontWeight.Medium,
    ),
    bodyLarge = baseline.bodyLarge.copy(
        fontFamily = Display,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = baseline.bodyMedium.copy(
        fontFamily = Display,
    ),
)

@Suppress("unused")
val TickflowDisplayHero = TextStyle(
    fontFamily = Display,
    fontWeight = FontWeight.Bold,
    fontSize = 60.sp,
    lineHeight = 64.sp,
    letterSpacing = (-1).sp,
)
