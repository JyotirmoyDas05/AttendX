/*
 * Vendored from Kyant0/Capsule
 * https://github.com/Kyant0/Capsule — Copyright 2025 Kyant0, Apache License 2.0
 * capsule/src/main/java/com/kyant/capsule/Continuity.kt
 *
 * Vendored so the shape ships as source with this app. Package renamed
 * accordingly (com.kyant.capsule -> `in`.jyotirmoy.attendx.core.ui.glass.shapes).
 */
package `in`.jyotirmoy.attendx.core.ui.glass.shapes

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import `in`.jyotirmoy.attendx.core.ui.glass.shapes.continuities.G2Continuity
import `in`.jyotirmoy.attendx.core.ui.glass.shapes.path.PathSegments
import `in`.jyotirmoy.attendx.core.ui.glass.shapes.path.toPath

@Immutable
interface Continuity {

    fun createRoundedRectanglePathSegments(
        width: Double,
        height: Double,
        topLeft: Double,
        topRight: Double,
        bottomRight: Double,
        bottomLeft: Double
    ): PathSegments

    fun createRoundedRectangleOutline(
        size: Size,
        topLeft: Float,
        topRight: Float,
        bottomRight: Float,
        bottomLeft: Float
    ): Outline {
        val path =
            createRoundedRectanglePathSegments(
                width = size.width.toDouble(),
                height = size.height.toDouble(),
                topLeft = topLeft.toDouble(),
                topRight = topRight.toDouble(),
                bottomRight = bottomRight.toDouble(),
                bottomLeft = bottomLeft.toDouble()
            ).toPath()
        return Outline.Generic(path)
    }

    fun createCapsuleOutline(size: Size): Outline {
        val radius = size.minDimension * 0.5f
        return createRoundedRectangleOutline(
            size = size,
            topLeft = radius,
            topRight = radius,
            bottomRight = radius,
            bottomLeft = radius
        )
    }

    fun lerp(stop: Continuity, fraction: Double): Continuity

    companion object {

        @Stable
        val Default: Continuity = G2Continuity()
    }
}
