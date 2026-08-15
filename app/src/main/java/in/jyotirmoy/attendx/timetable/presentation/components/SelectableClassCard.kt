package `in`.jyotirmoy.attendx.timetable.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.jyotirmoy.attendx.core.presentation.util.bounceClick
import `in`.jyotirmoy.attendx.timetable.data.model.TimeTableScheduleWithSubject

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SelectableClassCard(
    scheduleWithSubject: TimeTableScheduleWithSubject,
    isSelected: Boolean,
    onLongPress: () -> Unit,
    onClick: () -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current
    val schedule = scheduleWithSubject.schedule
    val subject = scheduleWithSubject.subject

    val subjectColor = getSubjectThemeColor(subject.subject)
    val cardBackground = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        subjectColor.copy(alpha = 0.20f)
    }

    val cardBorder = if (isSelected) {
        Modifier.border(
            width = 2.dp,
            color = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(12.dp)
        )
    } else {
        Modifier.border(
            width = 0.8.dp,
            color = subjectColor.copy(alpha = 0.45f),
            shape = RoundedCornerShape(12.dp)
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .then(cardBorder)
            .bounceClick(onClick = onClick)
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongPress()
                }
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
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
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else Color(0xFF1E293B)
                )

                Spacer(modifier = Modifier.height(3.dp))

                val startTimeStr = formatTime(schedule.startTime)
                val endTimeStr = formatTime(schedule.endTime)
                Text(
                    text = "$startTimeStr – $endTimeStr",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f) else Color(0xFF475569)
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
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f) else Color(0xFF64748B)
                    )
                }
            }

            // CheckCircle overlay when selected
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
