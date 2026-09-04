@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package `in`.jyotirmoy.attendx.timetable.presentation.screens.template

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.jyotirmoy.attendx.core.common.LocalWeakHaptic
import `in`.jyotirmoy.attendx.core.presentation.components.button.BackButton
import `in`.jyotirmoy.attendx.core.presentation.components.card.RoundedCornerCard
import `in`.jyotirmoy.attendx.core.presentation.components.layout.AdaptiveTwoColumn
import `in`.jyotirmoy.attendx.core.presentation.components.state.StatusMessage
import `in`.jyotirmoy.attendx.core.presentation.components.text.AutoResizeableText
import `in`.jyotirmoy.attendx.timetable.data.model.community.CommunityTemplate
import `in`.jyotirmoy.attendx.timetable.presentation.screens.template.components.LinkAccountDialog
import `in`.jyotirmoy.attendx.timetable.presentation.screens.template.components.LinkCollisionDialog
import `in`.jyotirmoy.attendx.timetable.presentation.screens.template.components.SelectionSummary
import `in`.jyotirmoy.attendx.timetable.presentation.screens.template.components.UsernameDialog
import `in`.jyotirmoy.attendx.timetable.presentation.screens.template.components.classSelectionItems
import `in`.jyotirmoy.attendx.timetable.presentation.screens.template.components.subjectSelectionItems

@Composable
fun UploadTemplateScreen(
    onNavigateBack: () -> Unit,
    viewModel: UploadTemplateViewModel = hiltViewModel()
) {
    val state by viewModel.state
    val context = LocalContext.current
    val weakHaptic = LocalWeakHaptic.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var selectedTab by rememberSaveable { mutableStateOf(UploadTab.Details) }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = if (state.isEditing) "Update Timetable" else "Share Timetable",
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = { BackButton() },
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            // Lives outside the tabs so it is reachable from all three, matching
            // where Import Template sits on the preview screen.
            UploadAction(
                isEditing = state.isEditing,
                isLoading = state.isLoading,
                onClick = {
                    weakHaptic()
                    viewModel.onEvent(UploadEvent.Upload)
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            PrimaryTabRow(selectedTabIndex = selectedTab.ordinal) {
                UploadTab.entries.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = {
                            weakHaptic()
                            selectedTab = tab
                        },
                        text = {
                            // Shrinks to fit its third of the row rather than
                            // ellipsising to "Timeta...".
                            AutoResizeableText(
                                text = tab.label,
                                style = MaterialTheme.typography.titleSmall,
                                textAlign = TextAlign.Center
                            )
                        },
                        icon = { Icon(tab.icon(), contentDescription = null) }
                    )
                }
            }

            when (selectedTab) {
                UploadTab.Details -> DetailsTab(
                    state = state,
                    onEvent = viewModel::onEvent
                )

                UploadTab.Subjects -> SubjectsTab(
                    state = state,
                    onEvent = viewModel::onEvent
                )

                UploadTab.Timetable -> TimetableTab(
                    state = state,
                    onEvent = viewModel::onEvent
                )
            }
        }
    }

    when (state.dialog) {
        UploadDialog.LinkExplainer -> LinkAccountDialog(
            onConfirm = { viewModel.onEvent(UploadEvent.ConfirmLink(context)) },
            onDismiss = { viewModel.onEvent(UploadEvent.DismissDialog) }
        )

        UploadDialog.LinkCollision -> LinkCollisionDialog(
            onConfirm = { viewModel.onEvent(UploadEvent.ConfirmCollisionSignIn) },
            onDismiss = { viewModel.onEvent(UploadEvent.DismissDialog) }
        )

        UploadDialog.UsernameEditor -> UsernameDialog(
            value = state.usernameInput,
            error = state.usernameError,
            isSaving = state.isAccountBusy,
            onValueChange = { viewModel.onEvent(UploadEvent.UsernameChanged(it)) },
            onConfirm = { viewModel.onEvent(UploadEvent.SaveUsername) },
            onDismiss = { viewModel.onEvent(UploadEvent.DismissDialog) }
        )

        null -> Unit
    }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) onNavigateBack()
    }

    LaunchedEffect(state.info) {
        state.info?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(UploadEvent.InfoShown)
        }
    }

    // Show the tab the error is about before the message names the field.
    LaunchedEffect(state.focusTab) {
        state.focusTab?.let {
            selectedTab = it
            viewModel.onEvent(UploadEvent.FocusHandled)
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(UploadEvent.ErrorShown)
        }
    }
}

