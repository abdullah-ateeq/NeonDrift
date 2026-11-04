package com.example.neondrift

import com.example.neondrift.domain.engine.DifficultyManager
import com.example.neondrift.domain.engine.ScoreCalculator
import com.example.neondrift.domain.model.DifficultyLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameRulesAndDifficultyTest {

    @Test
    fun difficultyProgression_scalesWithDistance() {
        assertEquals(DifficultyLevel.EASY, DifficultyManager.getDifficulty(0f))
        assertEquals(DifficultyLevel.EASY, DifficultyManager.getDifficulty(249f))
        assertEquals(DifficultyLevel.MEDIUM, DifficultyManager.getDifficulty(250f))
        assertEquals(DifficultyLevel.MEDIUM, DifficultyManager.getDifficulty(749f))
        assertEquals(DifficultyLevel.HARD, DifficultyManager.getDifficulty(750f))
        assertEquals(DifficultyLevel.HARD, DifficultyManager.getDifficulty(2000f))
    }

    @Test
    fun scrollSpeed_increasesAsDistanceProgresses() {
        val speedAtStart = DifficultyManager.getScrollSpeed(0f)
        val speedAtMid = DifficultyManager.getScrollSpeed(300f)
        val speedAtHigh = DifficultyManager.getScrollSpeed(1000f)

        assertTrue("Mid speed should be higher than start", speedAtMid > speedAtStart)
        assertTrue("High speed should be higher than mid", speedAtHigh > speedAtMid)
    }

    @Test
    fun baseScoreCalculation_multipliesDistanceAccurately() {
        assertEquals(0, ScoreCalculator.calculateBaseScore(0f))
        assertEquals(100, ScoreCalculator.calculateBaseScore(10f))
        assertEquals(2504, ScoreCalculator.calculateBaseScore(250.4f))
    }

    @Test
    fun milestoneTrigger_awardsBonusAndStreakOnCrossingThreshold() {
        // Milestone at 250m: crossing from 248m to 252m
        val milestone = ScoreCalculator.checkMilestone(
            previousDistance = 248f,
            currentDistance = 252f,
            currentStreak = 0
        )

        assertNotNull("Milestone should be triggered", milestone)
        assertEquals(250f, milestone!!.milestoneDistance, 0.01f)
        assertEquals(1, milestone.streakIncrease)
        assertTrue(milestone.bonusScore > 0)
        assertTrue(milestone.message.contains("250m"))

        // Not crossing milestone: from 252m to 255m
        val noMilestone = ScoreCalculator.checkMilestone(
            previousDistance = 252f,
            currentDistance = 255f,
            currentStreak = 1
        )
        assertNull("Should not trigger when threshold not crossed", noMilestone)
    }

    @Test
    fun centerBonusCalculation_onlyAppliesNearCenter() {
        val bonusInCenter = ScoreCalculator.calculateCenterBonus(
            isNearCenter = true,
            deltaTimeSec = 1.0f,
            streak = 2
        )
        assertTrue("Should award bonus when in center", bonusInCenter > 0)

        val bonusOutside = ScoreCalculator.calculateCenterBonus(
            isNearCenter = false,
            deltaTimeSec = 1.0f,
            streak = 2
        )
        assertEquals(0, bonusOutside)
    }
}
