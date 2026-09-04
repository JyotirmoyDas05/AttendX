@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package `in`.jyotirmoy.attendx.timetable.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.jyotirmoy.attendx.calender.presentation.screens.calculateAttendanceInsight
import `in`.jyotirmoy.attendx.core.common.LocalWeakHaptic
import `in`.jyotirmoy.attendx.core.data.model.AttendanceEntity
import `in`.jyotirmoy.attendx.core.domain.model.AttendanceStatus
import `in`.jyotirmoy.attendx.core.domain.model.SubjectAttendance
import `in`.jyotirmoy.attendx.timetable.data.model.TimeTableScheduleWithSubject
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun ViewClassSheet(
    scheduleWithSubject: TimeTableScheduleWithSubject,
    classDate: LocalDate,
    attendanceListFlow: Flow<List<AttendanceEntity>>,
    attendanceCountsFlow: Flow<SubjectAttendance>,
    onDismiss: () -> Unit,
    onEdit: (TimeTableScheduleWithSubject) -> Unit,
    onDelete: (scheduleId: Int) -> Unit = {},
    onMarkAttendance: (subjectId: Int, date: String, status: AttendanceStatus) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val weakHaptic = LocalWeakHaptic.current
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val schedule = scheduleWithSubject.schedule
    val subject = scheduleWithSubject.subject
    val subjectThemeColor = getSubjectThemeColor(subject.subject)

    // Supplied by the caller rather than derived from today, so marking a class
    // while looking at another week records it against the day on screen.
    val classDateString = remember(classDate) { classDate.toString() }
    val today = remember { LocalDate.now() }

    val attendances by attendanceListFlow.collectAsState(initial = emptyList())
    val attendanceCounts by attendanceCountsFlow.collectAsState(initial = SubjectAttendance())

    // Current attendance status for this date
    val currentEntry = attendances.find { it.date == classDateString }
    val currentStatus = currentEntry?.status ?: AttendanceStatus.UNMARKED

    val insight = calculateAttendanceInsight(
        presentCount = attendanceCounts.presentCount,
        totalCount = attendanceCounts.totalCount,
        targetPercentage = subject.targetPercentage
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Close and Edit buttons (XCalendar style)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        weakHaptic()
                        onDismiss()
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close",
                        modifier = Modifier.size(20.dp)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            weakHaptic()
                            showDeleteConfirm = true
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = "Delete Class Slot",
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    FilledTonalButton(
                        onClick = {
                            weakHaptic()
                            onDismiss()
                            onEdit(scheduleWithSubject)
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Edit",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Edit Class",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Subject Title Block
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp, 36.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(subjectThemeColor)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = subject.subject,
                        style = MaterialTheme.typography.headlineSmall.copy(fontSize = 22.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (!subject.subjectCode.isNullOrBlank()) {
                        Text(
                            text = subject.subjectCode ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Schedule Details Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val dayName = classDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
                    val monthName = classDate.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                    val dayFormatted = "$dayName, $monthName ${classDate.dayOfMonth}"
                    val timeFormatted = "${formatTime(schedule.startTime)} – ${formatTime(schedule.endTime)}"

                    DetailInfoRow(
                        icon = Icons.Rounded.CalendarToday,
                        label = "Day & Date",
                        value = if (classDate == today) "$dayFormatted (Today)" else dayFormatted
                    )

                    DetailInfoRow(
                        icon = Icons.Rounded.AccessTime,
                        label = "Time Slot",
                        value = timeFormatted
                    )

                    if (!schedule.room.isNullOrBlank()) {
                        DetailInfoRow(
                            icon = Icons.Rounded.LocationOn,
                            label = "Room / Venue",
                            value = schedule.room
                        )
                    }

                    if (!schedule.classType.isNullOrBlank()) {
                        DetailInfoRow(
                            icon = Icons.Rounded.Category,
                            label = "Class Type",
                            value = schedule.classType
                        )
                    }
                }
            }

            // Quick Attendance Marking Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Attendance for this Session",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        val total = attendanceCounts.totalCount
                        val present = attendanceCounts.presentCount
                        val pct = if (total > 0) (present.toFloat() / total.toFloat() * 100).toInt() else 0

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (pct >= subject.targetPercentage.toInt()) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.errorContainer
                            }
                        ) {
                            Text(
                                text = "$pct%",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (pct >= subject.targetPercentage.toInt()) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onErrorContainer
                                }
                            )
                        }
                    }

                    // Attendance Insight snippet
                    Text(
                        text = insight.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // 3 Quick Action Buttons: Present | Absent | Ignore
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Present Button
                        AttendanceActionButton(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Rounded.Check,
                            label = "Present",
                            isSelected = currentStatus == AttendanceStatus.PRESENT,
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedContentColor = MaterialTheme.colorScheme.onPrimary,
                            onClick = {
                                weakHaptic()
                                val nextStatus = if (currentStatus == AttendanceStatus.PRESENT) {
                                    AttendanceStatus.UNMARKED
                                } else {
                                    AttendanceStatus.PRESENT
                                }
                                onMarkAttendance(subject.id, classDateString, nextStatus)
                            }
                        )

                        // Absent Button
                        AttendanceActionButton(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Rounded.Close,
                            label = "Absent",
                            isSelected = currentStatus == AttendanceStatus.ABSENT,
                            selectedContainerColor = MaterialTheme.colorScheme.error,
                            selectedContentColor = MaterialTheme.colorScheme.onError,
                            onClick = {
                                weakHaptic()
                                val nextStatus = if (currentStatus == AttendanceStatus.ABSENT) {
                                    AttendanceStatus.UNMARKED
                                } else {
                                    AttendanceStatus.ABSENT
                                }
                                onMarkAttendance(subject.id, classDateString, nextStatus)
                            }
                        )

                        // Ignore / Clear Button
                        AttendanceActionButton(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Rounded.Remove,
                            label = "Ignore",
                            isSelected = currentStatus == AttendanceStatus.UNMARKED,
                            selectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            selectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            onClick = {
                                weakHaptic()
                                onMarkAttendance(subject.id, classDateString, AttendanceStatus.UNMARKED)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        val startTimeStr = formatTime(schedule.startTime)
        val endTimeStr = formatTime(schedule.endTime)
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = {
                Text(
                    text = "Delete Class Slot",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to remove this ${subject.subject} class from the timetable ($startTimeStr – $endTimeStr)?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        weakHaptic()
                        showDeleteConfirm = false
                        onDismiss()
                        onDelete(schedule.id)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun DetailInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun AttendanceActionButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    selectedContainerColor: Color,
    selectedContentColor: Color,
    onClick: () -> Unit
) {
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) selectedContainerColor else MaterialTheme.colorScheme.surfaceContainer,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "btnContainer"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) selectedContentColor else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "btnContent"
    )

    Surface(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = containerColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor
            )
        }
    }
}
