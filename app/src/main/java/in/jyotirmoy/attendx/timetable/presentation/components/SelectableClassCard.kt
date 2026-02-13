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
import `in`.jyotirmoy.attendx.timetable.data.model.TimeTableScheduleWithSubject
import `in`.jyotirmoy.attendx.core.presentation.util.bounceClick

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

    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow
    }

    val borderModifier = if (isSelected) {
        Modifier.border(
            width = 3.dp,
            color = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(16.dp)
        )
    } else {
        Modifier
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .then(borderModifier)
            .bounceClick(onClick = onClick)
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongPress()
                }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Time column
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formatTime(schedule.startTime),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formatTime(schedule.endTime),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Color indicator
                val subjectColor = generateSubjectColor(subject.subject)
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(40.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(subjectColor)
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Details
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = subject.subject,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (schedule.room != null) {
                        Text(
                            text = schedule.room,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (schedule.classType != "Lecture") {
                        Text(
                            text = schedule.classType,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }

            // CheckCircle overlay
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
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
