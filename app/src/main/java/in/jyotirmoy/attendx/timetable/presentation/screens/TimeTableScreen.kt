package `in`.jyotirmoy.attendx.timetable.presentation.screens

import android.app.Activity
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import `in`.jyotirmoy.attendx.core.presentation.components.dialog.ConfirmDeleteDialog
import `in`.jyotirmoy.attendx.timetable.presentation.components.AddEditClassSheet
import `in`.jyotirmoy.attendx.timetable.presentation.components.DaySelector
import `in`.jyotirmoy.attendx.timetable.presentation.components.NextUpCard
import `in`.jyotirmoy.attendx.timetable.presentation.components.SelectableClassCard
import `in`.jyotirmoy.attendx.timetable.presentation.components.TimetableCalendarView
import `in`.jyotirmoy.attendx.timetable.presentation.components.TodayCalendarIcon
import `in`.jyotirmoy.attendx.timetable.presentation.components.ViewClassSheet
import `in`.jyotirmoy.attendx.timetable.presentation.viewmodel.TimeTableViewModel
import `in`.jyotirmoy.attendx.timetable.presentation.viewmodel.TimetableViewMode

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun TimeTableScreen(
    viewModel: TimeTableViewModel = hiltViewModel(),
    onNavigateToUpload: () -> Unit = {},
    onNavigateToMarketplace: () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val subjects by viewModel.subjects.collectAsState()
    val selectedDay by viewModel.selectedDay.collectAsState()
    val dailySchedule by viewModel.dailySchedule.collectAsState()
    val nextClass by viewModel.nextClass.collectAsState()
    val currentClass by viewModel.currentClass.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val threeDayStartDay by viewModel.threeDayStartDay.collectAsState()
    val weeklySchedule by viewModel.weeklySchedule.collectAsState()

    val showAddEditSheet by viewModel.showAddEditSheet.collectAsState()
    val editingSchedule by viewModel.editingSchedule.collectAsState()
    val initialSlotTime by viewModel.initialSlotTime.collectAsState()
    val viewingSchedule by viewModel.viewingSchedule.collectAsState()

    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val selectedClassIds by viewModel.selectedClassIds.collectAsState()
    val showDeleteConfirmDialog by viewModel.showDeleteConfirmDialog.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var snapToNow by remember { mutableStateOf(false) }

    // Update status bar icon contrast
    val view = LocalView.current
    val isDarkTheme = MaterialTheme.colorScheme.background.toArgb() == Color.Black.toArgb()
    if (!view.isInEditMode) {
        LaunchedEffect(isDarkTheme) {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !isDarkTheme
        }
    }

    // Handle back button for selection mode
    BackHandler(enabled = isSelectionMode) {
        viewModel.deselectAllClasses()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            if (isSelectionMode) {
                TopAppBar(
                    title = {
                        Text("${selectedClassIds.size} selected")
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.deselectAllClasses() }) {
                            Icon(Icons.Default.Close, contentDescription = "Close selection")
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.selectAllClasses() }) {
                            Icon(Icons.Default.Check, contentDescription = "Select all")
                        }
                        IconButton(
                            onClick = { viewModel.showDeleteConfirmation() },
                            enabled = selectedClassIds.isNotEmpty()
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete selected")
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (!isSelectionMode) {
                FloatingActionButton(
                    onClick = { viewModel.showAddSheet() },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    shape = CircleShape,
                    modifier = Modifier
                        .padding(bottom = if (isLandscape) 46.dp else 72.dp)
                        .size(if (isLandscape) 44.dp else 56.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add Class",
                        modifier = Modifier.size(if (isLandscape) 20.dp else 24.dp)
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header with view switcher
            AnimatedVisibility(visible = !isSelectionMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 14.dp,
                            end = 12.dp,
                            top = if (isLandscape) 4.dp else 10.dp,
                            bottom = if (isLandscape) 2.dp else 6.dp
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Timetable",
                        style = if (isLandscape) MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold)
                               else MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Jump to today / snap to now button
                    IconButton(
                        onClick = { 
                            snapToNow = true
                            viewModel.snapToToday()
                        },
                        modifier = Modifier.size(if (isLandscape) 34.dp else 40.dp)
                    ) {
                        TodayCalendarIcon(
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))

                    // M3 Expressive 4-Way Segmented View Switcher (Schedule | Day | 3-Day | Week)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .padding(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (mode in TimetableViewMode.entries) {
                            val isSelected = viewMode == mode
                            val label = when (mode) {
                                TimetableViewMode.SCHEDULE -> "Schedule"
                                TimetableViewMode.DAY -> "Day"
                                TimetableViewMode.THREE_DAY -> "3-Day"
                                TimetableViewMode.WEEK -> "Week"
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else Color.Transparent
                                    )
                                    .clickable { viewModel.setViewMode(mode) }
                                    .padding(
                                        horizontal = if (isLandscape) 6.dp else 8.dp,
                                        vertical = if (isLandscape) 3.dp else 5.dp
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = if (isLandscape) 10.5.sp else 11.sp
                                    ),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            
            // Priority Dashboard (Shown in Portrait only to maximize landscape space)
            var showNextUp by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { showNextUp = true }

            AnimatedVisibility(
                visible = showNextUp && !isLandscape,
                enter = slideInVertically { -it } + fadeIn(),
                exit = slideOutVertically { -it } + fadeOut()
            ) {
                NextUpCard(
                    nextClass = nextClass,
                    currentClass = currentClass
                )
            }
            
            // SharedTransitionLayout for view switching with morphing transitions
            SharedTransitionLayout(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = viewMode,
                    transitionSpec = {
                        (scaleIn(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            initialScale = 0.94f
                        ) + fadeIn(animationSpec = tween(200)))
                        .togetherWith(
                            scaleOut(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                ),
                                targetScale = 0.94f
                            ) + fadeOut(animationSpec = tween(160))
                        )
                    },
                    label = "viewModeTransition",
                    modifier = Modifier.fillMaxSize()
                ) { currentViewMode ->
                    when (currentViewMode) {
                        TimetableViewMode.SCHEDULE -> {
                            // Schedule / Agenda List View
                            Column(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                // Day Selector
                                DaySelector(
                                    selectedDay = selectedDay,
                                    onDaySelected = viewModel::onDaySelected
                                )
                                
                                Spacer(modifier = Modifier.height(if (isLandscape) 6.dp else 12.dp))

                                // Class List
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    items(
                                        items = dailySchedule,
                                        key = { it.schedule.id }
                                    ) { scheduleWithSubject ->
                                        SelectableClassCard(
                                            scheduleWithSubject = scheduleWithSubject,
                                            isSelected = selectedClassIds.contains(scheduleWithSubject.schedule.id),
                                            onLongPress = {
                                                if (isSelectionMode) {
                                                    viewModel.toggleClassSelection(scheduleWithSubject.schedule.id)
                                                } else {
                                                    viewModel.showEditSheet(scheduleWithSubject)
                                                }
                                            },
                                            onClick = {
                                                if (isSelectionMode) {
                                                    viewModel.toggleClassSelection(scheduleWithSubject.schedule.id)
                                                } else {
                                                    viewModel.showViewSheet(scheduleWithSubject)
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        TimetableViewMode.DAY -> {
                            // 1-Day Timeline Grid View
                            TimetableCalendarView(
                                weeklySchedule = weeklySchedule,
                                onClassClick = { schedule ->
                                    viewModel.showViewSheet(schedule)
                                },
                                onClassLongClick = { schedule ->
                                    viewModel.showEditSheet(schedule)
                                },
                                onEmptySlotClick = { day, time ->
                                    viewModel.showAddSheetForSlot(day, time)
                                },
                                onShiftDays = { delta -> viewModel.shiftStartDay(delta) },
                                numDays = 1,
                                startDay = selectedDay,
                                currentClassId = currentClass?.schedule?.id,
                                snapToNow = snapToNow,
                                onSnapConsumed = { snapToNow = false },
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                        TimetableViewMode.THREE_DAY -> {
                            // 3-Day Shiftable Timeline Grid View
                            TimetableCalendarView(
                                weeklySchedule = weeklySchedule,
                                onClassClick = { schedule ->
                                    viewModel.showViewSheet(schedule)
                                },
                                onClassLongClick = { schedule ->
                                    viewModel.showEditSheet(schedule)
                                },
                                onEmptySlotClick = { day, time ->
                                    viewModel.showAddSheetForSlot(day, time)
                                },
                                onShiftDays = { delta -> viewModel.shiftStartDay(delta) },
                                numDays = 3,
                                startDay = threeDayStartDay,
                                currentClassId = currentClass?.schedule?.id,
                                snapToNow = snapToNow,
                                onSnapConsumed = { snapToNow = false },
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                        TimetableViewMode.WEEK -> {
                            // 7-Day Full Week Timeline Grid View
                            TimetableCalendarView(
                                weeklySchedule = weeklySchedule,
                                onClassClick = { schedule ->
                                    viewModel.showViewSheet(schedule)
                                },
                                onClassLongClick = { schedule ->
                                    viewModel.showEditSheet(schedule)
                                },
                                onEmptySlotClick = { day, time ->
                                    viewModel.showAddSheetForSlot(day, time)
                                },
                                onShiftDays = { /* Week fits full screen */ },
                                numDays = 7,
                                currentClassId = currentClass?.schedule?.id,
                                snapToNow = snapToNow,
                                onSnapConsumed = { snapToNow = false },
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // View Class Details Bottom Sheet (XCalendar style + attendance actions)
    viewingSchedule?.let { schedule ->
        ViewClassSheet(
            scheduleWithSubject = schedule,
            attendanceListFlow = viewModel.getAttendanceForSubject(schedule.subject.id),
            attendanceCountsFlow = viewModel.getSubjectAttendanceCounts(schedule.subject.id),
            onDismiss = { viewModel.hideViewSheet() },
            onEdit = { viewModel.showEditSheet(it) },
            onMarkAttendance = { subjectId, date, status ->
                viewModel.markAttendance(subjectId, date, status)
            }
        )
    }

    // Add/Edit Bottom Sheet
    if (showAddEditSheet) {
        AddEditClassSheet(
            subjects = subjects,
            initialSchedule = editingSchedule,
            initialDay = selectedDay,
            initialStartTime = initialSlotTime?.first,
            initialEndTime = initialSlotTime?.second,
            onDismiss = { viewModel.hideAddEditSheet() },
            onSave = { schedule ->
                viewModel.saveSchedule(schedule)
            }
        )
    }

    // Confirmation Dialog for Bulk Delete
    if (showDeleteConfirmDialog) {
        ConfirmDeleteDialog(
            onConfirm = { viewModel.deleteSelectedClasses() },
            onDismiss = { viewModel.hideDeleteConfirmation() }
        )
    }
}
