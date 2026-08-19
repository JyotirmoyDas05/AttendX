package `in`.jyotirmoy.attendx.tools.presentation.pomodoro.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import `in`.jyotirmoy.attendx.tools.data.model.Stat
import java.time.DayOfWeek
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

val CALENDAR_CELL_SIZE = 40.dp
val CALENDAR_CELL_HORIZONTAL_GAP = 2.dp
val CALENDAR_CELL_VERTICAL_GAP = 4.dp
val CALENDAR_INTERNAL_PADDING = 20.dp

@Composable
fun FocusHistoryCalendar(
    data: List<Stat?>,
    averageRankList: List<Int>,
    minutesFormat: String = "%1\$dm",
    hoursMinutesFormat: String = "%1\$dh %2\$dm",
    modifier: Modifier = Modifier,
    size: Dp = CALENDAR_CELL_SIZE,
    horizontalGap: Dp = CALENDAR_CELL_HORIZONTAL_GAP,
    verticalGap: Dp = CALENDAR_CELL_VERTICAL_GAP,
    internalPadding: Dp = CALENDAR_INTERNAL_PADDING
) {
    val locale = Locale.getDefault()
    val shapes = shapes
    val last = data.lastOrNull { it != null }

    val daysOfWeek = remember(locale) {
        DayOfWeek.entries.map {
            it.getDisplayName(
                TextStyle.SHORT,
                locale
            )
        }
    }

    val dateFormat = remember(locale) {
        DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(locale)
    }

    val groupedData = remember(data) {
        data.chunked(7)
    }
    var selectedItemIndex by remember { mutableIntStateOf(-1) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(verticalGap),
        modifier = modifier
            .fillMaxWidth()
            .background(colorScheme.surfaceContainerLow, shapes.large)
            .horizontalScroll(rememberScrollState())
            .padding(internalPadding)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(horizontalGap)) {
            daysOfWeek.fastForEach {
                Text(
                    text = it,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    style = typography.bodySmall,
                    color = colorScheme.outline,
                    modifier = Modifier.width(size)
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(verticalGap)) {
            groupedData.fastForEachIndexed { baseIndex, items ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(horizontalGap),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.height(size)
                ) {
                    items.fastForEachIndexed { index, it ->
                        val sum = remember(it) { it?.totalFocusTime() ?: 0L }
                        val background = sum > 0

                        val currentMonth =
                            remember(it, last) { it?.date?.month == last?.date?.month }
                        val flatIndex = baseIndex * 7 + index

                        val shape = remember(data) {
                            if (background) {
                                val next =
                                    (data.getOrNull(flatIndex + 1)?.totalFocusTime() ?: 0) > 0
                                val previous =
                                    (data.getOrNull(flatIndex - 1)?.totalFocusTime() ?: 0) > 0

                                RoundedCornerShape(
                                    topStart = if (previous) shapes.extraSmall.topStart else shapes.large.topStart,
                                    topEnd = if (next) shapes.extraSmall.topEnd else shapes.large.topEnd,
                                    bottomStart = if (previous) shapes.extraSmall.bottomStart else shapes.large.bottomStart,
                                    bottomEnd = if (next) shapes.extraSmall.bottomEnd else shapes.large.bottomEnd
                                )
                            } else RoundedCornerShape(0)
                        }

                        val isTooltipVisible = it != null && selectedItemIndex == flatIndex

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(size)
                                .then(
                                    if (background) Modifier.background(
                                        if (currentMonth) colorScheme.primaryContainer
                                        else colorScheme.secondaryContainer,
                                        if (isTooltipVisible) CircleShape else shape
                                    )
                                    else Modifier
                                )
                                .clickable(enabled = it != null) {
                                    selectedItemIndex = flatIndex
                                }
                        ) {
                            Text(
                                text = it?.date?.dayOfMonth?.toString() ?: "",
                                color =
                                    if (currentMonth) {
                                        if (background) colorScheme.onPrimaryContainer
                                        else colorScheme.onSurface
                                    } else {
                                        if (background) colorScheme.onSecondaryContainer
                                        else colorScheme.outline
                                    }
                            )

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
}
