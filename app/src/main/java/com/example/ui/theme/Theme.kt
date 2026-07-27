package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ToolVipColorScheme = darkColorScheme(
    primary = BrightTurquoise,
    secondary = GlowGreen,
    tertiary = CoralVibrant,
    background = DeepObsidian,
    surface = DarkTealCard,
    onPrimary = DeepObsidian,
    onSecondary = DeepObsidian,
    onBackground = IceMint,
    onSurface = IceMint,
    surfaceVariant = DarkTealCard,
    onSurfaceVariant = TextGray,
    outline = BorderGreen
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ToolVipColorScheme,
        typography = Typography,
        content = content
    )
}
