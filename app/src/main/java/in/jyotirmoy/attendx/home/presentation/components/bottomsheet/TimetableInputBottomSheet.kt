@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class)

package `in`.jyotirmoy.attendx.home.presentation.components.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.intPreferencesKey
import `in`.jyotirmoy.attendx.core.common.LocalWeakHaptic
import `in`.jyotirmoy.attendx.core.domain.model.ClassSchedule
import `in`.jyotirmoy.attendx.core.presentation.components.layout.AdaptiveTwoColumn
import `in`.jyotirmoy.attendx.core.presentation.components.text.AutoResizeableText
import `in`.jyotirmoy.attendx.core.utils.TimeUtils
import `in`.jyotirmoy.attendx.home.presentation.components.TimePickerField
import `in`.jyotirmoy.attendx.settings.data.local.SettingsKeys
import `in`.jyotirmoy.attendx.settings.data.local.provider.settingsDataStore
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

/**
 * Adds or edits a single weekly class slot.
 *
 * @param existingSchedules the slots already on this subject's timetable. Used
 *   to reject an overlap before it is saved; [initialSchedule] is excluded from
 *   the comparison so editing a slot does not clash with itself.
 */
@Composable
fun TimetableInputBottomSheet(
    initialSchedule: ClassSchedule? = null,
    existingSchedules: List<ClassSchedule> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (dayOfWeek: Int, startTime: String, endTime: String, location: String?) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val weakHaptic = LocalWeakHaptic.current
    val isEditing = initialSchedule != null

    val context = LocalContext.current
    val settings by context.settingsDataStore.data.collectAsState(initial = null)
    val defaultDuration =
        settings?.get(intPreferencesKey(SettingsKeys.DEFAULT_CLASS_DURATION.name)) ?: 60
    val defaultStartMinute =
        settings?.get(intPreferencesKey(SettingsKeys.DEFAULT_START_MINUTE.name)) ?: 45

    var selectedDay by remember {
        mutableIntStateOf(initialSchedule?.dayOfWeek ?: DayOfWeek.MONDAY.value)
    }
    var startTime by remember { mutableStateOf(initialSchedule?.startTime ?: "09:00") }
    var endTime by remember { mutableStateOf(initialSchedule?.endTime ?: "10:00") }
    var location by remember { mutableStateOf(initialSchedule?.location.orEmpty()) }

    // Only a confirmed pick in the end-time dialog counts as manual. The previous
    // wheel picker reported a selection during its own first layout, which set
    // this flag before the user had touched anything and silently killed the
    // auto-fill below.
    var isEndTimeManuallySet by remember { mutableStateOf(isEditing) }

    // Apply the preferred start minute once, and only for a new slot.
    LaunchedEffect(defaultStartMinute) {
        if (!isEditing && startTime == "09:00" && defaultStartMinute in 0..59) {
            startTime = TimeUtils.formatTime(9, defaultStartMinute)
        }
    }

    // Keep the end time a class-length after the start until the user overrides
    // it. Clamped to 23:59 so a late start cannot roll past midnight and leave
    // the form permanently invalid.
    LaunchedEffect(startTime, defaultDuration, isEndTimeManuallySet) {
        if (!isEndTimeManuallySet) {
            val startMinutes = TimeUtils.getHour(startTime) * 60 + TimeUtils.getMinute(startTime)
            val endMinutes = (startMinutes + defaultDuration).coerceAtMost(23 * 60 + 59)
            endTime = TimeUtils.formatTime(endMinutes / 60, endMinutes % 60)
        }
    }

    val rangeError = TimeUtils.validateRange(startTime, endTime)
    val clash = remember(selectedDay, startTime, endTime, existingSchedules, initialSchedule) {
        if (rangeError != null) {
            null
        } else {
            existingSchedules.firstOrNull { other ->
                other !== initialSchedule &&
                    other.dayOfWeek == selectedDay &&
                    TimeUtils.overlaps(startTime, endTime, other.startTime, other.endTime)
            }
        }
    }
    val clashError = clash?.let {
        "Overlaps an existing class at " +
            "${TimeUtils.format24To12Hour(it.startTime)} – ${TimeUtils.format24To12Hour(it.endTime)}"
    }
    val error = rangeError ?: clashError
    val isValid = error == null

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = if (isEditing) "Edit Class" else "Add Class",
                style = MaterialTheme.typography.headlineMediumEmphasized,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Wraps to as many rows as the width and text size need, so the last
            // day is never pushed off-screen the way a single scrolling row did.
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DayOfWeek.entries.forEach { day ->
                    FilterChip(
                        selected = selectedDay == day.value,
                        onClick = {
                            weakHaptic()
                            selectedDay = day.value
                        },
                        label = {
                            Text(
                                text = day.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                }
            }

            TimeRangeFields(
                startTime = startTime,
                endTime = endTime,
                isError = !isValid,
                onStartChange = { startTime = it },
                onEndChange = {
                    endTime = it
                    isEndTimeManuallySet = true
                }
            )

            if (error != null) {
                SheetMessage(
                    icon = Icons.Rounded.ErrorOutline,
                    text = error,
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                SheetMessage(
                    icon = Icons.Rounded.Schedule,
                    text = "Duration: " + TimeUtils.formatDuration(
                        TimeUtils.calculateDuration(startTime, endTime)
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Room (optional)", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    weakHaptic()
                    scope.launch {
                        onSave(
                            selectedDay,
                            startTime,
                            endTime,
                            location.trim().takeIf { it.isNotBlank() }
                        )
                        sheetState.hide()
                        onDismiss()
                    }
                },
                enabled = isValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Height comes from the label, not a fixed dp, so a scaled-up or
                // wider translated label grows the button instead of being cut.
                AutoResizeableText(
                    text = if (isEditing) "Save Changes" else "Add to Timetable",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
            }
        }
    }
}

/**
 * Start and end side by side, stacking when they would be too narrow to read.
 */
@Composable
private fun TimeRangeFields(
    startTime: String,
    endTime: String,
    isError: Boolean,
    onStartChange: (String) -> Unit,
    onEndChange: (String) -> Unit
) {
    AdaptiveTwoColumn(
        first = {
            TimePickerField(
                label = "Starts at",
                time = startTime,
                onTimeChange = onStartChange,
                isError = isError,
                modifier = Modifier.fillMaxWidth()
            )
        },
        second = {
            TimePickerField(
                label = "Ends at",
                time = endTime,
                onTimeChange = onEndChange,
                isError = isError,
                modifier = Modifier.fillMaxWidth()
            )
        }
    )
}

@Composable
private fun SheetMessage(
    icon: ImageVector,
    text: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(16.dp)
        )
        // Wraps freely: a validation message that is clipped is worse than one
        // that costs an extra line.
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            modifier = Modifier.weight(1f)
        )
    }
}
