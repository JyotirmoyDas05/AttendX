package `in`.jyotirmoy.attendx.tools.presentation.pomodoro.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.window.Popup
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.util.millisecondsToHoursMinutes
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.util.millisecondsToMinutes
import kotlin.math.roundToInt

val HORIZONTAL_STACKED_BAR_HEIGHT = 40.dp

@Composable
fun HorizontalStackedBar(
    values: List<Long>,
    minutesFormat: String = "%dm",
    hoursMinutesFormat: String = "%1\$dh %2\$dm",
    modifier: Modifier = Modifier,
    rankList: List<Int> = remember(values) {
        val sortedIndices = values.indices.sortedByDescending { values[it] }
        val ranks = MutableList(values.size) { 0 }
        sortedIndices.forEachIndexed { rank, originalIndex ->
            ranks[originalIndex] = rank
        }
        ranks
    },
    labelFormatter: @Composable (Int, Long, Long) -> String = { index, value, total ->
        buildString {
            append(
                when (index) {
                    0 -> "[00:00 - 06:00] "
                    1 -> "[06:00 - 12:00] "
                    2 -> "[12:00 - 18:00] "
                    else -> "[18:00 - 24:00] "
                }
            )
            if (value < 60 * 60 * 1000)
                append(millisecondsToMinutes(value, minutesFormat))
            else
                append(millisecondsToHoursMinutes(value, hoursMinutesFormat))
            if (total > 0) {
                append(" (%.1f%%)".format((value.toFloat() / total) * 100))
            }
        }
    },
    height: Dp = HORIZONTAL_STACKED_BAR_HEIGHT,
    gap: Dp = 2.dp
) {
    val shapes = MaterialTheme.shapes
    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val valuesTotal = remember(values) { values.sum() }
    val firstNonZeroIndex = remember(values) { values.indexOfFirst { it > 0L } }
    val lastNonZeroIndex = remember(values) { values.indexOfLast { it > 0L } }
    val tooltipOffset = with(LocalDensity.current) { (28).dp.toPx().roundToInt() }

    if (firstNonZeroIndex != -1) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(gap),
            modifier = modifier.height(height)
        ) {
            values.fastForEachIndexed { index, item ->
                if (item > 0L) {
                    var showTooltip by remember { mutableStateOf(false) }
                    val shape = remember(index, firstNonZeroIndex, lastNonZeroIndex) {
                        if (firstNonZeroIndex == lastNonZeroIndex) shapes.large
                        else when (index) {
                            firstNonZeroIndex -> shapes.large.copy(
                                topEnd = shapes.extraSmall.topEnd,
                                bottomEnd = shapes.extraSmall.bottomEnd
                            )
                            lastNonZeroIndex -> shapes.large.copy(
                                topStart = shapes.extraSmall.topStart,
                                bottomStart = shapes.extraSmall.bottomStart
                            )
                            else -> shapes.extraSmall
                        }
                    }
                    Box(
                        Modifier
                            .weight(item.toFloat())
                            .height(height)
                            .clip(shape)
                            .background(colorScheme.surfaceVariant)
                            .background(
                                colorScheme.primary.copy(
                                    alpha = (1f - (rankList.getOrNull(index) ?: 0) * 0.15f).coerceIn(0.2f, 1f)
                                )
                            )
                            .clickable { showTooltip = true }
                    ) {
                        if (showTooltip) {
                            Popup(
                                alignment = Alignment.TopCenter,
                                offset = IntOffset(0, -tooltipOffset),
                                onDismissRequest = { showTooltip = false }
                            ) {
                                Text(
                                    text = labelFormatter(index, item, valuesTotal),
                                    style = typography.bodySmall,
                                    color = colorScheme.inverseOnSurface,
                                    modifier = Modifier
                                        .padding(horizontal = 8.dp)
                                        .background(
                                            color = colorScheme.inverseSurface,
                                            shape = shapes.extraSmall
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    } else {
        Spacer(
            modifier
                .fillMaxWidth()
                .height(height)
                .clip(shapes.large)
                .background(colorScheme.surfaceVariant)
        )
    }
}

@Composable
fun FocusBreakRatioVisualization(
    focusDuration: Long,
    breakDuration: Long,
    modifier: Modifier = Modifier,
    height: Dp = HORIZONTAL_STACKED_BAR_HEIGHT,
    gap: Dp = 2.dp
) {
    val colorScheme = MaterialTheme.colorScheme
    val shapes = MaterialTheme.shapes
    val typography = MaterialTheme.typography

    if (focusDuration + breakDuration > 0) {
        val focusPercentage = ((focusDuration / (focusDuration.toFloat() + breakDuration)) * 100)
        val breakPercentage = 100 - focusPercentage

        val focusShape = remember(breakDuration) {
            if (breakDuration > 0) shapes.large.copy(
                topEnd = shapes.extraSmall.topEnd,
                bottomEnd = shapes.extraSmall.bottomEnd
            ) else shapes.large
        }
        val breakShape = remember(focusDuration) {
            if (focusDuration > 0) shapes.large.copy(
                topStart = shapes.extraSmall.topStart,
                bottomStart = shapes.extraSmall.bottomStart
            ) else shapes.large
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(gap),
            modifier = modifier
        ) {
            Text(
                text = "${focusPercentage.roundToInt()}%",
                style = typography.bodyLarge,
                color = colorScheme.primary,
                modifier = Modifier.padding(end = 6.dp)
            )
            if (focusDuration > 0) {
                Spacer(
                    Modifier
                        .weight(focusPercentage.coerceAtLeast(1f))
                        .height(height)
                        .background(colorScheme.primary, focusShape)
                )
            }
            if (breakDuration > 0) {
                Spacer(
                    Modifier
                        .weight(breakPercentage.coerceAtLeast(1f))
                        .height(height)
                        .background(colorScheme.tertiary, breakShape)
                )
            }
            Text(
                text = "${breakPercentage.roundToInt()}%",
                style = typography.bodyLarge,
                color = colorScheme.tertiary,
                modifier = Modifier.padding(start = 6.dp)
            )
        }
    }
}
