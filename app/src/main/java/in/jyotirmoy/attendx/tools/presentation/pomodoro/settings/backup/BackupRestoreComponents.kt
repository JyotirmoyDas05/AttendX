package `in`.jyotirmoy.attendx.tools.presentation.pomodoro.settings.backup

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.unit.dp
import `in`.jyotirmoy.attendx.R
import kotlinx.coroutines.launch

enum class BackupRestoreState {
    CHOOSE_FILE,
    LOADING,
    DONE
}

data class FileLocator(val uri: Uri? = null) {
    val path: String? get() = uri?.path
    val isNull: Boolean get() = uri == null
}

@Composable
fun rememberDirectoryPickerLauncher(
    onResult: (FileLocator) -> Unit
): () -> Unit {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        onResult(FileLocator(uri))
    }
    return { launcher.launch(null) }
}

@Composable
fun rememberFilePickerLauncher(
    mimeType: String,
    onResult: (FileLocator) -> Unit
): () -> Unit {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        onResult(FileLocator(uri))
    }
    return { launcher.launch(arrayOf(mimeType)) }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BackupBottomSheetTemplate(
    backupState: BackupRestoreState,
    onDismissRequest: () -> Unit,
    onStartAction: (FileLocator) -> Unit,
    resetBackupState: () -> Unit,
    openPicker: () -> Unit,
    icon: @Composable () -> Unit,
    titleText: String,
    labelText: AnnotatedString,
    buttonText: String,
    selectedFileLocator: FileLocator,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    val animatedBgColor by animateColorAsState(
        targetValue = when (backupState) {
            BackupRestoreState.DONE -> colorScheme.primaryContainer
            else -> colorScheme.surfaceBright
        },
        label = "backupBackground"
    )

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = {
            resetBackupState()
            onDismissRequest()
        },
        sheetState = sheetState,
        containerColor = colorScheme.surfaceContainer,
        contentColor = colorScheme.onSurface,
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            icon()
            Text(
                titleText,
                style = typography.headlineSmall,
                color = colorScheme.onSurface
            )
            Text(
                labelText,
                style = typography.bodyMedium,
                color = colorScheme.onSurfaceVariant
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(40.dp))
                    .clickable(
                        onClick = { openPicker() },
                        enabled = backupState == BackupRestoreState.CHOOSE_FILE
                    )
                    .drawBehind { drawRect(animatedBgColor) }
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                AnimatedContent(backupState, label = "stateIcon") {
                    when (it) {
                        BackupRestoreState.CHOOSE_FILE ->
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .background(colorScheme.onSurfaceVariant, shapes.extraLarge)
                                    .size(48.dp)
                            ) {
                                Icon(
                                    painterResource(R.drawable.folder),
                                    null,
                                    tint = colorScheme.surfaceVariant,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                        BackupRestoreState.LOADING ->
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(48.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(32.dp))
                            }

                        BackupRestoreState.DONE ->
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .background(colorScheme.onPrimaryContainer, shapes.extraLarge)
                                    .size(48.dp)
                            ) {
                                Icon(
                                    painterResource(R.drawable.check),
                                    null,
                                    tint = colorScheme.surfaceVariant,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                    }
                }

                Text(
                    selectedFileLocator.path?.substringAfter(':') ?: buttonText,
                    style = typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.align(Alignment.End)
            ) {
                FilledTonalButton(
                    onClick = {
                        scope.launch {
                            sheetState.hide()
                            resetBackupState()
                            onDismissRequest()
                        }
                    },
                    shapes = ButtonDefaults.shapes()
                ) { Text(stringResource(R.string.cancel)) }
                Button(
                    onClick = {
                        if (backupState == BackupRestoreState.DONE) {
                            scope.launch {
                                sheetState.hide()
                                resetBackupState()
                                onDismissRequest()
                            }
                        } else if (selectedFileLocator.isNull) openPicker()
                        else onStartAction(selectedFileLocator)
                    },
                    enabled = backupState != BackupRestoreState.LOADING,
                    shapes = ButtonDefaults.shapes()
                ) {
                    AnimatedContent(buttonText, label = "buttonText") {
                        Text(it)
                    }
                }
            }
        }
    }
}

@Composable
fun BackupBottomSheet(
    backupState: BackupRestoreState,
    onDismissRequest: () -> Unit,
    onStartBackup: (FileLocator) -> Unit,
    resetBackupState: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFileLocator: FileLocator by remember { mutableStateOf(FileLocator()) }

    val launchDirectoryPicker = rememberDirectoryPickerLauncher { fileLocator ->
        selectedFileLocator = fileLocator
        resetBackupState()
    }

    BackupBottomSheetTemplate(
        backupState = backupState,
        onDismissRequest = onDismissRequest,
        onStartAction = onStartBackup,
        resetBackupState = resetBackupState,
        openPicker = launchDirectoryPicker,
        icon = {
            Icon(
                painterResource(R.drawable.backup_40dp),
                null,
                tint = colorScheme.secondary
            )
        },
        titleText = stringResource(R.string.backup),
        labelText = AnnotatedString.fromHtml(
            stringResource(
                R.string.backup_dialog_desc,
                "<b>${stringResource(R.string.settings)} > ${stringResource(R.string.backup_and_restore)} > ${stringResource(R.string.restore)}</b>"
            )
        ),
        buttonText = if (backupState == BackupRestoreState.DONE) stringResource(R.string.exit)
        else if (selectedFileLocator.isNull) stringResource(R.string.choose_folder)
        else stringResource(R.string.backup),
        selectedFileLocator = selectedFileLocator,
        modifier = modifier
    )
}

@Composable
fun RestoreBottomSheet(
    restoreState: BackupRestoreState,
    onDismissRequest: () -> Unit,
    onStartRestore: (FileLocator) -> Unit,
    resetRestoreState: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFileLocator: FileLocator by remember { mutableStateOf(FileLocator()) }

    val launchFilePicker = rememberFilePickerLauncher("application/octet-stream") { locator ->
        selectedFileLocator = locator
        resetRestoreState()
    }

    BackupBottomSheetTemplate(
        backupState = restoreState,
        onDismissRequest = onDismissRequest,
        onStartAction = onStartRestore,
        resetBackupState = resetRestoreState,
        openPicker = launchFilePicker,
        icon = {
            Icon(
                painterResource(R.drawable.restore_40dp),
                null,
                tint = colorScheme.secondary
            )
        },
        titleText = stringResource(R.string.restore),
        labelText = AnnotatedString.fromHtml(stringResource(R.string.restore_dialog_desc)),
        buttonText = if (restoreState == BackupRestoreState.DONE) stringResource(R.string.restart_app)
        else if (selectedFileLocator.isNull) stringResource(R.string.choose_file)
        else stringResource(R.string.restore),
        selectedFileLocator = selectedFileLocator,
        modifier = modifier
    )
}

fun restartApp(context: Context) {
    val packageManager = context.packageManager
    val intent = packageManager.getLaunchIntentForPackage(context.packageName)
    val componentName = intent?.component

    val mainIntent = Intent.makeRestartActivityTask(componentName)
    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)

    context.startActivity(mainIntent)
    Runtime.getRuntime().exit(0)
}
