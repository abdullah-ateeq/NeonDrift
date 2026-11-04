package com.example.neondrift.domain.model

enum class DifficultyLevel(
    val displayName: String,
    val minDistance: Float,
    val baseScrollSpeed: Float, // pixels per second
    val pathWidthFraction: Float, // relative to screen width
    val maxTurnDeltaFraction: Float, // max lateral displacement relative to screen width
    val steeringSensitivity: Float
) {
    EASY(
        displayName = "Easy",
        minDistance = 0f,
        baseScrollSpeed = 380f,
        pathWidthFraction = 0.44f,
        maxTurnDeltaFraction = 0.22f,
        steeringSensitivity = 1.0f
    ),
    MEDIUM(
        displayName = "Medium",
        minDistance = 250f,
        baseScrollSpeed = 520f,
        pathWidthFraction = 0.33f,
        maxTurnDeltaFraction = 0.30f,
        steeringSensitivity = 1.15f
    ),
    HARD(
        displayName = "Hard",
        minDistance = 750f,
        baseScrollSpeed = 680f,
        pathWidthFraction = 0.22f,
        maxTurnDeltaFraction = 0.38f,
        steeringSensitivity = 1.3f
    )
}
