@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.jyotirmoy.attendx.timetable.presentation.screens.template.update

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import `in`.jyotirmoy.attendx.core.common.LocalWeakHaptic
import `in`.jyotirmoy.attendx.core.presentation.components.text.AutoResizeableText
import `in`.jyotirmoy.attendx.timetable.domain.usecase.TemplateUpdateInfo

/**
 * Informational, never automatic. The author has published a newer version; the
 * user decides whether any of it lands in their timetable.
 */
@Composable
fun TemplateUpdateDialog(
    info: TemplateUpdateInfo,
    onReview: () -> Unit,
    onNotNow: () -> Unit,
    onStopChecking: () -> Unit
) {
    val weakHaptic = LocalWeakHaptic.current

    Dialog(
        onDismissRequest = onNotNow,
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
                    text = "Timetable update available",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${info.templateName} • v${info.version}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Whoever shared this timetable has published a newer version.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (info.incoming.isNotEmpty()) {
                    DiffRow(
                        icon = Icons.Rounded.Add,
                        text = "${info.incoming.size} " +
                            plural(info.incoming.size, "class", "classes") +
                            " you don't have yet"
                    )
                }

                if (info.newSubjects.isNotEmpty()) {
                    DiffRow(
                        icon = Icons.Rounded.Bookmark,
                        text = "${info.newSubjects.size} new " +
                            plural(info.newSubjects.size, "subject", "subjects")
                    )
                }

                if (info.localOnly.isNotEmpty()) {
                    DiffRow(
                        icon = Icons.Rounded.Remove,
                        text = "${info.localOnly.size} " +
                            plural(info.localOnly.size, "class", "classes") +
                            " you have that the new version doesn't"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    // The honest caveat: this diff cannot tell an author's removal
                    // apart from something the user added themselves, so it never
                    // deletes and says so.
                    text = "Nothing changes unless you choose it, and nothing is ever " +
                        "removed from your timetable. Anything you added yourself stays put.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                @Suppress("DEPRECATION")
                ButtonGroup(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { onNotNow(); weakHaptic() },
                        modifier = Modifier.weight(1f)
                    ) {
                        AutoResizeableText(
                            text = "Not now",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    Button(
                        onClick = { onReview(); weakHaptic() },
                        modifier = Modifier.weight(1f)
                    ) {
                        AutoResizeableText(text = "Review")
                    }
                }

                TextButton(
                    onClick = { onStopChecking(); weakHaptic() },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = "Stop checking for updates",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun DiffRow(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun plural(count: Int, singular: String, plural: String) =
    if (count == 1) singular else plural
