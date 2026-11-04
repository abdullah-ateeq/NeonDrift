package com.example.neondrift

import com.example.neondrift.domain.engine.DifficultyManager
import com.example.neondrift.domain.engine.PathGenerator
import com.example.neondrift.domain.model.PathSegment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class PathEngineTest {

    private val screenWidth = 1080f
    private val generator = PathGenerator(screenWidth = screenWidth, segmentSpacing = 200f, random = Random(12345))

    @Test
    fun initialPathGeneration_createsExpectedNumberOfSegments() {
        val count = 16
        val path = generator.createInitialPath(count)

        assertEquals(count, path.size)
        assertEquals(0, path.first().index)
        assertEquals(count - 1, path.last().index)
        assertEquals(0f, path.first().worldY, 0.01f)
        assertEquals((count - 1) * 200f, path.last().worldY, 0.01f)
    }

    @Test
    fun nextSegmentGeneration_staysWithinScreenMargins() {
        var path = generator.createInitialPath(10)

        // Generate 100 consecutive segments across increasing difficulty
        for (i in 0 until 100) {
            val distMeters = i * 20f
            val nextSeg = generator.generateNextSegment(path, distMeters)

            val halfW = nextSeg.width / 2f
            val leftEdge = nextSeg.centerX - halfW
            val rightEdge = nextSeg.centerX + halfW

            // Boundaries must stay strictly within screen boundaries with safety margins
            assertTrue("Left boundary should be inside screen: $leftEdge", leftEdge >= 0f)
            assertTrue("Right boundary should be inside screen: $rightEdge", rightEdge <= screenWidth)

            path = path + nextSeg
        }
    }

    @Test
    fun pathWidthReduction_narrowsGraduallyWithDistance() {
        val startWidth = DifficultyManager.getPathWidth(0f, screenWidth)
        val midWidth = DifficultyManager.getPathWidth(500f, screenWidth)
        val hardWidth = DifficultyManager.getPathWidth(1500f, screenWidth)
        val maxDistWidth = DifficultyManager.getPathWidth(3000f, screenWidth)

        assertTrue("Start width should be greater than mid width", startWidth > midWidth)
        assertTrue("Mid width should be greater than hard width", midWidth > hardWidth)
        assertTrue("Hard width should be equal or close to minimum limit", hardWidth <= midWidth)
        assertEquals(hardWidth, maxDistWidth, 0.01f)
    }

    @Test
    fun pathInterpolation_returnsSmoothIntermediateValues() {
        val seg0 = PathSegment(index = 0, worldY = 0f, centerX = 500f, width = 400f)
        val seg1 = PathSegment(index = 1, worldY = 200f, centerX = 600f, width = 380f)
        val seg2 = PathSegment(index = 2, worldY = 400f, centerX = 700f, width = 360f)
        val seg3 = PathSegment(index = 3, worldY = 600f, centerX = 650f, width = 340f)

        val segments = listOf(seg0, seg1, seg2, seg3)

        // Sample halfway between seg1 and seg2 (worldY = 300f)
        val (sampleCenterX, sampleWidth) = PathGenerator.samplePath(300f, segments)

        assertTrue("Sample center should be between 600 and 700", sampleCenterX in 600f..700f)
        assertTrue("Sample width should be between 360 and 380", sampleWidth in 360f..380f)
    }

    @Test
    fun catmullRomSpline_isContinuous() {
        val p0 = 100f
        val p1 = 200f
        val p2 = 300f
        val p3 = 400f

        val start = PathGenerator.catmullRom(p0, p1, p2, p3, 0f)
        val end = PathGenerator.catmullRom(p0, p1, p2, p3, 1f)

        assertEquals(p1, start, 0.001f)
        assertEquals(p2, end, 0.001f)
    }
}
