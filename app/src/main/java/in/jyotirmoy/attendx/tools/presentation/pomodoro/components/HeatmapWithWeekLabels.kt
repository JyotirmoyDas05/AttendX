package `in`.jyotirmoy.attendx.tools.presentation.pomodoro.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMaxBy
import `in`.jyotirmoy.attendx.tools.data.model.Stat
import java.time.DayOfWeek
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

val HEATMAP_CELL_SIZE = 28.dp
val HEATMAP_CELL_GAP = 2.dp

@Composable
fun HeatmapWithWeekLabels(
    data: List<Stat?>,
    averageRankList: List<Int>,
    minutesFormat: String = "%1\$dm",
    hoursMinutesFormat: String = "%1\$dh %2\$dm",
    modifier: Modifier = Modifier,
    state: LazyGridState = rememberLazyGridState(Int.MAX_VALUE),
    size: Dp = HEATMAP_CELL_SIZE,
    gap: Dp = HEATMAP_CELL_GAP,
    contentPadding: PaddingValues = PaddingValues(),
    maxValue: Long = remember(data) {
        data.fastMaxBy { it?.totalFocusTime() ?: 0 }?.totalFocusTime() ?: 0
    }
) {
    val locale = Locale.getDefault()
    val shapes = shapes

    val daysOfWeek = remember(locale) {
        DayOfWeek.entries.map {
            it.getDisplayName(
                TextStyle.NARROW,
                locale
            )
        }
    }

    val dateFormat = remember(locale) {
        DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(locale)
    }

    var selectedItemIndex by remember { mutableIntStateOf(-1) }

    Row(modifier) {
        Column(
            verticalArrangement = Arrangement.spacedBy(gap),
        ) {
            daysOfWeek.fastForEach {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(size)
                ) {
                    Text(
                        text = it,
                        style = typography.labelSmall
                    )
                }
            }
        }
        LazyHorizontalGrid(
            state = state,
            rows = GridCells.Fixed(7),
            modifier = Modifier
                .height(size * 7 + gap * 6)
                .clip(shapes.small.copy(topEnd = CornerSize(0), bottomEnd = CornerSize(0))),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(gap),
            horizontalArrangement = Arrangement.spacedBy(gap)
        ) {
            itemsIndexed(
                items = data,
                key = { index, it -> it?.date?.toEpochDay() ?: index.toString() },
                contentType = { _, it -> if (it == null) "spacer" else "cell" }
            ) { index, it ->
                if (it == null) {
                    Spacer(Modifier.size(size))
                } else {
                    val sum = it.totalFocusTime()
                    val background = sum > 0
                    val shape = remember(data) {
                        val top = data.getOrNull(index - 1) != null && index % 7 != 0
                        val end = data.getOrNull(index + 7) != null
                        val bottom = data.getOrNull(index + 1) != null && index % 7 != 6
                        val start = data.getOrNull(index - 7) != null

                        RoundedCornerShape(
                            topStart = if (top || start) shapes.extraSmall.topStart else shapes.small.topStart,
                            topEnd = if (top || end) shapes.extraSmall.topEnd else shapes.small.topEnd,
                            bottomStart = if (bottom || start) shapes.extraSmall.bottomStart else shapes.small.bottomStart,
                            bottomEnd = if (bottom || end) shapes.extraSmall.bottomEnd else shapes.small.bottomEnd
                        )
                    }

                    val isTooltipVisible = it != null && selectedItemIndex == index

                    Box(
                        Modifier
                            .size(size)
                            .then(
                                if (background) Modifier.background(
                                    colorScheme.primary.copy(
                                        alpha = (0.25f + (0.75f * sum.toFloat() / maxValue.toFloat())).coerceIn(
                                            0.25f,
                                            1f
                                        )
                                    ),
                                    if (!isTooltipVisible) shape else CircleShape
                                )
                                else Modifier.background(colorScheme.surfaceVariant, shape)
                            )
                            .clickable {
                                selectedItemIndex = index
                            }
                    ) {
                        if (isTooltipVisible) {
                            FocusBreakdownTooltip(
                                it,
                                averageRankList,
                                dateFormat,
                                minutesFormat,
                                hoursMinutesFormat
                            ) { selectedItemIndex = -1 }
                        }
                    }
                }
            }
        }
    }
}
