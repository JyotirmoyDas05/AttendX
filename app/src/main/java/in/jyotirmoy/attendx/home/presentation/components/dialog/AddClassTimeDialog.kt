@file:OptIn(ExperimentalLayoutApi::class)

package `in`.jyotirmoy.attendx.home.presentation.components.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import `in`.jyotirmoy.attendx.core.domain.model.ClassSchedule
import `in`.jyotirmoy.attendx.core.presentation.components.text.AutoResizeableText
import `in`.jyotirmoy.attendx.core.presentation.theme.Shape
import `in`.jyotirmoy.attendx.core.utils.TimeUtils
import `in`.jyotirmoy.attendx.home.presentation.components.TimePickerField

/**
 * @param existingSchedules other slots on this subject's timetable, so an
 *   overlap is caught here rather than silently saved. [existingSchedule] is
 *   excluded from the comparison so editing a slot cannot clash with itself.
 */
@Composable
fun AddClassTimeDialog(
    dayOfWeek: Int,
    dayName: String,
    subjectId: Int,
    existingSchedule: ClassSchedule? = null,
    existingSchedules: List<ClassSchedule> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (ClassSchedule) -> Unit
) {
    var startTime by remember { mutableStateOf(existingSchedule?.startTime ?: "09:00") }
    var endTime by remember { mutableStateOf(existingSchedule?.endTime ?: "10:00") }
    var location by remember { mutableStateOf(existingSchedule?.location ?: "") }

    val rangeError = TimeUtils.validateRange(startTime, endTime)
    val clash = remember(startTime, endTime, dayOfWeek, existingSchedules, existingSchedule) {
        if (rangeError != null) {
            null
        } else {
            existingSchedules.firstOrNull { other ->
                other.id != existingSchedule?.id &&
                    other.dayOfWeek == dayOfWeek &&
                    TimeUtils.overlaps(startTime, endTime, other.startTime, other.endTime)
            }
        }
    }
    val error = rangeError ?: clash?.let {
        "Overlaps a class at ${TimeUtils.format24To12Hour(it.startTime)} – " +
            TimeUtils.format24To12Hour(it.endTime)
    }
    val isValid = error == null

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(Shape.cardCornerLarge)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (existingSchedule != null) "Edit Class — $dayName" else "Add Class — $dayName",
                style = MaterialTheme.typography.titleLarge
            )

            TimePickerField(
                label = "Start Time",
                time = startTime,
                onTimeChange = { startTime = it },
                isError = !isValid,
                modifier = Modifier.fillMaxWidth()
            )

            TimePickerField(
                label = "End Time",
                time = endTime,
                onTimeChange = { endTime = it },
                isError = !isValid,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = error ?: "Duration: ${
                    TimeUtils.formatDuration(TimeUtils.calculateDuration(startTime, endTime))
                }",
                style = MaterialTheme.typography.bodySmall,
                color = if (isValid) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.error
                }
            )

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = {
                    Text("Location (Optional)", maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                placeholder = { Text("e.g., Lab 301, Room 205", maxLines = 1) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Buttons wrap to their own lines rather than squeezing when the
            // labels grow — a 50/50 split truncates "Update" long before it has
            // to at large font scales.
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 2
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    AutoResizeableText(
                        text = "Cancel",
                        style = MaterialTheme.typography.labelLarge,
                        textAlign = TextAlign.Center
                    )
                }

                Button(
                    onClick = {
                        onSave(
                            ClassSchedule(
                                id = existingSchedule?.id ?: 0,
                                subjectId = subjectId,
                                dayOfWeek = dayOfWeek,
                                startTime = startTime,
                                endTime = endTime,
                                location = location.trim().takeIf { it.isNotBlank() }
                            )
                        )
                    },
                    enabled = isValid,
                    modifier = Modifier.weight(1f)
                ) {
                    AutoResizeableText(
                        text = if (existingSchedule != null) "Update" else "Add",
                        style = MaterialTheme.typography.labelLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
