package com.example.neondrift

import com.example.neondrift.domain.engine.CollisionDetector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CollisionAndMovementTest {

    private val pathCenterX = 500f
    private val pathWidth = 400f // Left bound = 300f, Right bound = 700f

    @Test
    fun playerInCenter_doesNotCollide() {
        val result = CollisionDetector.checkCollision(
            playerX = 500f,
            pathCenterX = pathCenterX,
            pathWidth = pathWidth,
            playerRadius = 14f,
            forgivenessRadius = 6f
        )

        assertFalse(result.hasCollided)
        assertFalse(result.hitLeft)
        assertFalse(result.hitRight)
        assertEquals(0f, result.centerOffset, 0.01f)
    }

    @Test
    fun playerExceedingLeftBound_triggersCollision() {
        // Left bound is 300f. Effective radius is 14 - 6 = 8f.
        // At playerX = 290f, playerLeftEdge = 282f <= 300f.
        val result = CollisionDetector.checkCollision(
            playerX = 290f,
            pathCenterX = pathCenterX,
            pathWidth = pathWidth,
            playerRadius = 14f,
            forgivenessRadius = 6f
        )

        assertTrue(result.hasCollided)
        assertTrue(result.hitLeft)
        assertFalse(result.hitRight)
    }

    @Test
    fun playerExceedingRightBound_triggersCollision() {
        // Right bound is 700f. Effective radius is 8f.
        // At playerX = 710f, playerRightEdge = 718f >= 700f.
        val result = CollisionDetector.checkCollision(
            playerX = 710f,
            pathCenterX = pathCenterX,
            pathWidth = pathWidth,
            playerRadius = 14f,
            forgivenessRadius = 6f
        )

        assertTrue(result.hasCollided)
        assertFalse(result.hitLeft)
        assertTrue(result.hitRight)
    }

    @Test
    fun forgivenessRadius_preventsPrematureCollisionOnMildEdgeGraze() {
        // Without forgiveness (radius 14f): playerX = 305f gives left edge = 291f (collides with 300f).
        // With forgiveness (radius 14f, forgiveness 10f -> effective radius 4f):
        // playerX = 305f gives left edge = 301f > 300f (safe!).
        val resultWithForgiveness = CollisionDetector.checkCollision(
            playerX = 305f,
            pathCenterX = pathCenterX,
            pathWidth = pathWidth,
            playerRadius = 14f,
            forgivenessRadius = 10f
        )

        assertFalse("Should be forgiven near border", resultWithForgiveness.hasCollided)
    }

    @Test
    fun precisionCenterDetection_recognizesNearCenter() {
        // Path width 400f, 22% tolerance is 88f.
        // Center is 500f. Range: 412f .. 588f.
        assertTrue(CollisionDetector.isNearCenter(500f, pathCenterX, pathWidth))
        assertTrue(CollisionDetector.isNearCenter(530f, pathCenterX, pathWidth))
        assertTrue(CollisionDetector.isNearCenter(470f, pathCenterX, pathWidth))

        assertFalse(CollisionDetector.isNearCenter(620f, pathCenterX, pathWidth))
        assertFalse(CollisionDetector.isNearCenter(350f, pathCenterX, pathWidth))
    }

    @Test
    fun horizontalBoundsClamping_keepsPlayerInsideScreen() {
        val screenWidth = 1080f
        val margin = 16f

        val extremeLeft = -50f
        val clampedLeft = extremeLeft.coerceIn(margin, screenWidth - margin)
        assertEquals(margin, clampedLeft, 0.01f)

        val extremeRight = 1200f
        val clampedRight = extremeRight.coerceIn(margin, screenWidth - margin)
        assertEquals(screenWidth - margin, clampedRight, 0.01f)
    }
}
