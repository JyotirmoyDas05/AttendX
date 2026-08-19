package `in`.jyotirmoy.attendx.tools.presentation.pomodoro.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import `in`.jyotirmoy.attendx.tools.data.model.Stat
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.util.millisecondsToHoursMinutes
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@Composable
fun FocusBreakdownTooltip(
    item: Stat,
    rankList: List<Int>,
    dateFormat: DateTimeFormatter,
    minutesFormat: String = "%1\$dm",
    hoursMinutesFormat: String = "%1\$dh %2\$dm",
    onDismissRequest: (() -> Unit)
) {
    val tooltipOffset = with(LocalDensity.current) {
        (16 * 2 +
                typography.titleSmall.lineHeight.value + 4 +
                typography.bodyMedium.lineHeight.value + 8 +
                HORIZONTAL_STACKED_BAR_HEIGHT.value +
                8).dp.toPx().roundToInt()
    }
    val values = listOf(
        item.focusTimeQ1,
        item.focusTimeQ2,
        item.focusTimeQ3,
        item.focusTimeQ4
    )
    Popup(
        alignment = Alignment.TopCenter,
        offset = IntOffset(0, -tooltipOffset),
        onDismissRequest = onDismissRequest
    ) {
        Surface(
            shape = shapes.large,
            color = colorScheme.surfaceContainer,
            contentColor = colorScheme.onSurfaceVariant,
            shadowElevation = 3.dp,
            tonalElevation = 3.dp,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = item.date.format(dateFormat),
                    style = typography.titleSmall
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = millisecondsToHoursMinutes(
                        item.totalFocusTime(),
                        hoursMinutesFormat
                    ),
                    style = typography.bodyMedium
                )
                Spacer(Modifier.height(8.dp))
                HorizontalStackedBar(
                    values = values,
                    rankList = rankList,
                    minutesFormat = minutesFormat,
                    hoursMinutesFormat = hoursMinutesFormat
                )
            }
        }
    }
}
