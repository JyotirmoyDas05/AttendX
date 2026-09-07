package `in`.jyotirmoy.attendx.timetable.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.jyotirmoy.attendx.core.presentation.components.text.AutoResizeableText
import `in`.jyotirmoy.attendx.core.presentation.util.bounceClick
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale

@Composable
fun DaySelector(
    selectedDate: LocalDate,
    today: LocalDate,
    onDaySelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    // The week containing the selection, not the week containing today: the
    // agenda pages through dates, so the strip has to follow it rather than
    // staying pinned to this week.
    val weekStart = remember(selectedDate) {
        selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Monday through Sunday of the selected week
        for (offset in 0L until 7L) {
            val dayDate = weekStart.plusDays(offset)
            val dayNameShort = dayDate.dayOfWeek
                .getDisplayName(TextStyle.SHORT, Locale.getDefault())
                .uppercase()

            DayItem(
                dayName = dayNameShort,
                dayOfMonth = dayDate.dayOfMonth,
                isSelected = dayDate == selectedDate,
                isToday = dayDate == today,
                onClick = { onDaySelected(dayDate) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DayItem(
    dayName: String,
    dayOfMonth: Int,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .bounceClick(onClick = onClick)
            .padding(horizontal = 2.dp, vertical = 4.dp)
    ) {
        // softWrap = false with no room to shrink meant "MON" and "TUE" drew
        // over each other once text was scaled up; sizing down keeps all seven
        // legible and inside their own column.
        AutoResizeableText(
            text = dayName,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
            softWrap = false,
            textAlign = TextAlign.Center,
            color = when {
                isSelected -> MaterialTheme.colorScheme.primary
                isToday -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(4.dp))

        // M3 Expressive Cookie9Sided Date Badge
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(MaterialShapes.Cookie9Sided.toShape())
                .background(
                    when {
                        isSelected -> MaterialTheme.colorScheme.primary
                        isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        else -> Color.Transparent
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$dayOfMonth",
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                softWrap = false,
                color = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    isToday -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}
