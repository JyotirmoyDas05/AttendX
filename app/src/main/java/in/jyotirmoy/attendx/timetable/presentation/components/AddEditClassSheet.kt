package `in`.jyotirmoy.attendx.timetable.presentation.components

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.jyotirmoy.attendx.core.data.model.SubjectEntity
import `in`.jyotirmoy.attendx.timetable.data.model.TimeTableScheduleEntity
import `in`.jyotirmoy.attendx.timetable.data.model.TimeTableScheduleWithSubject
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditClassSheet(
    subjects: List<SubjectEntity>,
    initialSchedule: TimeTableScheduleWithSubject?,
    initialDay: Int = 1,
    onDismiss: () -> Unit,
    onSave: (TimeTableScheduleEntity) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isEditing = initialSchedule != null

    // form states
    var selectedSubjectId by remember { mutableIntStateOf(initialSchedule?.schedule?.subjectId ?: (subjects.firstOrNull()?.id ?: 0)) }
    var selectedDay by remember { mutableIntStateOf(initialSchedule?.schedule?.dayOfWeek ?: initialDay) }
    var startTime24 by remember { mutableStateOf(initialSchedule?.let { formatTimeInput(it.schedule.startTime) } ?: "09:00") }
    var endTime24 by remember { mutableStateOf(initialSchedule?.let { formatTimeInput(it.schedule.endTime) } ?: "10:00") }
    var room by remember { mutableStateOf(initialSchedule?.schedule?.room ?: "") }
    var professor by remember { mutableStateOf(initialSchedule?.schedule?.professor ?: "") }
    var classType by remember { mutableStateOf(initialSchedule?.schedule?.classType ?: "Lecture") }
    var weekPattern by remember { mutableStateOf(initialSchedule?.schedule?.weekPattern ?: "all") }
    var notes by remember { mutableStateOf(initialSchedule?.schedule?.notes ?: "") }

    var subjectDropdownExpanded by remember { mutableStateOf(false) }

    // M3 TimePicker dialog visibility
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    // M3 TimePickerState
    val startParts = startTime24.split(":")
    val startTimePickerState = rememberTimePickerState(
        initialHour = startParts.getOrElse(0) { "9" }.toIntOrNull() ?: 9,
        initialMinute = startParts.getOrElse(1) { "0" }.toIntOrNull() ?: 0,
        is24Hour = false
    )

    val endParts = endTime24.split(":")
    val endTimePickerState = rememberTimePickerState(
        initialHour = endParts.getOrElse(0) { "10" }.toIntOrNull() ?: 10,
        initialMinute = endParts.getOrElse(1) { "0" }.toIntOrNull() ?: 0,
        is24Hour = false
    )

    // Start Time Picker Dialog
    if (showStartTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showStartTimePicker = false },
            title = { Text("Select Start Time") },
            confirmButton = {
                TextButton(onClick = {
                    startTime24 = String.format("%02d:%02d", startTimePickerState.hour, startTimePickerState.minute)
                    showStartTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showStartTimePicker = false }) { Text("Cancel") }
            }
        ) {
            TimePicker(state = startTimePickerState)
        }
    }

    // End Time Picker Dialog
    if (showEndTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showEndTimePicker = false },
            title = { Text("Select End Time") },
            confirmButton = {
                TextButton(onClick = {
                    endTime24 = String.format("%02d:%02d", endTimePickerState.hour, endTimePickerState.minute)
                    showEndTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEndTimePicker = false }) { Text("Cancel") }
            }
        ) {
            TimePicker(state = endTimePickerState)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = if (isEditing) "Edit Class" else "Add Class",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Subject Dropdown
            ExposedDropdownMenuBox(
                expanded = subjectDropdownExpanded,
                onExpandedChange = { subjectDropdownExpanded = !subjectDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = subjects.find { it.id == selectedSubjectId }?.subject ?: "Select Subject",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Subject") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectDropdownExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = subjectDropdownExpanded,
                    onDismissRequest = { subjectDropdownExpanded = false }
                ) {
                    subjects.forEach { subject ->
                        DropdownMenuItem(
                            text = { Text(subject.subject) },
                            onClick = {
                                selectedSubjectId = subject.id
                                subjectDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Day Selector Chips
            Text("Day", style = MaterialTheme.typography.labelMedium)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (day in 1..7) {
                    FilterChip(
                        selected = selectedDay == day,
                        onClick = { selectedDay = day },
                        label = { Text(DayOfWeek.of(day).getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(2)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Time Inputs - click to open M3 TimePicker
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Start Time
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = format24To12Display(startTime24),
                        onValueChange = {},
                        label = { Text("Start") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) }
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showStartTimePicker = true }
                    )
                }

                // End Time
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = format24To12Display(endTime24),
                        onValueChange = {},
                        label = { Text("End") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) }
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showEndTimePicker = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Room & Professor
            OutlinedTextField(
                value = room,
                onValueChange = { room = it },
                label = { Text("Room (optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = professor,
                onValueChange = { professor = it },
                label = { Text("Professor (optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Class Type Chips
            Text("Class Type", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Lecture", "Lab", "Tutorial").forEach { type ->
                    FilterChip(
                        selected = classType == type,
                        onClick = { classType = type },
                        label = { Text(type) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Week Pattern Chips
            Text("Week Pattern", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("all" to "Every Week", "odd" to "Odd Weeks", "even" to "Even Weeks").forEach { (pattern, label) ->
                    FilterChip(
                        selected = weekPattern == pattern,
                        onClick = { weekPattern = pattern },
                        label = { Text(label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        val startMinutes = parseTimeToMinutes(startTime24)
                        val endMinutes = parseTimeToMinutes(endTime24)
                        if (startMinutes != null && endMinutes != null && startMinutes < endMinutes) {
                            onSave(
                                TimeTableScheduleEntity(
                                    id = initialSchedule?.schedule?.id ?: 0,
                                    subjectId = selectedSubjectId,
                                    dayOfWeek = selectedDay,
                                    startTime = startMinutes,
                                    endTime = endMinutes,
                                    room = room.ifBlank { null },
                                    professor = professor.ifBlank { null },
                                    classType = classType,
                                    isActive = true,
                                    weekPattern = weekPattern,
                                    notes = notes.ifBlank { null }
                                )
                            )
                        }
                    }
                ) {
                    Text(if (isEditing) "Update" else "Add")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// minutes → "HH:mm"
private fun formatTimeInput(minutes: Long): String {
    val h = minutes / 60
    val m = minutes % 60
    return String.format("%02d:%02d", h, m)
}

// "HH:mm" → total minutes
private fun parseTimeToMinutes(input: String): Long? {
    return try {
        val parts = input.split(":")
        val h = parts[0].toLong()
        val m = parts.getOrElse(1) { "0" }.toLong()
        h * 60 + m
    } catch (e: Exception) {
        null
    }
}

// "14:30" → "2:30 PM"
private fun format24To12Display(time24: String): String {
    return try {
        val parts = time24.split(":")
        val h = parts[0].toInt()
        val m = parts.getOrElse(1) { "0" }.toInt()
        val amPm = if (h < 12) "AM" else "PM"
        val h12 = when {
            h == 0 -> 12
            h > 12 -> h - 12
            else -> h
        }
        String.format("%d:%02d %s", h12, m, amPm)
    } catch (e: Exception) {
        time24
    }
}
