package `in`.jyotirmoy.attendx.timetable.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TimetableDaysHeader(
    dates: List<LocalDate>,
    timeColumnWidth: Dp,
    today: LocalDate,
    onDayClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    badgeModifier: @Composable (LocalDate) -> Modifier = { Modifier }
) {

    Row(
        modifier = modifier
            .fillMaxWidth()
            // A minimum, not a fixed height: at a large font scale the weekday
            // letter and the date badge together need more than 54dp, and a hard
            // height cropped the numbers in half.
            .heightIn(min = 54.dp)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(vertical = 3.dp)
    ) {
        // Fixed time column offset
        Spacer(modifier = Modifier.width(timeColumnWidth))

        // Evenly distributed day headers matching grid columns
        Row(
            modifier = Modifier.weight(1f)
        ) {
            dates.forEach { dayDate ->
                val isToday = dayDate == today
                val dayNameShort = dayDate.dayOfWeek
                    .getDisplayName(TextStyle.SHORT, Locale.getDefault())
                    .uppercase()

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onDayClick(dayDate) }
                        .padding(vertical = 3.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (dates.size > 5) dayNameShort.take(1) else dayNameShort,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = if (dates.size > 5) 9.5.sp else 10.5.sp
                        ),
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // M3 Expressive Cookie9Sided Date Badge
                    Box(
                        modifier = Modifier
                            .then(badgeModifier(dayDate))
                            // Grows with the number inside instead of cropping it.
                            .defaultMinSize(
                                minWidth = if (dates.size > 5) 24.dp else 28.dp,
                                minHeight = if (dates.size > 5) 24.dp else 28.dp
                            )
                            .clip(MaterialShapes.Cookie9Sided.toShape())
                            .background(
                                if (isToday) MaterialTheme.colorScheme.primary
                                else Color.Transparent
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${dayDate.dayOfMonth}",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = if (dates.size > 5) 10.sp else 11.5.sp
                            ),
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1,
                            color = if (isToday) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
