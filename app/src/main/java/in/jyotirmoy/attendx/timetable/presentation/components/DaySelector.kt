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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.jyotirmoy.attendx.core.presentation.util.bounceClick
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun DaySelector(
    selectedDay: Int,
    onDaySelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val todayDow = today.dayOfWeek.value

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Mon (1) to Sun (7)
        for (day in 1..7) {
            val dayOfWeek = DayOfWeek.of(day)
            val dayNameShort = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase()
            val isSelected = day == selectedDay
            val isToday = day == todayDow

            val dayOffset = (day - todayDow).toLong()
            val dayDate = today.plusDays(dayOffset)

            DayItem(
                dayName = dayNameShort,
                dayOfMonth = dayDate.dayOfMonth,
                isSelected = isSelected,
                isToday = isToday,
                onClick = { onDaySelected(day) }
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
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .bounceClick(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        Text(
            text = dayName,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
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
                color = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    isToday -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}
