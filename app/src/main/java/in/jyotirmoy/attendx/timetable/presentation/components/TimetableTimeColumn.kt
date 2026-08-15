package `in`.jyotirmoy.attendx.timetable.presentation.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

fun formatHour12(hour: Int): String {
    val period = if (hour < 12) "am" else "pm"
    val displayHour = when (hour) {
        0 -> 12
        in 1..12 -> hour
        else -> hour - 12
    }
    return "$displayHour $period"
}

@Composable
fun TimetableTimeColumn(
    hours: List<Int>,
    hourHeight: Dp,
    width: Dp,
    scrollState: ScrollState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(width)
            .verticalScroll(scrollState)
    ) {
        hours.forEach { hour ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(hourHeight)
                    .padding(end = 6.dp)
            ) {
                Text(
                    text = formatHour12(hour),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(y = (-7).dp)
                )
            }
        }
    }
}
