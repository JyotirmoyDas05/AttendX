package `in`.jyotirmoy.attendx.timetable.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import `in`.jyotirmoy.attendx.timetable.data.model.TimeTableScheduleWithSubject

/**
 * Google Calendar-style synchronized grid view
 */
@Composable
fun TimetableCalendarView(
    weeklySchedule: Map<Int, List<TimeTableScheduleWithSubject>>,
    onClassClick: (TimeTableScheduleWithSubject) -> Unit,
    modifier: Modifier = Modifier
) {
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val timeSlots = listOf("09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00")
    
    val cellWidth = 100.dp 
    val cellHeight = 80.dp
    val timeColumnWidth = 60.dp
    val headerHeight = 50.dp

    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()
    
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    val headerColor = MaterialTheme.colorScheme.surface
    val contentColor = MaterialTheme.colorScheme.surface

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(contentColor)
    ) {
        // Sticky Header Row (Day Names)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerHeight)
                .background(headerColor)
        ) {
            // Corner spacer (Empty box above time column)
            Box(
                modifier = Modifier
                    .width(timeColumnWidth)
                    .fillMaxHeight()
                    .border(width = 0.5.dp, color = gridColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Time",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Scrollable Day Headers
            Row(
                modifier = Modifier
                    .weight(1f) // Takes remaining width and restricts scroll area
                    .horizontalScroll(horizontalScrollState)
            ) {
                days.forEach { day ->
                    Box(
                        modifier = Modifier
                            .width(cellWidth)
                            .fillMaxHeight()
                            .border(width = 0.5.dp, color = gridColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = gridColor)

        // Srcollable Body (Time + Grid)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // Fill remaining height of the screen
                .verticalScroll(verticalScrollState)
        ) {
            // Freeze-Left Time Column
            Column(
                modifier = Modifier
                    .width(timeColumnWidth)
                    .background(headerColor)
            ) {
                timeSlots.forEach { time ->
                    Box(
                        modifier = Modifier
                            .width(timeColumnWidth)
                            .height(cellHeight)
                            .border(width = 0.5.dp, color = gridColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = time,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Scrollable Content Grid
            Column(
                modifier = Modifier
                    .horizontalScroll(horizontalScrollState) // Syncs with header
            ) {
                timeSlots.forEach { time ->
                    Row {
                        days.forEachIndexed { dayIndex, _ ->
                            val dayOfWeek = dayIndex + 1 // 1=Mon, 2=Tue...
                            val classesForSlot = weeklySchedule[dayOfWeek]?.filter { schedule ->
                                formatTimeSlot(schedule.schedule.startTime) == time
                            } ?: emptyList()
                            
                            Box(
                                modifier = Modifier
                                    .width(cellWidth)
                                    .height(cellHeight)
                                    .border(width = 0.5.dp, color = gridColor),
                                contentAlignment = Alignment.Center
                            ) {
                                ClassCell(
                                    classItem = classesForSlot.firstOrNull(),
                                    onClick = { classItem -> onClassClick(classItem) },
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClassCell(
    classItem: TimeTableScheduleWithSubject?,
    onClick: (TimeTableScheduleWithSubject) -> Unit,
    modifier: Modifier = Modifier
) {
    if (classItem == null) return

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = generateSubjectColor(classItem.subject.subject).copy(alpha = 0.85f), // Increased opacity for better look
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(8.dp),
        onClick = { onClick(classItem) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = classItem.subject.subject,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            classItem.schedule.room?.let { room ->
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = room,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Light
                )
            }
        }
    }
}

private fun generateSubjectColor(subjectName: String): Color {
    val colors = listOf(
        Color(0xFF673AB7), // Deep Purple
        Color(0xFF009688), // Teal
        Color(0xFFFF5722), // Deep Orange
        Color(0xFF2196F3), // Blue
        Color(0xFF4CAF50), // Green
        Color(0xFFE91E63), // Pink
        Color(0xFFFF9800), // Orange
        Color(0xFF9C27B0), // Purple
        Color(0xFF00BCD4), // Cyan
        Color(0xFF795548), // Brown
        Color(0xFF3F51B5), // Indigo
        Color(0xFF607D8B)  // Blue Grey
    )
    return colors[kotlin.math.abs(subjectName.hashCode()) % colors.size]
}

private fun formatTimeSlot(minutesFromMidnight: Long): String {
    val hours = (minutesFromMidnight / 60).toInt()
    return String.format("%02d:00", hours)
}
