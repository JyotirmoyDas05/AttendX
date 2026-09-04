@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package `in`.jyotirmoy.attendx.timetable.presentation.screens.template.preview

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import `in`.jyotirmoy.attendx.core.presentation.components.button.BackButton
import `in`.jyotirmoy.attendx.core.presentation.components.state.StatusMessage
import `in`.jyotirmoy.attendx.core.presentation.components.text.AutoResizeableText
import `in`.jyotirmoy.attendx.timetable.presentation.screens.template.components.SelectionSummary
import `in`.jyotirmoy.attendx.timetable.presentation.screens.template.components.classSelectionItems
import `in`.jyotirmoy.attendx.timetable.presentation.screens.template.components.subjectSelectionItems
import `in`.jyotirmoy.attendx.timetable.data.model.community.CommunityTemplate
import `in`.jyotirmoy.attendx.timetable.data.model.community.TemplateClassEntry
import `in`.jyotirmoy.attendx.timetable.data.model.community.TemplateSubjectEntry

private sealed interface PreviewContent {
    data object Loading : PreviewContent
    data class Failed(val message: String) : PreviewContent
    data class Loaded(val template: CommunityTemplate) : PreviewContent
}

@Composable
fun TemplatePreviewScreen(
    onNavigateBack: () -> Unit,
    viewModel: TemplatePreviewViewModel = hiltViewModel()
) {
    val state by viewModel.state
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(state.importSuccess) {
        if (state.importSuccess) {
            snackbarHostState.showSnackbar("Template imported")
            onNavigateBack()
        }
    }

    // A load failure replaces the content; an import failure is transient and
    // belongs in a snackbar over the still-usable template.
    LaunchedEffect(state.error) {
        val message = state.error
        if (message != null && state.template != null) {
            snackbarHostState.showSnackbar(message)
        }
    }

    val content: PreviewContent = when {
        state.isLoading -> PreviewContent.Loading
        state.template != null -> PreviewContent.Loaded(state.template!!)
        else -> PreviewContent.Failed(state.error ?: "Template not found")
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = "Template Details",
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
            if (content is PreviewContent.Loaded) {
                Button(
                    onClick = { viewModel.importTemplate() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    enabled = !state.isImporting
                ) {
                    if (state.isImporting) {
                        LoadingIndicator(modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        AutoResizeableText(
                            text = "Importing…",
                            style = MaterialTheme.typography.labelLarge,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        // Height follows the label so a scaled-up or translated
                        // string grows the button rather than being cut off.
                        AutoResizeableText(
                            text = "Import Template",
                            style = MaterialTheme.typography.labelLarge,
                            textAlign = TextAlign.Center,
                            maxLines = 2
                        )
                    }
                }
            }
        }
    ) { padding ->
        AnimatedContent(
            targetState = content,
            contentKey = { it::class },
            label = "template-preview",
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) { target ->
            when (target) {
                is PreviewContent.Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }

                is PreviewContent.Failed -> StatusMessage(
                    icon = Icons.Rounded.CloudOff,
                    title = "Couldn't load this template",
                    description = target.message,
                    tint = MaterialTheme.colorScheme.error
                )

                is PreviewContent.Loaded -> Column(modifier = Modifier.fillMaxSize()) {
                    TemplateHeader(template = target.template)

                    PrimaryTabRow(selectedTabIndex = selectedTab) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = {
                                AutoResizeableText(
                                    text = "Subjects",
                                    style = MaterialTheme.typography.titleSmall,
                                    textAlign = TextAlign.Center
                                )
                            },
                            icon = { Icon(Icons.Rounded.Info, contentDescription = null) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = {
                                AutoResizeableText(
                                    text = "Timetable",
                                    style = MaterialTheme.typography.titleSmall,
                                    textAlign = TextAlign.Center
                                )
                            },
                            icon = { Icon(Icons.Rounded.Schedule, contentDescription = null) }
                        )
                    }

                    when (selectedTab) {
                        0 -> SubjectsList(
                            template = target.template,
                            selectedSubjects = state.selectedSubjects,
                            onToggle = viewModel::toggleSubject,
                            onToggleAll = viewModel::toggleAllSubjects
                        )
                        else -> TimetableWeeklyView(
                            template = target.template,
                            selectedClasses = state.selectedClasses,
                            onToggle = viewModel::toggleClass,
                            onToggleAll = viewModel::toggleAllClasses
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TemplateHeader(template: CommunityTemplate) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = template.name,
            style = MaterialTheme.typography.titleLargeEmphasized,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${template.college} • ${template.department}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Semester ${template.semester} • Section ${template.section}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Shared by ${template.authorName}" +
                if (template.version > 1) " • version ${template.version}" else "",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
private fun SubjectsList(
    template: CommunityTemplate,
    selectedSubjects: List<TemplateSubjectEntry>,
    onToggle: (TemplateSubjectEntry) -> Unit,
    onToggleAll: () -> Unit
) {
    val subjects = remember(template) {
        if (template.subjects.isNotEmpty()) {
            template.subjects.sortedBy { it.name }
        } else {
            // Fallback for templates published before subjects were stored
            template.classes.map { it.subject }.distinct().sorted().map {
                TemplateSubjectEntry(name = it)
            }
        }
    }

    if (subjects.isEmpty()) {
        StatusMessage(
            icon = Icons.Rounded.Info,
            title = "No subjects in this template"
        )
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(vertical = 8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item(key = "summary") {
            SelectionSummary(
                label = "subjects",
                selectedCount = selectedSubjects.size,
                totalCount = subjects.size,
                onToggleAll = onToggleAll
            )
        }

        subjectSelectionItems(
            subjects = subjects,
            selected = selectedSubjects,
            onToggle = onToggle
        )
    }
}

@Composable
private fun TimetableWeeklyView(
    template: CommunityTemplate,
    selectedClasses: List<TemplateClassEntry>,
    onToggle: (TemplateClassEntry) -> Unit,
    onToggleAll: () -> Unit
) {
    if (template.classes.isEmpty()) {
        StatusMessage(
            icon = Icons.Rounded.Schedule,
            title = "No schedule in this template"
        )
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item(key = "summary") {
            SelectionSummary(
                label = "classes",
                selectedCount = selectedClasses.size,
                totalCount = template.classes.size,
                onToggleAll = onToggleAll
            )
        }

        classSelectionItems(
            classes = template.classes,
            selected = selectedClasses,
            onToggle = onToggle
        )
    }
}
