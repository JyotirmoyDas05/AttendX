package `in`.jyotirmoy.attendx.timetable.presentation.screens.template.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import `in`.jyotirmoy.attendx.timetable.data.model.community.CommunityTemplate
import `in`.jyotirmoy.attendx.timetable.data.model.community.TemplateClassEntry
import `in`.jyotirmoy.attendx.timetable.data.model.community.TemplateSubjectEntry
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatePreviewScreen(
    onNavigateBack: () -> Unit,
    viewModel: TemplatePreviewViewModel = hiltViewModel()
) {
    val state by remember { viewModel.state }
    var selectedTab by remember { mutableIntStateOf(0) }
    
    LaunchedEffect(state.importSuccess) {
        if (state.importSuccess) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Template Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            state.template?.let {
                Button(
                    onClick = { viewModel.importTemplate() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    enabled = !state.isImporting
                ) {
                    if (state.isImporting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Importing...")
                    } else {
                        Text("Import Template")
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.error != null) {
                Text(
                    text = state.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (state.template != null) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Template Header
                    TemplateHeader(template = state.template!!)
                    
                    // Tabs
                    TabRow(selectedTabIndex = selectedTab) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Subjects") },
                            icon = { Icon(Icons.Default.Info, contentDescription = null) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Timetable") },
                            icon = { Icon(Icons.Default.Schedule, contentDescription = null) }
                        )
                    }
                    
                    // Content
                    when (selectedTab) {
                        0 -> SubjectsList(
                            template = state.template!!,
                            selectedSubjects = state.selectedSubjects,
                            onToggle = viewModel::toggleSubject
                        )
                        1 -> TimetableWeeklyView(
                            template = state.template!!,
                            selectedClasses = state.selectedClasses,
                            onToggle = viewModel::toggleClass
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TemplateHeader(template: CommunityTemplate) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = template.name,
            style = MaterialTheme.typography.headlineSmall,
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
            text = "Uploaded by ${template.authorName}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
fun SubjectsList(
    template: CommunityTemplate,
    selectedSubjects: List<TemplateSubjectEntry>,
    onToggle: (TemplateSubjectEntry) -> Unit
) {
    val subjects = remember(template) {
        if (template.subjects.isNotEmpty()) {
            template.subjects.sortedBy { it.name }
        } else {
            // Fallback for old templates
            template.classes.map { it.subject }.distinct().sorted().map {
                TemplateSubjectEntry(name = it)
            }
        }
    }
    
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        if (subjects.isEmpty()) {
            item {
                Text(
                    "No subjects found in this template.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(subjects) { subject ->
                val isSelected = selectedSubjects.contains(subject)
                val containerColor = if (isSelected) 
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) 
                else 
                    MaterialTheme.colorScheme.surfaceContainer
                
                ListItem(
                    headlineContent = { 
                        Text(
                            text = subject.name,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    supportingContent = if (!subject.code.isNullOrBlank()) {
                        { Text(text = subject.code) }
                    } else null,
                    leadingContent = {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
                                    shape = MaterialTheme.shapes.small
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = subject.name.take(1).uppercase(),
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    },
                    trailingContent = {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { onToggle(subject) }
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = containerColor),
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .clickable { onToggle(subject) }
                )
            }
        }
    }
}

@Composable
fun TimetableWeeklyView(
    template: CommunityTemplate,
    selectedClasses: List<TemplateClassEntry>,
    onToggle: (TemplateClassEntry) -> Unit
) {
    val weekDays = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Group classes by day of week (1=Monday, 7=Sunday)
        val groupedClasses = template.classes.groupBy { it.dayOfWeek }
        
        items(weekDays.indices.toList()) { index ->
            val dayOfWeek = index + 1
            val classesForDay = groupedClasses[dayOfWeek]?.sortedBy { it.startTime } ?: emptyList()
            
            if (classesForDay.isNotEmpty()) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = weekDays[index],
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        classesForDay.forEach { classEntry ->
                            ClassItemRow(
                                classEntry = classEntry,
                                isSelected = selectedClasses.contains(classEntry),
                                onToggle = { onToggle(classEntry) }
                            )
                            if (classEntry != classesForDay.last()) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }
        }
        
        if (template.classes.isEmpty()) {
             item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("No usage schedule found.")
                }
            }
        }
    }
}

@Composable
fun ClassItemRow(
    classEntry: TemplateClassEntry,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    val containerColor = if (isSelected) 
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) 
    else 
        MaterialTheme.colorScheme.surface

    ListItem(
        headlineContent = {
            Text(
                text = classEntry.subject,
                fontWeight = FontWeight.SemiBold
            )
        },
        supportingContent = {
            Column {
                if (!classEntry.room.isNullOrBlank()) {
                    Text(
                        text = classEntry.room,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "${classEntry.type} • ${formatTime(classEntry.startTime)} - ${formatTime(classEntry.endTime)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        },
        trailingContent = {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() }
            )
        },
        colors = ListItemDefaults.colors(containerColor = containerColor),
        modifier = Modifier
            .padding(vertical = 4.dp)
            .clip(MaterialTheme.shapes.medium)
            .clickable { onToggle() }
    )
}

private fun formatTime(minutes: Long): String {
    val h = minutes / 60
    val m = minutes % 60
    return String.format(Locale.getDefault(), "%02d:%02d", h, m)
}
