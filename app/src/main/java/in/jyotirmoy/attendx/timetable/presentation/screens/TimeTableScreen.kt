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
import `in`.jyotirmoy.attendx.R
import `in`.jyotirmoy.attendx.core.common.LocalFloatingNavBarPadding
import `in`.jyotirmoy.attendx.core.presentation.components.dialog.ConfirmDeleteDialog
import `in`.jyotirmoy.attendx.timetable.presentation.components.AddEditClassSheet
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import `in`.jyotirmoy.attendx.core.presentation.components.pager.InfiniteSwipePager
import `in`.jyotirmoy.attendx.core.presentation.components.state.StatusMessage
import `in`.jyotirmoy.attendx.timetable.domain.classesOn
import `in`.jyotirmoy.attendx.timetable.presentation.components.CalendarPaging
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.ui.platform.LocalDensity
import `in`.jyotirmoy.attendx.timetable.presentation.components.GRID_START_MINUTES
import `in`.jyotirmoy.attendx.timetable.presentation.components.GRID_END_MINUTES
import java.time.LocalTime
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.first
import `in`.jyotirmoy.attendx.core.presentation.components.text.AutoResizeableText
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.draw.clipToBounds
import `in`.jyotirmoy.attendx.timetable.presentation.components.DEFAULT_HOUR_HEIGHT
import `in`.jyotirmoy.attendx.timetable.presentation.components.DaySelector
import `in`.jyotirmoy.attendx.timetable.presentation.components.NextUpCard
import `in`.jyotirmoy.attendx.timetable.presentation.components.SelectableClassCard
import `in`.jyotirmoy.attendx.timetable.presentation.components.TimetableCalendarView
import `in`.jyotirmoy.attendx.timetable.presentation.components.TodayCalendarIcon
import `in`.jyotirmoy.attendx.timetable.presentation.screens.template.update.TemplateUpdateBadge
import `in`.jyotirmoy.attendx.timetable.presentation.screens.template.update.TemplateUpdateViewModel
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

    // Separate instance from the nav-graph one, but the pending update lives in a
    // @Singleton notifier so both observe the same state.
    val templateUpdateViewModel: TemplateUpdateViewModel = hiltViewModel()
    val pendingTemplateUpdate by templateUpdateViewModel.pending.collectAsState()

    val subjects by viewModel.subjects.collectAsState()
    val anchorDate by viewModel.anchorDate.collectAsState()
    val today by viewModel.today.collectAsState()
    val selectedDay by viewModel.selectedDay.collectAsState()
    val dailySchedule by viewModel.dailySchedule.collectAsState()
    val nextClass by viewModel.nextClass.collectAsState()
    val currentClass by viewModel.currentClass.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val weeklyClasses by viewModel.weeklyClasses.collectAsState()

    // Owned here rather than inside the grid so that paging to another day, or
    // switching between Day and Week, keeps the same time of day and zoom level
    // on screen instead of jumping back to the top.
    val gridScrollState = rememberScrollState()
    var hourHeight by rememberSaveable { mutableFloatStateOf(DEFAULT_HOUR_HEIGHT) }

    val showAddEditSheet by viewModel.showAddEditSheet.collectAsState()
    val editingSchedule by viewModel.editingSchedule.collectAsState()
    val initialSlotTime by viewModel.initialSlotTime.collectAsState()
    val viewingSchedule by viewModel.viewingSchedule.collectAsState()

    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val selectedClassIds by viewModel.selectedClassIds.collectAsState()
    val showDeleteConfirmDialog by viewModel.showDeleteConfirmDialog.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var snapToNow by remember { mutableStateOf(false) }

    // Centring the grid on the current time moved up here with the scroll state.
    // Doing it inside the grid meant it re-ran on every view switch, yanking the
    // user back to now in the middle of the morph.
    val density = LocalDensity.current
    fun nowScrollTarget(): Int? {
        val now = LocalTime.now()
        val minutes = now.hour * 60 + now.minute
        if (minutes !in GRID_START_MINUTES..GRID_END_MINUTES) return null
        val hourHeightPx = with(density) { hourHeight.dp.toPx() }
        return ((minutes - GRID_START_MINUTES) / 60f * hourHeightPx - hourHeightPx)
            .toInt()
            .coerceAtLeast(0)
    }

    LaunchedEffect(Unit) {
        // The scroll state is created here but filled by the grid a frame later,
        // so scrolling immediately would clamp to 0 and leave the view at 8am.
        snapshotFlow { gridScrollState.maxValue }.first { it > 0 }
        nowScrollTarget()?.let { gridScrollState.scrollTo(it) }
    }

    LaunchedEffect(snapToNow) {
        if (snapToNow) {
            nowScrollTarget()?.let { gridScrollState.animateScrollTo(it) }
            snapToNow = false
        }
    }
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
                        // Measured nav bar footprint — never a fixed dp, so the
                        // FAB can't slide behind the bar at any screen size,
                        // font scale or orientation.
                        .padding(bottom = bottomContentPadding)
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
                            TodayCalendarIcon(
                                dayOfMonth = today.dayOfMonth,
                                tint = MaterialTheme.colorScheme.primary
                            )
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

                        TemplateUpdateBadge(
                            visible = pendingTemplateUpdate != null,
                            onClick = { templateUpdateViewModel.requestShow() },
                            size = 34.dp,
                            iconSize = 18.dp
                        )

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
                                    AutoResizeableText(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                                        softWrap = false,
                                        textAlign = TextAlign.Center,
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
                                TodayCalendarIcon(
                                    dayOfMonth = today.dayOfMonth,
                                    tint = MaterialTheme.colorScheme.primary
                                )
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

                            TemplateUpdateBadge(
                                visible = pendingTemplateUpdate != null,
                                onClick = { templateUpdateViewModel.requestShow() }
                            )

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
                                // Shrinks to fit its quarter of the switcher
                                // rather than ellipsising "Schedule" away.
                                AutoResizeableText(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                                    softWrap = false,
                                    textAlign = TextAlign.Center,
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
                    // Anything the transition lifts stays inside the grid area
                    // instead of sliding up over the header.
                    modifier = Modifier
                        .fillMaxSize()
                        .clipToBounds()
                ) { currentViewMode ->
                    val numDays = when (currentViewMode) {
                        TimetableViewMode.SCHEDULE -> 1
                        TimetableViewMode.DAY -> 1
                        TimetableViewMode.THREE_DAY -> 3
                        TimetableViewMode.WEEK -> 7
                    }

                    when (currentViewMode) {
                        TimetableViewMode.SCHEDULE -> {
                            Column(modifier = Modifier.fillMaxSize()) {
                                DaySelector(
                                    selectedDate = anchorDate,
                                    today = today,
                                    onDaySelected = viewModel::onDaySelected
                                )

                                Spacer(modifier = Modifier.height(if (isLandscape) 6.dp else 12.dp))

                                // The agenda pages by day just like the grids, so
                                // the day strip above is no longer the only way to
                                // move between days.
                                InfiniteSwipePager(
                                    reference = anchorDate,
                                    offsetOf = { current, base ->
                                        CalendarPaging.pageOffsetOf(current, base, numDays)
                                    },
                                    referenceAt = { base, offset ->
                                        CalendarPaging.windowStartAt(base, offset, numDays)
                                    },
                                    onReferenceChange = viewModel::onDaySelected,
                                    modifier = Modifier.fillMaxSize(),
                                    key = { it }
                                ) { pageDate ->
                                    val classes = remember(weeklyClasses, pageDate) {
                                        classesOn(weeklyClasses, pageDate)
                                            .sortedBy { it.schedule.startTime }
                                    }

                                    if (classes.isEmpty()) {
                                        StatusMessage(
                                            illustration = R.drawable.undraw_note_list,
                                            title = "Nothing scheduled",
                                            description = dayLabelOf(pageDate),
                                            // Same allowance the populated list
                                            // makes, so the caption is not left
                                            // sitting under the floating nav bar.
                                            modifier = Modifier.padding(
                                                bottom = bottomContentPadding
                                            )
                                        )
                                    } else {
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
                                                items = classes,
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
                                                            viewModel.showViewSheet(scheduleWithSubject, pageDate)
                                                        }
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        else -> {
                            TimetableCalendarView(
                                weeklyClasses = weeklyClasses,
                                anchorDate = anchorDate,
                                numDays = numDays,
                                animatedVisibilityScope = this@AnimatedContent,
                                verticalScrollState = gridScrollState,
                                hourHeight = hourHeight,
                                onHourHeightChange = { hourHeight = it },
                                onAnchorDateChange = viewModel::setAnchorDate,
                                onClassClick = { schedule, date ->
                                    viewModel.showViewSheet(schedule, date)
                                },
                                onClassLongClick = { schedule ->
                                    viewModel.showEditSheet(schedule)
                                },
                                onEmptySlotClick = { date, time ->
                                    viewModel.showAddSheetForSlot(date, time)
                                },
                                currentClassId = currentClass?.schedule?.id,
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
    viewingSchedule?.let { viewed ->
        val schedule = viewed.schedule
        ViewClassSheet(
            scheduleWithSubject = schedule,
            classDate = viewed.date,
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

/** "Monday 7 September" — the empty agenda page names the day it is about. */
private fun dayLabelOf(date: LocalDate): String {
    val day = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
    val month = date.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
    return "$day ${date.dayOfMonth} $month"
}
