package com.example.neondrift.domain.engine

import com.example.neondrift.domain.model.DifficultyLevel
import kotlin.math.min

object DifficultyManager {

    /**
     * Determines current difficulty tier based on distance survived (in meters/units).
     */
    fun getDifficulty(distance: Float): DifficultyLevel {
        return when {
            distance >= DifficultyLevel.HARD.minDistance -> DifficultyLevel.HARD
            distance >= DifficultyLevel.MEDIUM.minDistance -> DifficultyLevel.MEDIUM
            else -> DifficultyLevel.EASY
        }
    }

    /**
     * Smoothly progressive scroll speed (pixels per second), increasing gradually.
     */
    fun getScrollSpeed(distance: Float): Float {
        val diff = getDifficulty(distance)
        // Add a gentle continuous ramp within the tier
        val progressInTier = when (diff) {
            DifficultyLevel.EASY -> min(1f, distance / DifficultyLevel.MEDIUM.minDistance)
            DifficultyLevel.MEDIUM -> min(
                1f,
                (distance - DifficultyLevel.MEDIUM.minDistance) /
                        (DifficultyLevel.HARD.minDistance - DifficultyLevel.MEDIUM.minDistance)
            )
            DifficultyLevel.HARD -> min(1f, (distance - DifficultyLevel.HARD.minDistance) / 1000f)
        }
        val bonus = progressInTier * 80f
        return diff.baseScrollSpeed + bonus
    }

    /**
     * Computes path width in pixels.
     * Starts at ~45% of screen width, narrows down toward ~20% as distance grows.
     */
    fun getPathWidth(distance: Float, screenWidth: Float): Float {
        val startFraction = 0.45f
        val minFraction = 0.20f
        // Over 1500m distance, path gradually shrinks from 45% to 20%
        val factor = min(1f, distance / 1500f)
        val currentFraction = startFraction - (startFraction - minFraction) * factor
        return screenWidth * currentFraction
    }

    /**
     * Computes maximum lateral delta allowed for next segment center.
     */
    fun getMaxTurnDelta(distance: Float, screenWidth: Float): Float {
        val diff = getDifficulty(distance)
        return screenWidth * diff.maxTurnDeltaFraction
    }
}
