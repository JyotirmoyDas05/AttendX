package `in`.jyotirmoy.attendx.timetable.presentation.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.jyotirmoy.attendx.core.presentation.util.bounceClick
import `in`.jyotirmoy.attendx.timetable.data.model.TimeTableScheduleWithSubject

@Composable
fun ClassCard(
    scheduleWithSubject: TimeTableScheduleWithSubject,
    onClick: () -> Unit
) {
    val schedule = scheduleWithSubject.schedule
    val subject = scheduleWithSubject.subject
    val subjectColor = getSubjectThemeColor(subject.subject)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .border(
                width = 0.8.dp,
                color = subjectColor.copy(alpha = 0.45f),
                shape = RoundedCornerShape(12.dp)
            )
            .bounceClick(onClick = onClick)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = subjectColor.copy(alpha = 0.20f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = subject.subject,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )

                Spacer(modifier = Modifier.height(3.dp))

                val startTimeStr = formatTime(schedule.startTime)
                val endTimeStr = formatTime(schedule.endTime)
                Text(
                    text = "$startTimeStr – $endTimeStr",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                    color = Color(0xFF475569)
                )

                val details = buildList {
                    if (!schedule.room.isNullOrBlank()) add("📍 ${schedule.room}")
                    if (schedule.classType != "Lecture") add(schedule.classType)
                }

                if (details.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = details.joinToString(" • "),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                        color = Color(0xFF64748B)
                    )
                }
            }
        }
    }
}
