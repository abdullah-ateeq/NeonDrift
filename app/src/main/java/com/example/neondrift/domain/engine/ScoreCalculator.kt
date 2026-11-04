package com.example.neondrift.domain.engine

import kotlin.math.max

data class ScoreMilestone(
    val milestoneDistance: Float,
    val bonusScore: Int,
    val streakIncrease: Int,
    val message: String
)

object ScoreCalculator {

    const val DISTANCE_SCORE_MULTIPLIER = 10f // 10 points per meter
    const val CENTER_BONUS_PER_SECOND = 15f   // 15 extra points per second near center
    const val MILESTONE_INTERVAL = 250f       // Every 250m
    const val MILESTONE_BONUS = 250           // 250 bonus points per milestone

    /**
     * Calculates base score strictly from distance in meters.
     */
    fun calculateBaseScore(distanceMeters: Float): Int {
        return (distanceMeters * DISTANCE_SCORE_MULTIPLIER).toInt()
    }

    /**
     * Checks if a milestone was crossed between previous distance and current distance.
     */
    fun checkMilestone(
        previousDistance: Float,
        currentDistance: Float,
        currentStreak: Int
    ): ScoreMilestone? {
        val prevCount = (previousDistance / MILESTONE_INTERVAL).toInt()
        val currCount = (currentDistance / MILESTONE_INTERVAL).toInt()

        if (currCount > prevCount) {
            val milestoneMeters = currCount * MILESTONE_INTERVAL
            val nextStreak = currentStreak + 1
            val bonus = MILESTONE_BONUS * nextStreak
            return ScoreMilestone(
                milestoneDistance = milestoneMeters,
                bonusScore = bonus,
                streakIncrease = 1,
                message = "${milestoneMeters.toInt()}m SURVIVED! STREAK x$nextStreak"
            )
        }
        return null
    }

    /**
     * Calculates precision center bonus added in a delta time step.
     */
    fun calculateCenterBonus(
        isNearCenter: Boolean,
        deltaTimeSec: Float,
        streak: Int
    ): Int {
        if (!isNearCenter) return 0
        val streakFactor = max(1f, 1f + (streak * 0.2f))
        return (CENTER_BONUS_PER_SECOND * deltaTimeSec * streakFactor).toInt()
    }
}
