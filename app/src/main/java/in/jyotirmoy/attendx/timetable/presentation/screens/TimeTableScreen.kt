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
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import `in`.jyotirmoy.attendx.core.common.LocalFloatingNavBarPadding
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

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalSharedTransitionApi::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun TimeTableScreen(
    viewModel: TimeTableViewModel = hiltViewModel(),
    onNavigateToUpload: () -> Unit = {},
    onNavigateToMarketplace: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val floatingNavBarPadding = LocalFloatingNavBarPadding.current
    val bottomContentPadding = if (floatingNavBarPadding > 0.dp) floatingNavBarPadding else if (isLandscape) 72.dp else 104.dp

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
    var showMoreMenu by remember { mutableStateOf(false) }
    var showLandscapeMoreMenu by remember { mutableStateOf(false) }

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
                if (isLandscape) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 14.dp, end = 12.dp, top = 4.dp, bottom = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Timetable",
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
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
                            modifier = Modifier.size(34.dp)
                        ) {
                            TodayCalendarIcon(tint = MaterialTheme.colorScheme.primary)
                        }

                        Spacer(modifier = Modifier.width(2.dp))

                        // Settings button
                        IconButton(
                            onClick = onNavigateToSettings,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(2.dp))

                        // Material 3 Expressive More Options Menu (Import / Export)
                        TimetableExpressiveMoreMenu(
                            expanded = showLandscapeMoreMenu,
                            onDismissRequest = { showLandscapeMoreMenu = false },
                            onToggle = { showLandscapeMoreMenu = true },
                            onNavigateToMarketplace = onNavigateToMarketplace,
                            onNavigateToUpload = onNavigateToUpload,
                            buttonSize = 34.dp,
                            iconSize = 20.dp
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        // M3 Expressive 4-Way Segmented View Switcher
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
                                        .padding(horizontal = 8.dp, vertical = 3.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                                        maxLines = 1,
                                        softWrap = false,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp)
                    ) {
                        // Title row with jump-to-today and settings actions
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 12.dp, top = 8.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Timetable",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
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
                                modifier = Modifier.size(40.dp)
                            ) {
                                TodayCalendarIcon(tint = MaterialTheme.colorScheme.primary)
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            // Settings button
                            IconButton(
                                onClick = onNavigateToSettings,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Settings,
                                    contentDescription = "Settings",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            // Material 3 Expressive More Options Menu (Import / Export)
                            TimetableExpressiveMoreMenu(
                                expanded = showMoreMenu,
                                onDismissRequest = { showMoreMenu = false },
                                onToggle = { showMoreMenu = true },
                                onNavigateToMarketplace = onNavigateToMarketplace,
                                onNavigateToUpload = onNavigateToUpload,
                                buttonSize = 40.dp,
                                iconSize = 24.dp
                            )
                        }

                    // Adaptive 4-Way Segmented View Switcher (Schedule | Day | 3-Day | Week)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .padding(3.dp),
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
                                    .weight(1f)
                                    .clip(RoundedCornerShape(11.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else Color.Transparent
                                    )
                                    .clickable { viewModel.setViewMode(mode) }
                                    .padding(vertical = 7.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
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
                                    contentPadding = PaddingValues(
                                        start = 16.dp,
                                        end = 16.dp,
                                        top = 6.dp,
                                        bottom = bottomContentPadding
                                    ),
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
                                bottomPadding = bottomContentPadding,
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
                                bottomPadding = bottomContentPadding,
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
                                bottomPadding = bottomContentPadding,
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
            onDelete = { scheduleId ->
                viewModel.deleteSchedule(scheduleId)
            },
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TimetableExpressiveMoreMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onToggle: () -> Unit,
    onNavigateToMarketplace: () -> Unit,
    onNavigateToUpload: () -> Unit,
    buttonSize: androidx.compose.ui.unit.Dp = 40.dp,
    iconSize: androidx.compose.ui.unit.Dp = 24.dp
) {
    Box {
        IconButton(
            onClick = onToggle,
            modifier = Modifier.size(buttonSize)
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More Options",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(iconSize)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissRequest,
            shape = RoundedCornerShape(22.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp,
            shadowElevation = 8.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
        ) {
            // 1. Import Template
            DropdownMenuItem(
                text = {
                    Column {
                        Text(
                            text = "Import Template",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Browse community schedules",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                leadingIcon = {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.CloudDownload,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                onClick = {
                    onDismissRequest()
                    onNavigateToMarketplace()
                }
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
            )

            // 2. Share Schedule
            DropdownMenuItem(
                text = {
                    Column {
                        Text(
                            text = "Share Schedule",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Upload timetable to community",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                leadingIcon = {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.CloudUpload,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                onClick = {
                    onDismissRequest()
                    onNavigateToUpload()
                }
            )
        }
    }
}
