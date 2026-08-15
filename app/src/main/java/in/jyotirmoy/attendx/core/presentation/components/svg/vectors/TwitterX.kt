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

val DynamicColorImageVectors.TwitterX: ImageVector
    get() {
        if (_twitterX != null) {
            return _twitterX!!
        }
        _twitterX = ImageVector.Builder(
            name = "TwitterX",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 16f,
            viewportHeight = 16f
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
                moveTo(12.6f, 0.75f)
                horizontalLineToRelative(2.454f)
                lineToRelative(-5.36f, 6.142f)
                lineTo(16.0f, 15.25f)
                horizontalLineToRelative(-4.937f)
                lineToRelative(-3.867f, -5.07f)
                lineToRelative(-4.425f, 5.07f)
                horizontalLineTo(0.316f)
                lineToRelative(5.733f, -6.57f)
                lineTo(0.0f, 0.75f)
                horizontalLineToRelative(5.063f)
                lineToRelative(3.495f, 4.633f)
                lineTo(12.601f, 0.75f)
                close()
                moveToRelative(-0.86f, 13.028f)
                horizontalLineToRelative(1.36f)
                lineTo(4.323f, 2.145f)
                horizontalLineTo(2.865f)
                close()
            }
        }.build()
        return _twitterX!!
    }

private var _twitterX: ImageVector? = null
