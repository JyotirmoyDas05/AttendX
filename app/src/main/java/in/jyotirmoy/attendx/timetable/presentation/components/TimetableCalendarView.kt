package `in`.jyotirmoy.attendx.timetable.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
 * Indian college timetable grid view
 * Shows classes in a traditional week grid layout
 */
@Composable
fun TimetableCalendarView(
    weeklySchedule: Map<Int, List<TimeTableScheduleWithSubject>>,
    onClassClick: (TimeTableScheduleWithSubject) -> Unit,
    modifier: Modifier = Modifier
) {
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val timeSlots = listOf("09:00", "10:00", "11:00", "12:00", "14:00", "15:00", "16:00", "17:00")
    val cellWidth = 80.dp
    val cellHeight = 72.dp
    val timeColumnWidth = 48.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        // Header row with days
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            // Empty corner
            Spacer(modifier = Modifier.width(timeColumnWidth))
            
            days.forEachIndexed { _, day ->
                Box(
                    modifier = Modifier
                        .width(cellWidth)
                        .height(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Time slots with class cells
        timeSlots.forEach { time ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                // Time column
                Box(
                    modifier = Modifier
                        .width(timeColumnWidth)
                        .height(cellHeight),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = time,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Day cells
                days.forEachIndexed { dayIndex, _ ->
                    val dayOfWeek = dayIndex + 1 // 1=Mon, 2=Tue...
                    val classesForSlot = weeklySchedule[dayOfWeek]?.filter { schedule ->
                        formatTimeSlot(schedule.schedule.startTime) == time
                    } ?: emptyList()
                    
                    ClassCell(
                        classItem = classesForSlot.firstOrNull(),
                        onClick = { classItem -> onClassClick(classItem) },
                        modifier = Modifier
                            .width(cellWidth)
                            .height(cellHeight)
                    )
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
    Card(
        modifier = modifier
            .padding(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = classItem?.let {
                generateSubjectColor(it.subject.subject).copy(alpha = 0.3f)
            } ?: MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = RoundedCornerShape(8.dp),
        onClick = { classItem?.let { onClick(it) } },
        enabled = classItem != null
    ) {
        if (classItem != null) {
            Column(
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = classItem.subject.subject,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                classItem.schedule.room?.let { room ->
                    Text(
                        text = room,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun generateSubjectColor(subjectName: String): Color {
    val colors = listOf(
        Color(0xFF6200EE), Color(0xFF03DAC5), Color(0xFFFF5722),
        Color(0xFF2196F3), Color(0xFF4CAF50), Color(0xFFE91E63),
        Color(0xFFFF9800), Color(0xFF9C27B0), Color(0xFF00BCD4),
        Color(0xFF795548)
    )
    return colors[kotlin.math.abs(subjectName.hashCode()) % colors.size]
}

private fun formatTimeSlot(minutesFromMidnight: Long): String {
    val hours = (minutesFromMidnight / 60).toInt()
    return String.format("%02d:00", hours)
}
