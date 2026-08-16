package `in`.jyotirmoy.attendx.timetable.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.jyotirmoy.attendx.timetable.data.model.TimeTableScheduleWithSubject
import kotlin.math.abs

// Material 3 Expressive pastel color generator (matches XCalendar)
fun getSubjectThemeColor(subjectName: String): Color {
    if (subjectName.isEmpty()) {
        return Color(0xFFE2E8F0)
    }
    val hash = subjectName.hashCode()
    val r = 180 + (abs(hash) % 75)
    val g = 180 + (abs(hash / 7) % 75)
    val b = 180 + (abs(hash / 13) % 75)
    return Color(r, g, b)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimetableClassCard(
    classItem: TimeTableScheduleWithSubject,
    onClick: (TimeTableScheduleWithSubject) -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: ((TimeTableScheduleWithSubject) -> Unit)? = null,
    isCurrentClass: Boolean = false,
    isOverlapping: Boolean = false
) {
    val hapticFeedback = LocalHapticFeedback.current
    val baseColor = getSubjectThemeColor(classItem.subject.subject)
    val cardShape = RoundedCornerShape(6.dp)

    // Animated breathing glow for currently ongoing class
    val pulseAlpha = remember { Animatable(0.4f) }
    LaunchedEffect(isCurrentClass) {
        if (isCurrentClass) {
            pulseAlpha.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        } else {
            pulseAlpha.snapTo(0.4f)
        }
    }

    Card(
        modifier = modifier
            .fillMaxSize()
            .clip(cardShape)
            .combinedClickable(
                onClick = { onClick(classItem) },
                onLongClick = onLongClick?.let { callback ->
                    {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        callback(classItem)
                    }
                }
            )
            .border(
                width = if (isCurrentClass) 1.5.dp else 0.8.dp,
                color = if (isCurrentClass) MaterialTheme.colorScheme.primary.copy(alpha = pulseAlpha.value)
                        else baseColor.copy(alpha = 0.8f),
                shape = cardShape
            ),
        shape = cardShape,
        colors = CardDefaults.cardColors(
            containerColor = baseColor.copy(alpha = if (isOverlapping) 0.75f else 0.92f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 5.dp, vertical = 3.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = classItem.subject.subject,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.5.sp,
                        lineHeight = 12.5.sp
                    ),
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                classItem.schedule.room?.let { room ->
                    if (room.isNotBlank()) {
                        Spacer(modifier = Modifier.height(1.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.LocationOn,
                                contentDescription = null,
                                tint = Color(0xFF475569),
                                modifier = Modifier.size(9.dp)
                            )
                            Spacer(modifier = Modifier.width(1.5.dp))
                            Text(
                                text = room,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF475569),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // 12-hour AM/PM time notation
            val startTimeStr = formatMinutesToTime(classItem.schedule.startTime)
            val endTimeStr = formatMinutesToTime(classItem.schedule.endTime)
            Text(
                text = "$startTimeStr - $endTimeStr",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                color = Color(0xFF64748B),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

fun formatMinutesToTime(totalMinutes: Long): String {
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    val isPm = hours >= 12
    val displayHour = when {
        hours == 0L -> 12L
        hours > 12L -> hours - 12L
        else -> hours
    }
    return String.format("%d:%02d %s", displayHour, minutes, if (isPm) "pm" else "am")
}
