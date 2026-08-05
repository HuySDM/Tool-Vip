package com.example.ui.theme

import androidx.compose.ui.graphics.Color

data class ThemeColors(
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val background: Color,
    val cardBg: Color,
    val border: Color,
    val glow: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color
) {
    companion object {
        fun getColors(style: String, isDarkMode: Boolean = true): ThemeColors {
            if (isDarkMode) {
                return when (style) {
                    "CyberpunkAurora" -> ThemeColors(
                        primary = Color(0xFFFF007F), // Neon Pink
                        secondary = Color(0xFF00F0FF), // Neon Cyan
                        tertiary = Color(0xFFFFD700), // Gold
                        background = Color(0xFF0C0324), // Ultra deep violet
                        cardBg = Color(0xFF160636), // Deep purple-magenta
                        border = Color(0xFF38127E), // Vibrant dark purple border
                        glow = Color(0xFFFF007F),
                        textPrimary = Color.White,
                        textSecondary = Color(0xFFE2E8F0),
                        textMuted = Color(0xFF94A3B8)
                    )
                    "NeonInferno" -> ThemeColors(
                        primary = Color(0xFFFF3F1A), // Inferno Neon Red-Orange
                        secondary = Color(0xFFFFB000), // Vibrant Orange-Yellow
                        tertiary = Color(0xFFFF0055), // Intense Crimson
                        background = Color(0xFF0D0303), // Smoky black
                        cardBg = Color(0xFF1A0A0A), // Lava obsidian card
                        border = Color(0xFF3B1515), // Burnt sienna border
                        glow = Color(0xFFFF3F1A),
                        textPrimary = Color.White,
                        textSecondary = Color(0xFFE2E8F0),
                        textMuted = Color(0xFF94A3B8)
                    )
                    "AcidEmerald" -> ThemeColors(
                        primary = Color(0xFF26FF4B), // Nuclear Green
                        secondary = Color(0xFF00E676), // Rich Emerald
                        tertiary = Color(0xFFFF7B00), // High-vis Alert Orange
                        background = Color(0xFF010A01), // Slime-black base
                        cardBg = Color(0xFF061B06), // Radioactive moss card
                        border = Color(0xFF113D11), // Corrosive border
                        glow = Color(0xFF26FF4B),
                        textPrimary = Color.White,
                        textSecondary = Color(0xFFE2E8F0),
                        textMuted = Color(0xFF94A3B8)
                    )
                    "IceArctic" -> ThemeColors(
                        primary = Color(0xFF64E5FF), // Glacial Aqua
                        secondary = Color(0xFF99F6E4), // Ice mint tint
                        tertiary = Color(0xFFF43F5E), // Hot rose alert
                        background = Color(0xFF06101E), // Ocean trench black-blue
                        cardBg = Color(0xFF111E2E), // Sub-zero navy hull
                        border = Color(0xFF23354E), // Frosted iron frame
                        glow = Color(0xFF64E5FF),
                        textPrimary = Color.White,
                        textSecondary = Color(0xFFE2E8F0),
                        textMuted = Color(0xFF94A3B8)
                    )
                    else -> ThemeColors( // DeepObsidian (Standard Teal Theme)
                        primary = Color(0xFF18E2C2), // BrightTurquoise
                        secondary = Color(0xFF4ADE80), // GlowGreen
                        tertiary = Color(0xFFF43F5E), // CoralVibrant
                        background = Color(0xFF040D12), // DeepObsidian
                        cardBg = Color(0xFF0A1A1A), // DarkTealCard
                        border = Color(0xFF1E3A3A), // BorderGreen
                        glow = Color(0xFF18E2C2),
                        textPrimary = Color.White,
                        textSecondary = Color(0xFFE2E8F0),
                        textMuted = Color(0xFF94A3B8)
                    )
                }
            } else {
                return when (style) {
                    "CyberpunkAurora" -> ThemeColors(
                        primary = Color(0xFFD0006F), // Vibrant Pink/Magenta
                        secondary = Color(0xFF0090A0), // Cyan Blue
                        tertiary = Color(0xFFC0A000), // Dark Gold
                        background = Color(0xFFF5F3FF), // Soft Lavender Tinted White
                        cardBg = Color(0xFFFFFFFF), // Pure White
                        border = Color(0xFFDDD6FE), // Light Lavender Border
                        glow = Color(0xFFD0006F),
                        textPrimary = Color(0xFF1E1B4B), // Very dark purple
                        textSecondary = Color(0xFF4338CA), // Deep indigo
                        textMuted = Color(0xFF6B7280)
                    )
                    "NeonInferno" -> ThemeColors(
                        primary = Color(0xFFE02401), // Dark Inferno Red-Orange
                        secondary = Color(0xFFD97706), // Vibrant Amber/Yellow-Orange
                        tertiary = Color(0xFFBE123C), // Intense Rose-Crimson
                        background = Color(0xFFFFF7F5), // Warm Sunset Tinted White
                        cardBg = Color(0xFFFFFFFF), // Pure White
                        border = Color(0xFFFEE2E2), // Light Red-Amber Border
                        glow = Color(0xFFE02401),
                        textPrimary = Color(0xFF450A0A), // Very dark red
                        textSecondary = Color(0xFF991B1B), // Dark crimson
                        textMuted = Color(0xFF6B7280)
                    )
                    "AcidEmerald" -> ThemeColors(
                        primary = Color(0xFF059669), // Nuclear emerald green
                        secondary = Color(0xFF10B981), // Rich Mint Green
                        tertiary = Color(0xFFEA580C), // High-vis Alert Orange
                        background = Color(0xFFF0FDF4), // Soft Radioactive Green Tinted White
                        cardBg = Color(0xFFFFFFFF), // Pure White
                        border = Color(0xFFDCFCE7), // Light Green Border
                        glow = Color(0xFF059669),
                        textPrimary = Color(0xFF064E3B), // Very dark forest green
                        textSecondary = Color(0xFF065F46), // Dark green-teal
                        textMuted = Color(0xFF6B7280)
                    )
                    "IceArctic" -> ThemeColors(
                        primary = Color(0xFF0284C7), // Glacial Ocean Blue
                        secondary = Color(0xFF0D9488), // Deep Mint Aqua
                        tertiary = Color(0xFFE11D48), // Hot Rose
                        background = Color(0xFFF0F9FF), // Soft Glacial Tinted White
                        cardBg = Color(0xFFFFFFFF), // Pure White
                        border = Color(0xFFE0F2FE), // Light Glacial Border
                        glow = Color(0xFF0284C7),
                        textPrimary = Color(0xFF0F172A), // Dark slate
                        textSecondary = Color(0xFF0369A1), // Dark ocean blue
                        textMuted = Color(0xFF6B7280)
                    )
                    else -> ThemeColors( // DeepObsidian (Standard Teal Theme)
                        primary = Color(0xFF0D9488), // Deep Teal Turquoise
                        secondary = Color(0xFF16A34A), // Glow Green
                        tertiary = Color(0xFFE11D48), // Coral Rose
                        background = Color(0xFFF4FBF9), // Soft Mint Tinted White
                        cardBg = Color(0xFFFFFFFF), // Pure White
                        border = Color(0xFFCCFBF1), // Light Teal Border
                        glow = Color(0xFF0D9488),
                        textPrimary = Color(0xFF0F172A), // Slate black
                        textSecondary = Color(0xFF0F766E), // Muted dark teal
                        textMuted = Color(0xFF4B5563) // Cool gray
                    )
                }
            }
        }
    }
}
