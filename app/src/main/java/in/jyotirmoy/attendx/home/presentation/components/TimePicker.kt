@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package `in`.jyotirmoy.attendx.home.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.TimePickerDialogDefaults
import androidx.compose.material3.TimePickerDisplayMode
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import `in`.jyotirmoy.attendx.core.common.LocalWeakHaptic
import `in`.jyotirmoy.attendx.core.utils.TimeUtils
import android.text.format.DateFormat

/**
 * Read-only field that opens the Material 3 time picker.
 *
 * The field is deliberately not a disabled `OutlinedTextField`: a disabled field
 * is announced as unavailable by TalkBack even though tapping it is the only way
 * to use this control. It stays enabled and read-only, with the tap handled by a
 * transparent overlay so the whole field — not just the trailing icon — is the
 * target.
 */
@Composable
fun TimePickerField(
    label: String,
    time: String,
    onTimeChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    var showDialog by remember { mutableStateOf(false) }
    val displayTime = TimeUtils.format24To12Hour(time)

    Box(modifier = modifier) {
        OutlinedTextField(
            value = displayTime,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            isError = isError,
            label = {
                // The label sits in the notch of the outline and cannot wrap, so
                // it is the first thing to clip once the user scales text up.
                Text(text = label, maxLines = 1, overflow = TextOverflow.Ellipsis)
            },
            trailingIcon = {
                Icon(Icons.Rounded.Schedule, contentDescription = null)
            },
            modifier = Modifier.fillMaxWidth()
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(
                    role = Role.Button,
                    onClickLabel = "Change $label"
                ) { showDialog = true }
        )
    }

    if (showDialog) {
        AttendXTimePickerDialog(
            title = label,
            initialTime = time,
            onDismiss = { showDialog = false },
            onTimeSelected = {
                onTimeChange(it)
                showDialog = false
            }
        )
    }
}

/**
 * Material 3 time picker dialog with a dial/keyboard toggle.
 *
 * Starts in keyboard-entry mode whenever the dial cannot fit — landscape, small
 * screens, or a large display size — because the dial has a fixed minimum height
 * and would otherwise be cut off. The toggle stays available either way, so the
 * choice is a starting point rather than a restriction.
 */
@Composable
fun AttendXTimePickerDialog(
    title: String,
    initialTime: String,
    onDismiss: () -> Unit,
    onTimeSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val weakHaptic = LocalWeakHaptic.current

    val state = rememberTimePickerState(
        initialHour = TimeUtils.getHour(initialTime),
        initialMinute = TimeUtils.getMinute(initialTime),
        is24Hour = DateFormat.is24HourFormat(context)
    )

    val dialFits = configuration.screenHeightDp.dp >= MIN_HEIGHT_FOR_DIAL
    var displayMode by remember {
        mutableStateOf(
            if (dialFits) TimePickerDisplayMode.Picker else TimePickerDisplayMode.Input
        )
    }

    TimePickerDialog(
        onDismissRequest = onDismiss,
        title = {
            // "Start time" / "End time" beats the default "Select time" when two
            // of these dialogs are reachable from the same form.
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        },
        modeToggleButton = {
            TimePickerDialogDefaults.DisplayModeToggle(
                displayMode = displayMode,
                onDisplayModeChange = {
                    weakHaptic()
                    displayMode = if (displayMode == TimePickerDisplayMode.Picker) {
                        TimePickerDisplayMode.Input
                    } else {
                        TimePickerDisplayMode.Picker
                    }
                }
            )
        },
        confirmButton = {
            TextButton(onClick = {
                weakHaptic()
                onTimeSelected(TimeUtils.formatTime(state.hour, state.minute))
            }) {
                Text("OK", maxLines = 1)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", maxLines = 1)
            }
        }
    ) {
        if (displayMode == TimePickerDisplayMode.Input) {
            TimeInput(state = state)
        } else {
            TimePicker(state = state)
        }
    }
}

/**
 * Below this the clock dial plus the dialog's title and buttons stop fitting, so
 * the dialog opens in keyboard-entry mode instead. Matches the height Material
 * uses to switch its own picker to a horizontal layout.
 */
private val MIN_HEIGHT_FOR_DIAL = 400.dp
