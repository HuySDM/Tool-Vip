package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ToolVipDarkColorScheme = darkColorScheme(
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

private val ToolVipLightColorScheme = lightColorScheme(
    primary = Color(0xFF0D9488), // Deep Teal
    secondary = Color(0xFF16A34A), // Green
    tertiary = Color(0xFFE11D48), // Rose Red
    background = Color(0xFFF4FBF9), // Soft Mint White
    surface = Color(0xFFFFFFFF), // White Card
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF0F172A), // Dark Slate Text
    onSurface = Color(0xFF0F172A), // Dark Slate Text
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1)
)

@Composable
fun MyApplicationTheme(
    isDarkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (isDarkTheme) ToolVipDarkColorScheme else ToolVipLightColorScheme,
        typography = Typography,
        content = content
    )
}
