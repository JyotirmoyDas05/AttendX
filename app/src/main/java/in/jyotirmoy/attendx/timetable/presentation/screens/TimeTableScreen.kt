package `in`.jyotirmoy.attendx.timetable.presentation.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.rounded.Publish
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import `in`.jyotirmoy.attendx.timetable.presentation.components.AddEditClassSheet
import `in`.jyotirmoy.attendx.timetable.presentation.components.SwipeableClassCard
import `in`.jyotirmoy.attendx.timetable.presentation.components.DaySelector
import `in`.jyotirmoy.attendx.timetable.presentation.components.NextUpCard
import `in`.jyotirmoy.attendx.timetable.presentation.components.TimetableCalendarView
import `in`.jyotirmoy.attendx.timetable.presentation.components.TodayCalendarIcon
import `in`.jyotirmoy.attendx.timetable.presentation.viewmodel.TimeTableViewModel
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.graphicsLayer
import `in`.jyotirmoy.attendx.core.presentation.util.bounceClick
import `in`.jyotirmoy.attendx.timetable.presentation.components.ExpressiveFabMenu
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeTableScreen(
    viewModel: TimeTableViewModel = hiltViewModel(),
    onNavigateToUpload: () -> Unit = {},
    onNavigateToMarketplace: () -> Unit = {}
) {
    val subjects by viewModel.subjects.collectAsState()
    val dailySchedule by viewModel.dailySchedule.collectAsState()
    val weeklySchedule by viewModel.weeklySchedule.collectAsState()
    val nextClass by viewModel.nextClass.collectAsState()
    val currentClass by viewModel.currentClass.collectAsState()
    val selectedDay by viewModel.selectedDay.collectAsState()
    val showSheet by viewModel.showAddEditSheet.collectAsState()
    val editingSchedule by viewModel.editingSchedule.collectAsState()
    val selectedIds by viewModel.selectedIds.collectAsState()
    val isCalendarView by viewModel.isCalendarView.collectAsState()
    var snapToNow by remember { androidx.compose.runtime.mutableStateOf(false) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    val isSelectionMode = selectedIds.isNotEmpty()

    Scaffold(
        topBar = {
            AnimatedVisibility(
                visible = isSelectionMode,
                enter = slideInVertically { -it } + fadeIn(),
                exit = slideOutVertically { -it } + fadeOut()
            ) {
                TopAppBar(
                    title = { 
                        Text("${selectedIds.size} selected")
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(Icons.Default.Close, "Clear selection")
                        }
                    },
                    actions = {
                        IconButton(
                                onClick = {
                                val count = selectedIds.size
                                viewModel.deleteSelected()
                                scope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = "$count class${if (count > 1) "es" else ""} deleted",
                                        actionLabel = "Undo",
                                        duration = SnackbarDuration.Short
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        viewModel.restoreDeletedClasses()
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Delete, "Delete selected")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                    actionColor = MaterialTheme.colorScheme.inversePrimary
                )
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = !isSelectionMode,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it }
            ) {
                var isFabExpanded by remember { androidx.compose.runtime.mutableStateOf(false) }
                
                ExpressiveFabMenu(
                    isExpanded = isFabExpanded,
                    onToggle = { isFabExpanded = !isFabExpanded },
                    onNavigateToUpload = { 
                        isFabExpanded = false
                        onNavigateToUpload() 
                    },
                        onNavigateToMarketplace = { 
                        isFabExpanded = false
                        onNavigateToMarketplace()
                    },
                    onManualAdd = { 
                        isFabExpanded = false
                        viewModel.showAddSheet() 
                    },
                    modifier = Modifier.offset(y = 30.dp)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header with view toggle
            AnimatedVisibility(visible = !isSelectionMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 8.dp, top = 16.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Timetable",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Snap to now button (only in calendar view)
                    if (isCalendarView) {
                        IconButton(onClick = { snapToNow = true }) {
                            TodayCalendarIcon(
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // View toggle button
                    IconButton(onClick = { viewModel.toggleViewMode() }) {
                        Icon(
                            imageVector = if (isCalendarView) Icons.Default.List else Icons.Default.CalendarMonth,
                            contentDescription = if (isCalendarView) "Switch to List" else "Switch to Calendar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            // Priority Dashboard
            // Priority Dashboard
            var showNextUp by remember { androidx.compose.runtime.mutableStateOf(false) }
            LaunchedEffect(Unit) { showNextUp = true }
            
            AnimatedVisibility(
                visible = showNextUp,
                enter = slideInVertically { -it } + fadeIn()
            ) {
                NextUpCard(
                    nextClass = nextClass,
                    currentClass = currentClass
                )
            }
            
            // AnimatedContent for view switching
            AnimatedContent(
                targetState = isCalendarView,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "viewToggle"
            ) { showCalendar ->
                if (showCalendar) {
                    // Calendar Grid View
                    TimetableCalendarView(
                        weeklySchedule = weeklySchedule,
                        onClassClick = { schedule ->
                            viewModel.showEditSheet(schedule)
                        },
                        snapToNow = snapToNow,
                        onSnapConsumed = { snapToNow = false },
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                } else {
                    // List View
                    Column {
                        // Day Selector
                        DaySelector(
                            selectedDay = selectedDay,
                            onDaySelected = viewModel::onDaySelected
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        // Class List
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            item {
                                Text(
                                    text = "Schedule",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                            
                            if (dailySchedule.isEmpty()) {
                                item {
                                    `in`.jyotirmoy.attendx.timetable.presentation.components.image.UndrawTask()
                                    Text(
                                        text = "No classes scheduled for this day.",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 16.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            } else {
                                itemsIndexed(dailySchedule, key = { _, item -> item.schedule.id }) { index, schedule ->
                                    // Staggered Entrance Animation
                                    val visibleState = remember { androidx.compose.animation.core.MutableTransitionState(false).apply { targetState = true } }
                                    
                                    AnimatedVisibility(
                                        visibleState = visibleState,
                                        enter = slideInVertically(
                                            animationSpec = androidx.compose.animation.core.tween(
                                                durationMillis = 300,
                                                delayMillis = index * 50 // Stagger by 50ms
                                            )
                                        ) { 50 } + fadeIn(
                                            animationSpec = androidx.compose.animation.core.tween(
                                                durationMillis = 300,
                                                delayMillis = index * 50
                                            )
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        `in`.jyotirmoy.attendx.timetable.presentation.components.SelectableClassCard(
                                            scheduleWithSubject = schedule,
                                            isSelected = selectedIds.contains(schedule.schedule.id),
                                            onLongPress = { viewModel.toggleSelection(schedule.schedule.id) },
                                            onClick = {
                                                if (selectedIds.isNotEmpty()) {
                                                    viewModel.toggleSelection(schedule.schedule.id)
                                                } else {
                                                    viewModel.showEditSheet(schedule)
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add/Edit Sheet
    if (showSheet) {
        AddEditClassSheet(
            subjects = subjects,
            initialSchedule = editingSchedule,
            initialDay = selectedDay,
            onDismiss = { viewModel.dismissSheet() },
            onSave = { schedule -> viewModel.saveClass(schedule) }
        )
    }
}
