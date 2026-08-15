package `in`.jyotirmoy.attendx.timetable.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

@Composable
fun TimetableLiveTimeIndicator(
    columnIndex: Int,
    cellWidthPx: Float,
    hourHeightPx: Float,
    gridStartMinutes: Int,
    currentMinutes: Int,
    cellWidth: Dp,
    modifier: Modifier = Modifier
) {
    val indicatorColor = MaterialTheme.colorScheme.primary
    val pulseScale = remember { Animatable(0.8f) }

    LaunchedEffect(Unit) {
        pulseScale.animateTo(
            targetValue = 1.3f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    val indicatorYPx = ((currentMinutes - gridStartMinutes).toFloat() / 60f) * hourHeightPx
    val columnXPx = columnIndex * cellWidthPx

    Box(modifier = modifier) {
        // Pulsing Dot on left edge of today's column
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        (columnXPx - 5.dp.toPx()).toInt(),
                        (indicatorYPx - 5.dp.toPx()).toInt()
                    )
                }
                .size(10.dp)
                .clip(CircleShape)
                .background(indicatorColor.copy(alpha = 0.4f))
        )

        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        (columnXPx - 3.dp.toPx()).toInt(),
                        (indicatorYPx - 3.dp.toPx()).toInt()
                    )
                }
                .size(6.dp)
                .clip(CircleShape)
                .background(indicatorColor)
        )

        // Line across today's column
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        columnXPx.toInt(),
                        indicatorYPx.toInt()
                    )
                }
                .width(cellWidth)
                .height(2.dp)
                .background(indicatorColor)
        )
    }
}
