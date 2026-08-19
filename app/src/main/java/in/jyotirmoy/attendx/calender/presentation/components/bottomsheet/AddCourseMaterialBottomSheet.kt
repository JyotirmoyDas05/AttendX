package `in`.jyotirmoy.attendx.calender.presentation.components.bottomsheet

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import `in`.jyotirmoy.attendx.R
import `in`.jyotirmoy.attendx.core.common.LocalWeakHaptic
import `in`.jyotirmoy.attendx.core.domain.model.CourseMaterial
import `in`.jyotirmoy.attendx.core.domain.model.MaterialType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCourseMaterialBottomSheet(
    subjectId: Int,
    subjectName: String = "",
    onDismiss: () -> Unit,
    onSaveMaterial: (CourseMaterial) -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val weakHaptic = LocalWeakHaptic.current

    var title by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(MaterialType.DOC) } // Document by default
    var uriOrUrl by remember { mutableStateOf("") }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var isCopyingFile by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}

            scope.launch {
                isCopyingFile = true
                val realName = queryRealFileName(context, uri)
                selectedFileName = realName
                if (title.isBlank()) {
                    title = realName.substringBeforeLast('.')
                }

                // Copy to AttendX/{SubjectName}/ dedicated directory
                val savedUriString = withContext(Dispatchers.IO) {
                    copyFileToAttendXStorage(context, uri, subjectName, realName)
                }
                uriOrUrl = savedUriString

                // Automatically classify type
                selectedType = when {
                    realName.endsWith(".pdf", ignoreCase = true) -> MaterialType.PDF
                    realName.endsWith(".jpg", ignoreCase = true) ||
                    realName.endsWith(".png", ignoreCase = true) ||
                    realName.endsWith(".jpeg", ignoreCase = true) -> MaterialType.IMAGE
                    else -> MaterialType.DOC
                }
                isCopyingFile = false
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.add_course_material),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                IconButton(
                    onClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
                    }
                ) {
                    Icon(imageVector = Icons.Outlined.Close, contentDescription = "Close")
                }
            }

            // Unified Option Chips: Document vs Web Link
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Pair(MaterialType.DOC, "Document" to Icons.Outlined.Description),
                    Pair(MaterialType.LINK, "Web Link" to Icons.Outlined.Link)
                ).forEach { (type, pair) ->
                    val (label, icon) = pair
                    val isSelected = if (type == MaterialType.DOC) {
                        selectedType != MaterialType.LINK
                    } else {
                        selectedType == MaterialType.LINK
                    }

                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            weakHaptic()
                            selectedType = type
                            if (type == MaterialType.LINK) {
                                uriOrUrl = ""
                                selectedFileName = null
                            }
                        },
                        label = { Text(label) },
                        leadingIcon = {
                            Icon(
                                imageVector = if (isSelected) Icons.Outlined.Check else icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    )
                }
            }

            // Title Field
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.material_title)) },
                placeholder = { Text("e.g. Lecture Slides, Reference Notes") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f),
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            )

            // Content input depending on type
            if (selectedType == MaterialType.LINK) {
                OutlinedTextField(
                    value = uriOrUrl,
                    onValueChange = { uriOrUrl = it },
                    label = { Text(stringResource(R.string.material_link)) },
                    placeholder = { Text("https://...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Link,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f),
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                )
            } else {
                OutlinedButton(
                    onClick = {
                        weakHaptic()
                        filePickerLauncher.launch(
                            arrayOf(
                                "application/pdf",
                                "application/msword",
                                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                "application/vnd.ms-powerpoint",
                                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                                "text/*",
                                "image/*",
                                "*/*"
                            )
                        )
                    },
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AttachFile,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isCopyingFile) "Saving file..." else (selectedFileName ?: stringResource(R.string.select_file)),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Save Button
            Button(
                onClick = {
                    if (title.isNotBlank() && uriOrUrl.isNotBlank() && !isCopyingFile) {
                        weakHaptic()
                        onSaveMaterial(
                            CourseMaterial(
                                subjectId = subjectId,
                                title = title.trim(),
                                type = selectedType,
                                uriOrUrl = uriOrUrl.trim(),
                                mimeType = when (selectedType) {
                                    MaterialType.PDF -> "application/pdf"
                                    MaterialType.IMAGE -> "image/*"
                                    MaterialType.DOC -> "application/octet-stream"
                                    MaterialType.LINK -> "text/html"
                                }
                            )
                        )
                        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
                    }
                },
                enabled = title.isNotBlank() && uriOrUrl.isNotBlank() && !isCopyingFile,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = "Save Material",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

/**
 * Retrieves the actual human-readable file name from Android's OpenableColumns.
 */
private fun queryRealFileName(context: Context, uri: Uri): String {
    var name: String? = null
    if (uri.scheme == "content") {
        try {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index >= 0) {
                        name = cursor.getString(index)
                    }
                }
            }
        } catch (_: Exception) {}
    }
    if (name.isNullOrBlank()) {
        name = uri.lastPathSegment?.substringAfterLast('/')?.substringAfterLast(':')
    }
    return name ?: "Document"
}

/**
 * Copies the picked file to the dedicated AttendX/{SubjectName}/ directory.
 */
private fun copyFileToAttendXStorage(context: Context, sourceUri: Uri, subjectName: String, fileName: String): String {
    return try {
        val sanitizedSubject = subjectName.ifBlank { "General" }.replace(Regex("[^a-zA-Z0-9._ -]"), "_")
        val baseDir = File(context.getExternalFilesDir(null), "AttendX/$sanitizedSubject")
        if (!baseDir.exists()) {
            baseDir.mkdirs()
        }
        val targetFile = File(baseDir, fileName)
        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            FileOutputStream(targetFile).use { output ->
                input.copyTo(output)
            }
        }
        Uri.fromFile(targetFile).toString()
    } catch (_: Exception) {
        sourceUri.toString()
    }
}
