package com.example.neondrift.domain.model

data class PathSegment(
    val index: Int,
    val worldY: Float,
    val centerX: Float,
    val width: Float
) {
    val leftBound: Float get() = centerX - (width / 2f)
    val rightBound: Float get() = centerX + (width / 2f)
}

data class PathSample(
    val worldY: Float,
    val screenY: Float,
    val centerX: Float,
    val width: Float,
    val leftX: Float,
    val rightX: Float
)
