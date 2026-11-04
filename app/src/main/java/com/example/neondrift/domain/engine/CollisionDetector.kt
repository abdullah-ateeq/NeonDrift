package com.example.neondrift.domain.engine

data class CollisionResult(
    val hasCollided: Boolean,
    val hitLeft: Boolean,
    val hitRight: Boolean,
    val distanceToLeft: Float,
    val distanceToRight: Float,
    val centerOffset: Float // positive = to the right of center, negative = to the left
)

object CollisionDetector {

    /**
     * Checks if player collides with path boundaries at the current path cross-section.
     *
     * @param playerX Current horizontal position of player
     * @param pathCenterX Center X of the path at player's position
     * @param pathWidth Total width of the path at player's position
     * @param playerRadius Visual radius of player
     * @param forgivenessRadius Grace margin subtracted from player collision radius (e.g. 6-10px)
     */
    fun checkCollision(
        playerX: Float,
        pathCenterX: Float,
        pathWidth: Float,
        playerRadius: Float = 14f,
        forgivenessRadius: Float = 8f
    ): CollisionResult {
        val halfWidth = pathWidth / 2f
        val leftBoundary = pathCenterX - halfWidth
        val rightBoundary = pathCenterX + halfWidth

        // Effective radius for collision (forgiving so edge grazes don't feel unfair)
        val effectiveRadius = (playerRadius - forgivenessRadius).coerceAtLeast(0f)

        val playerLeftEdge = playerX - effectiveRadius
        val playerRightEdge = playerX + effectiveRadius

        val hitLeft = playerLeftEdge <= leftBoundary
        val hitRight = playerRightEdge >= rightBoundary

        val distToLeft = playerX - leftBoundary
        val distToRight = rightBoundary - playerX
        val centerOffset = playerX - pathCenterX

        return CollisionResult(
            hasCollided = hitLeft || hitRight,
            hitLeft = hitLeft,
            hitRight = hitRight,
            distanceToLeft = distToLeft,
            distanceToRight = distToRight,
            centerOffset = centerOffset
        )
    }

    /**
     * Checks if player is within precision center zone (e.g. within 25% of path width from center).
     */
    fun isNearCenter(playerX: Float, pathCenterX: Float, pathWidth: Float): Boolean {
        val tolerance = pathWidth * 0.22f
        return kotlin.math.abs(playerX - pathCenterX) <= tolerance
    }
}
