package com.example.neondrift.domain.engine

import com.example.neondrift.domain.model.PathSegment
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class PathGenerator(
    val screenWidth: Float,
    val segmentSpacing: Float = 200f,
    private val random: Random = Random(System.currentTimeMillis())
) {

    /**
     * Initializes the starting segment list with a straight entry path.
     */
    fun createInitialPath(count: Int = 15): List<PathSegment> {
        val segments = ArrayList<PathSegment>(count)
        val initialWidth = DifficultyManager.getPathWidth(0f, screenWidth)
        val centerX = screenWidth / 2f

        for (i in 0 until count) {
            val worldY = i * segmentSpacing
            segments.add(
                PathSegment(
                    index = i,
                    worldY = worldY,
                    centerX = centerX,
                    width = initialWidth
                )
            )
        }
        return segments
    }

    /**
     * Appends the next segment, ensuring reachable turn angles and staying within screen bounds.
     */
    fun generateNextSegment(
        previousSegments: List<PathSegment>,
        distanceMeters: Float
    ): PathSegment {
        require(previousSegments.isNotEmpty()) { "Previous segments list cannot be empty" }

        val last = previousSegments.last()
        val nextIndex = last.index + 1
        val nextWorldY = last.worldY + segmentSpacing

        val nextWidth = DifficultyManager.getPathWidth(distanceMeters, screenWidth)
        val maxTurnDelta = DifficultyManager.getMaxTurnDelta(distanceMeters, screenWidth)

        // Safety margins from screen edges
        val edgeMargin = (nextWidth / 2f) + 20f
        val minX = edgeMargin
        val maxX = max(minX, screenWidth - edgeMargin)

        // Generate lateral shift with mild persistence towards screen center if getting close to edge
        val centerBias = if (last.centerX < screenWidth * 0.35f) {
            0.3f
        } else if (last.centerX > screenWidth * 0.65f) {
            -0.3f
        } else {
            0.0f
        }

        val rawDelta = (random.nextFloat() * 2f - 1f) * maxTurnDelta
        val biasedDelta = rawDelta + (centerBias * maxTurnDelta * 0.5f)

        val targetCenterX = (last.centerX + biasedDelta).coerceIn(minX, maxX)

        return PathSegment(
            index = nextIndex,
            worldY = nextWorldY,
            centerX = targetCenterX,
            width = nextWidth
        )
    }

    companion object {
        /**
         * Computes interpolated centerX and width at any given worldY.
         */
        fun samplePath(
            worldY: Float,
            segments: List<PathSegment>
        ): Pair<Float, Float> {
            if (segments.isEmpty()) return Pair(0f, 0f)
            if (segments.size == 1) return Pair(segments[0].centerX, segments[0].width)

            // Clamp to ends if worldY is outside current segment bounds
            if (worldY <= segments.first().worldY) {
                return Pair(segments.first().centerX, segments.first().width)
            }
            if (worldY >= segments.last().worldY) {
                return Pair(segments.last().centerX, segments.last().width)
            }

            // Find segment interval containing worldY
            var idx = 0
            while (idx < segments.size - 1 && segments[idx + 1].worldY < worldY) {
                idx++
            }

            val p1 = segments[idx]
            val p2 = segments[idx + 1]

            val span = p2.worldY - p1.worldY
            val t = if (span <= 0f) 0f else ((worldY - p1.worldY) / span).coerceIn(0f, 1f)

            // Optional 4-point Catmull-Rom spline for extra organic curvature
            val p0 = if (idx > 0) segments[idx - 1] else p1
            val p3 = if (idx + 2 < segments.size) segments[idx + 2] else p2

            val interpolatedCenter = catmullRom(p0.centerX, p1.centerX, p2.centerX, p3.centerX, t)

            // Smooth linear/hermite for width
            val smoothT = t * t * (3f - 2f * t)
            val interpolatedWidth = p1.width + (p2.width - p1.width) * smoothT

            return Pair(interpolatedCenter, interpolatedWidth)
        }

        /**
         * Standard Catmull-Rom spline formula for smooth C1 continuous curves.
         */
        fun catmullRom(p0: Float, p1: Float, p2: Float, p3: Float, t: Float): Float {
            val t2 = t * t
            val t3 = t2 * t
            return 0.5f * (
                (2f * p1) +
                (-p0 + p2) * t +
                (2f * p0 - 5f * p1 + 4f * p2 - p3) * t2 +
                (-p0 + 3f * p1 - 3f * p2 + p3) * t3
            )
        }
    }
}
