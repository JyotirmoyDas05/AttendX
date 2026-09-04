@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.jyotirmoy.attendx.timetable.presentation.screens.template.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import `in`.jyotirmoy.attendx.core.common.LocalWeakHaptic
import `in`.jyotirmoy.attendx.core.presentation.components.text.AutoResizeableText

/** House dialog shell: Surface + extraLarge shape + ButtonGroup actions. */
@Composable
private fun TemplateDialog(
    title: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmEnabled: Boolean = true,
    destructive: Boolean = false,
    body: @Composable () -> Unit
) {
    val weakHaptic = LocalWeakHaptic.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = true)
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 8.dp,
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .widthIn(min = 280.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                AutoResizeableText(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                body()

                Spacer(modifier = Modifier.height(24.dp))

                @Suppress("DEPRECATION")
                ButtonGroup(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { onDismiss(); weakHaptic() },
                        modifier = Modifier.weight(1f)
                    ) {
                        AutoResizeableText(
                            text = "Cancel",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    Button(
                        onClick = { onConfirm(); weakHaptic() },
                        enabled = confirmEnabled,
                        colors = if (destructive) {
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        } else {
                            ButtonDefaults.buttonColors()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        AutoResizeableText(text = confirmLabel)
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogBody(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
fun LinkAccountDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    TemplateDialog(
        title = "Link a Google account?",
        confirmLabel = "Continue",
        onConfirm = onConfirm,
        onDismiss = onDismiss
    ) {
        DialogBody(
            "Right now your shared timetables are tied to this app install. If you " +
                "reinstall AttendX or switch phones, you lose the ability to update them.\n\n" +
                "Linking attaches a Google account to your existing anonymous ID so you " +
                "can keep editing from anywhere.\n\n" +
                "Your email is stored by Firebase Authentication only. It is never added " +
                "to a shared timetable, and other students still see you as " +
                "\"Anonymous Student\" unless you pick a display name.\n\n" +
                "Nothing else in AttendX asks you to sign in."
        )
    }
}

@Composable
fun LinkCollisionDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    TemplateDialog(
        title = "That account is already in use",
        confirmLabel = "Sign in anyway",
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        destructive = true
    ) {
        DialogBody(
            "This Google account is already linked to another AttendX identity — " +
                "usually because you linked it on a different device.\n\n" +
                "You can sign in to that identity and pick up the timetables it owns. " +
                "But the anonymous ID on this phone will be given up, so anything you " +
                "shared from here and never linked can no longer be edited by you.\n\n" +
                "If you would rather keep this phone's identity, cancel and link a " +
                "different Google account instead."
        )
    }
}

@Composable
fun UsernameDialog(
    value: String,
    error: String?,
    isSaving: Boolean,
    onValueChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    TemplateDialog(
        title = "Choose a display name",
        confirmLabel = if (isSaving) "Saving…" else "Save",
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        confirmEnabled = !isSaving && value.isNotBlank()
    ) {
        DialogBody(
            "This replaces \"Anonymous Student\" on timetables you share. " +
                "Names are first come, first served."
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text("Display name") },
            supportingText = {
                Text(error ?: "3-24 characters: letters, numbers, . _ -")
            },
            isError = error != null,
            singleLine = true,
            enabled = !isSaving,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
