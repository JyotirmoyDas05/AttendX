package `in`.jyotirmoy.attendx.core.presentation.components.svg

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

public fun DynamicColorImageVectors.linkedInLogo(): ImageVector {
    if (_linkedInLogo != null) {
        return _linkedInLogo!!
    }
    _linkedInLogo = ImageVector.Builder(
        name = "LinkedInLogo",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(
        fill = SolidColor(Color(0xFF0077B5)),
        fillAlpha = 1.0f,
        stroke = null,
        strokeAlpha = 1.0f,
        strokeLineWidth = 1.0f,
        strokeLineCap = StrokeCap.Butt,
        strokeLineJoin = StrokeJoin.Miter,
        strokeLineMiter = 1.0f,
        pathFillType = PathFillType.NonZero
    ) {
        moveTo(19f, 0f)
        horizontalLineTo(5f)
        curveTo(2.239f, 0f, 0f, 2.239f, 0f, 5f)
        verticalLineTo(19f)
        curveTo(0f, 21.761f, 2.239f, 24f, 5f, 24f)
        horizontalLineTo(19f)
        curveTo(21.761f, 24f, 24f, 21.761f, 24f, 19f)
        verticalLineTo(5f)
        curveTo(24f, 2.239f, 21.761f, 0f, 19f, 0f)
        close()
        moveTo(7.12f, 19f)
        horizontalLineTo(4.14f)
        verticalLineTo(9.54f)
        horizontalLineTo(7.12f)
        verticalLineTo(19f)
        close()
        moveTo(5.63f, 8.24f)
        curveTo(4.67f, 8.24f, 3.89f, 7.47f, 3.89f, 6.51f)
        curveTo(3.89f, 5.55f, 4.67f, 4.77f, 5.63f, 4.77f)
        curveTo(6.59f, 4.77f, 7.37f, 5.55f, 7.37f, 6.51f)
        curveTo(7.37f, 7.47f, 6.59f, 8.24f, 5.63f, 8.24f)
        close()
        moveTo(20.11f, 19f)
        horizontalLineTo(17.13f)
        verticalLineTo(14.33f)
        curveTo(17.13f, 13.22f, 17.11f, 11.79f, 15.58f, 11.79f)
        curveTo(14.04f, 11.79f, 13.8f, 13f, 13.8f, 14.25f)
        verticalLineTo(19f)
        horizontalLineTo(10.82f)
        verticalLineTo(9.54f)
        horizontalLineTo(13.68f)
        verticalLineTo(10.84f)
        horizontalLineTo(13.72f)
        curveTo(14.12f, 10.09f, 15.08f, 9.3f, 16.53f, 9.3f)
        curveTo(19.54f, 9.3f, 20.11f, 11.28f, 20.11f, 13.85f)
        verticalLineTo(19f)
        close()
    }.build()
    return _linkedInLogo!!
}

private var _linkedInLogo: ImageVector? = null
