package com.example.neondrift.data

enum class ThemeMode {
    SYSTEM,
    DARK,
    LIGHT
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true
)

data class GameStats(
    val bestScore: Int = 0,
    val bestDistance: Float = 0f
)
