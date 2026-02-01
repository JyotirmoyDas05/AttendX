package `in`.jyotirmoy.attendx.core.presentation.components.svg.vectors

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import `in`.jyotirmoy.attendx.core.presentation.components.svg.DynamicColorImageVectors

val DynamicColorImageVectors.LinkedIn: ImageVector
    get() {
        if (_linkedIn != null) {
            return _linkedIn!!
        }
        _linkedIn = ImageVector.Builder(
            name = "LinkedIn",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)),
                fillAlpha = 1.0f,
                stroke = null,
                strokeAlpha = 1.0f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(19.0f, 0.0f)
                horizontalLineTo(5.0f)
                curveTo(2.239f, 0.0f, 0.0f, 2.239f, 0.0f, 5.0f)
                verticalLineTo(19.0f)
                curveTo(0.0f, 21.761f, 2.239f, 24.0f, 5.0f, 24.0f)
                horizontalLineTo(19.0f)
                curveTo(21.762f, 24.0f, 24.0f, 21.761f, 24.0f, 19.0f)
                verticalLineTo(5.0f)
                curveTo(24.0f, 2.239f, 21.762f, 0.0f, 19.0f, 0.0f)
                close()
                moveTo(8.0f, 19.0f)
                horizontalLineTo(5.0f)
                verticalLineTo(8.0f)
                horizontalLineTo(8.0f)
                verticalLineTo(19.0f)
                close()
                moveTo(6.5f, 6.732f)
                curveTo(5.534f, 6.732f, 4.75f, 5.942f, 4.75f, 4.968f)
                curveTo(4.75f, 3.994f, 5.534f, 3.204f, 6.5f, 3.204f)
                curveTo(7.466f, 3.204f, 8.25f, 3.994f, 8.25f, 4.968f)
                curveTo(8.25f, 5.942f, 7.466f, 6.732f, 6.5f, 6.732f)
                close()
                moveTo(20.0f, 19.0f)
                horizontalLineTo(17.0f)
                verticalLineTo(13.396f)
                curveTo(17.0f, 10.028f, 13.0f, 10.283f, 13.0f, 13.396f)
                verticalLineTo(19.0f)
                horizontalLineTo(10.0f)
                verticalLineTo(8.0f)
                horizontalLineTo(13.0f)
                verticalLineTo(9.765f)
                curveTo(14.396f, 7.179f, 20.0f, 7.049f, 20.0f, 12.232f)
                verticalLineTo(19.0f)
                close()
            }
        }.build()
        return _linkedIn!!
    }

private var _linkedIn: ImageVector? = null