private val UploadTab.label: String
    get() = when (this) {
        UploadTab.Details -> "Details"
        UploadTab.Subjects -> "Subjects"
        UploadTab.Timetable -> "Timetable"
    }

private fun UploadTab.icon() = when (this) {
    UploadTab.Details -> Icons.Rounded.Info
    UploadTab.Subjects -> Icons.Rounded.MenuBook
    UploadTab.Timetable -> Icons.Rounded.Schedule
}

@Composable
private fun UploadAction(
    isEditing: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        enabled = !isLoading
    ) {
        if (isLoading) {
            LoadingIndicator(modifier = Modifier.size(24.dp))
        } else {
            Icon(
                if (isEditing) Icons.Rounded.Sync else Icons.Rounded.CloudUpload,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            // Shrinks to fit before it truncates: this label is the longest in
            // the screen and the first to clip at a large font scale.
            AutoResizeableText(
                text = if (isEditing) "Update Template" else "Upload Template",
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun DetailsTab(
    state: UploadState,
    onEvent: (UploadEvent) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(key = "intro") {
            Text(
                text = if (state.isEditing) {
                    "Push your current schedule to this shared timetable. Everyone who " +
                        "imported it will be offered the update."
                } else {
                    "Help other students by sharing your class schedule. Your personal " +
                        "attendance data is never shared."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (state.myTemplates.isNotEmpty()) {
            item(key = "my-uploads") {
                MyUploadsSection(
                    templates = state.myTemplates,
                    editingId = state.editingId,
                    onEdit = { onEvent(UploadEvent.StartEditing(it)) },
                    onCancelEdit = { onEvent(UploadEvent.CancelEditing) }
                )
            }
        }

        item(key = "account") {
            AccountSection(
                isLinked = state.isLinked,
                username = state.username,
                isBusy = state.isAccountBusy,
                onLink = { onEvent(UploadEvent.ShowLinkExplainer) },
                onEditName = { onEvent(UploadEvent.ShowUsernameEditor) }
            )
        }

        item(key = "college") {
            OutlinedTextField(
                value = state.college,
                onValueChange = { onEvent(UploadEvent.EnteredCollege(it)) },
                label = { FieldLabel("College Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item(key = "department") {
            OutlinedTextField(
                value = state.department,
                onValueChange = { onEvent(UploadEvent.EnteredDepartment(it)) },
                label = { FieldLabel("Department / Branch") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item(key = "sem-section") {
            AdaptiveTwoColumn(
                first = {
                    OutlinedTextField(
                        value = state.semester,
                        onValueChange = { onEvent(UploadEvent.EnteredSemester(it)) },
                        label = { FieldLabel("Semester") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                second = {
                    OutlinedTextField(
                        value = state.section,
                        onValueChange = { onEvent(UploadEvent.EnteredSection(it)) },
                        label = { FieldLabel("Section") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            )
        }

        item(key = "year") {
            OutlinedTextField(
                value = state.academicYear,
                onValueChange = { onEvent(UploadEvent.EnteredYear(it)) },
                label = { FieldLabel("Academic Year (e.g. 2025-26)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SubjectsTab(
    state: UploadState,
    onEvent: (UploadEvent) -> Unit
) {
    if (state.availableSubjects.isEmpty()) {
        StatusMessage(
            icon = Icons.Rounded.MenuBook,
            title = "No subjects to share",
            description = "Add a subject on the home screen first."
        )
        return
    }

    val subjects = remember(state.availableSubjects) {
        state.availableSubjects.sortedBy { it.name }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        item(key = "summary") {
            SelectionSummary(
                label = "subjects",
                selectedCount = state.selectedSubjects.size,
                totalCount = state.availableSubjects.size,
                onToggleAll = { onEvent(UploadEvent.ToggleAllSubjects) }
            )
        }

        subjectSelectionItems(
            subjects = subjects,
            selected = state.selectedSubjects,
            onToggle = { onEvent(UploadEvent.ToggleSubject(it)) }
        )
    }
}

@Composable
private fun TimetableTab(
    state: UploadState,
    onEvent: (UploadEvent) -> Unit
) {
    if (state.availableClasses.isEmpty()) {
        StatusMessage(
            icon = Icons.Rounded.Schedule,
            title = "No classes to share",
            description = "Add class times to your timetable first."
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "summary") {
            SelectionSummary(
                label = "classes",
                selectedCount = state.selectedClasses.size,
                totalCount = state.availableClasses.size,
                onToggleAll = { onEvent(UploadEvent.ToggleAllClasses) },
                modifier = Modifier.padding(horizontal = 0.dp)
            )
        }

        classSelectionItems(
            classes = state.availableClasses,
            selected = state.selectedClasses,
            onToggle = { onEvent(UploadEvent.ToggleClass(it)) }
        )
    }
}

/** Outline-notch labels cannot wrap, so they ellipsize rather than clip. */
@Composable
private fun FieldLabel(text: String) {
    Text(text = text, maxLines = 1, overflow = TextOverflow.Ellipsis)
}

@Composable
private fun MyUploadsSection(
    templates: List<CommunityTemplate>,
    editingId: String?,
    onEdit: (String) -> Unit,
    onCancelEdit: () -> Unit
) {
    RoundedCornerCard(
        roundedCornerShape = RoundedCornerShape(20.dp),
        paddingValues = PaddingValues(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(vertical = 12.dp)) {
            Text(
                text = if (templates.size == 1) {
                    "You've shared 1 timetable"
                } else {
                    "You've shared ${templates.size} timetables"
                },
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            templates.forEach { template ->
                val isEditingThis = template.id == editingId
                ListItem(
                    headlineContent = { Text(template.name) },
                    supportingContent = {
                        Text(
                            "v${template.version} • ${template.downloads} downloads" +
                                if (template.section.isNotBlank()) " • ${template.section}" else ""
                        )
                    },
                    trailingContent = {
                        if (isEditingThis) {
                            TextButton(onClick = onCancelEdit) {
                                Text("Cancel", maxLines = 1)
                            }
                        } else {
                            OutlinedButton(onClick = { onEdit(template.id) }) {
                                Icon(
                                    Icons.Rounded.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Update", maxLines = 1)
                            }
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }
        }
    }
}

@Composable
private fun AccountSection(
    isLinked: Boolean,
    username: String?,
    isBusy: Boolean,
    onLink: () -> Unit,
    onEditName: () -> Unit
) {
    RoundedCornerCard(
        roundedCornerShape = RoundedCornerShape(20.dp),
        paddingValues = PaddingValues(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        ListItem(
            leadingContent = {
                Icon(Icons.Rounded.AccountCircle, contentDescription = null)
            },
            headlineContent = {
                Text(if (isLinked) (username ?: "Anonymous Student") else "Editing on this phone only")
            },
            supportingContent = {
                Text(
                    if (isLinked) {
                        "Linked — you can update these from any device."
                    } else {
                        "Reinstalling or changing phone means you can no longer update what you share."
                    }
                )
            },
            trailingContent = {
                when {
                    isBusy -> LoadingIndicator(modifier = Modifier.size(24.dp))
                    isLinked -> TextButton(onClick = onEditName) {
                        Text(if (username == null) "Set name" else "Change", maxLines = 1)
                    }
                    else -> OutlinedButton(onClick = onLink) {
                        Text("Link", maxLines = 1)
                    }
                }
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )
    }
}
