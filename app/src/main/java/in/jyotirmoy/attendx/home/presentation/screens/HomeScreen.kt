package `in`.jyotirmoy.attendx.home.presentation.screens

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.Text
import androidx.compose.material3.animateFloatingActionButton
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.jyotirmoy.attendx.R
import `in`.jyotirmoy.attendx.core.common.LocalSettings
import `in`.jyotirmoy.attendx.core.common.LocalWeakHaptic
import `in`.jyotirmoy.attendx.core.domain.model.SubjectAttendance
import `in`.jyotirmoy.attendx.core.domain.model.TotalAttendance
import `in`.jyotirmoy.attendx.core.presentation.components.dialog.NotificationPermDialog
import `in`.jyotirmoy.attendx.core.presentation.components.progress.AnimatedHalfCircleProgress
import `in`.jyotirmoy.attendx.notification.isNotificationPermissionGranted
import `in`.jyotirmoy.attendx.home.presentation.components.card.SubjectCard
import `in`.jyotirmoy.attendx.home.presentation.components.dialog.AddSubjectDialog
import `in`.jyotirmoy.attendx.home.presentation.components.drawer.SmartAttendanceDrawer
import `in`.jyotirmoy.attendx.home.presentation.components.histogram.AttendanceHistogramCard
import `in`.jyotirmoy.attendx.home.presentation.components.histogram.SubjectHistogramData
import `in`.jyotirmoy.attendx.home.presentation.components.image.UndrawRelaxedReading
import `in`.jyotirmoy.attendx.home.presentation.components.sheet.EditChartNameBottomSheet
import `in`.jyotirmoy.attendx.home.presentation.components.label.Label
import `in`.jyotirmoy.attendx.core.data.model.SubjectEntity
import `in`.jyotirmoy.attendx.home.presentation.viewmodel.HomeViewModel
import `in`.jyotirmoy.attendx.navigation.CalendarScreen
import `in`.jyotirmoy.attendx.navigation.LocalNavController
import `in`.jyotirmoy.attendx.navigation.SettingsScreen
import `in`.jyotirmoy.attendx.settings.data.local.SettingsKeys
import `in`.jyotirmoy.attendx.settings.presentation.event.SettingsUiEvent
import `in`.jyotirmoy.attendx.settings.presentation.viewmodel.SettingsViewModel
import `in`.jyotirmoy.attendx.settings.presentation.page.autoupdate.viewmodel.AutoUpdateViewModel
import `in`.jyotirmoy.attendx.core.presentation.components.bottomsheet.UpdateBottomSheet
import kotlinx.coroutines.launch
import androidx.compose.foundation.text.ClickableText
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import `in`.jyotirmoy.attendx.navigation.MarketplaceScreen
import androidx.compose.material3.LocalContentColor
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@SuppressLint("DefaultLocale")
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    autoUpdateViewModel: AutoUpdateViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val weakHaptic = LocalWeakHaptic.current
    val navController = LocalNavController.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var importTextLayoutCoordinates by remember { mutableStateOf<androidx.compose.ui.layout.LayoutCoordinates?>(null) }
    var rootLayoutCoordinates by remember { mutableStateOf<androidx.compose.ui.layout.LayoutCoordinates?>(null) }
    
    val subjects by viewModel.subjectList.collectAsState()
    val subjectCount by viewModel.subjectCount.collectAsState(initial = 0)
    var isDialogOpen by rememberSaveable { mutableStateOf(false) }
    val totalAttendance by viewModel.getTotalAttendanceCounts()
        .collectAsState(initial = TotalAttendance())
    val totalPresent = totalAttendance.totalPresent
    val totalAbsent = totalAttendance.totalAbsent
    val totalCount = totalAttendance.totalCount
    val totalProgress = totalPresent.toFloat() / totalCount.toFloat()
    val totalProgressText = "${String.format("%.0f", totalProgress * 100)}%"



    val progressColor = lerp(
        start = MaterialTheme.colorScheme.error,
        stop = MaterialTheme.colorScheme.primary,
        fraction = totalProgress.coerceIn(0f, 1f)
    )

    val subjectCardCornerRadius = LocalSettings.current.subjectCardCornerRadius
    var selectedCardsCount by rememberSaveable { mutableIntStateOf(0) }
    var showNotificationPermissionDialog by rememberSaveable { mutableStateOf(false) }
    var editingSubject by remember { mutableStateOf<SubjectEntity?>(null) }
    
    // Update state for persistent update button
    val isUpdateAvailable by autoUpdateViewModel.isUpdateAvailable.collectAsState()
    val latestVersion by autoUpdateViewModel.latestVersion.collectAsState()
    val releaseNotes by autoUpdateViewModel.releaseNotes.collectAsState()
    val apkUrl by autoUpdateViewModel.apkUrl.collectAsState()
    var showUpdateSheet by rememberSaveable { mutableStateOf(false) }
    
    // Track scroll state for FAB visibility
    val listState = rememberLazyListState()
    val isExpanded by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 && 
            listState.firstVisibleItemScrollOffset == 0
        }
    }
    // Interaction source removed
    val notificationsEnabled by rememberSaveable {
        mutableStateOf(
            isNotificationPermissionGranted(
                context
            )
        )
    }

    val notificationPreference = LocalSettings.current.notificationPreference

    val launcherReqPerm = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->

            settingsViewModel.setBoolean(
                key = SettingsKeys.ENABLE_NOTIFICATIONS,
                value = isGranted || notificationPreference
            )

            settingsViewModel.refreshNotificationPermissionState()
        }
    )

    val launcherIntent = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = {
            val isGranted = isNotificationPermissionGranted(context)

            settingsViewModel.setBoolean(
                key = SettingsKeys.ENABLE_NOTIFICATIONS,
                value = isGranted || notificationPreference
            )

            settingsViewModel.refreshNotificationPermissionState()
        }
    )

    val notificationPermDialogShown = LocalSettings.current.notificationPermissionDialogShown

    LaunchedEffect(notificationsEnabled, notificationPermDialogShown, totalCount) {
        showNotificationPermissionDialog =
            !notificationsEnabled && !notificationPermDialogShown && totalCount != 0
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is SettingsUiEvent.RequestPermission -> launcherReqPerm.launch(event.permission)

                is SettingsUiEvent.LaunchIntent -> launcherIntent.launch(event.intent)

                else -> {}
            }
        }
    }

    BackHandler(enabled = selectedCardsCount > 0) { selectedCardsCount = 0 }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            SmartAttendanceDrawer(
                onSettingsClick = {
                    scope.launch { drawerState.close() }
                    navController.navigate(SettingsScreen)
                    weakHaptic()
                }
            )
        }
    ) {
        Scaffold(
        modifier = modifier.fillMaxSize(),
    ) { innerPadding ->

        Box(modifier = Modifier.fillMaxSize().onGloballyPositioned { rootLayoutCoordinates = it }) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(5.dp), // Changed from 15.dp to 5.dp for grouped appearance
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding() + 85.dp, // Space for button
                    start = 0.dp,
                    end = 0.dp
                ),
            ) {
                item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 25.dp, end = 25.dp, top = 35.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.displaySmallEmphasized.copy(
                            fontWeight = FontWeight.ExtraBold
                        ),
                        modifier = Modifier.alpha(0.95f)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Persistent update button - visible when update available
                    AnimatedVisibility(
                        visible = isUpdateAvailable,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
                        exit = fadeOut() + slideOutVertically(targetOffsetY = { -it })
                    ) {
                        Surface(
                            onClick = {
                                showUpdateSheet = true
                                weakHaptic()
                            },
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.tertiary,
                            contentColor = MaterialTheme.colorScheme.onTertiary,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.SystemUpdate,
                                    contentDescription = "Update Available",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    if (isUpdateAvailable) {
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Surface(
                        onClick = {
                            scope.launch { drawerState.open() }
                            weakHaptic()
                        },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Open Settings",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            if (subjectCount == 0 || totalCount == 0) {
                item {
                    Box(
                        modifier
                            .fillMaxWidth()
                            .padding(horizontal = 25.dp)
                            .then(
                                if (subjectCount == 0) Modifier.height(400.dp)
                                else Modifier.padding(vertical = 20.dp)
                            ), contentAlignment = Alignment.Center
                    ) {
                        UndrawRelaxedReading()
                    }
                }
            }

            if (subjectCount == 0) {
                item {


// ... inside the composable, replacing the Text ...

                    val noSubjectText = stringResource(R.string.no_subject_yet)
                    val importText = stringResource(R.string.import_template)
                    
                    val annotatedString = buildAnnotatedString {
                        append(noSubjectText)
                        append(" ")
                        pushStringAnnotation(tag = "IMPORT", annotation = "import")
                        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline, fontWeight = FontWeight.Bold)) {
                            append(importText)
                        }
                        pop()
                    }

                    ClickableText(
                        text = annotatedString,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            textAlign = TextAlign.Center, 
                            color = LocalContentColor.current.copy(alpha = 0.75f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 25.dp)
                            .onGloballyPositioned { importTextLayoutCoordinates = it },
                        onClick = { offset ->
                            annotatedString.getStringAnnotations(tag = "IMPORT", start = offset, end = offset)
                                .firstOrNull()?.let {
                                    val rootHeight = rootLayoutCoordinates?.size?.height?.toFloat() ?: 1f
                                    val textPosition = importTextLayoutCoordinates?.positionInRoot()
                                    val textHeight = importTextLayoutCoordinates?.size?.height?.toFloat() ?: 0f
                                    val textWidth = importTextLayoutCoordinates?.size?.width?.toFloat() ?: 0f
                                    
                                    // Calculate pivot roughly at center of the text block manually since we don't have exact click coordinates relative to root easily without pointer input
                                    // A safe bet is vertically centered on the text, horizontally centered (0.5f)
                                    
                                    val pivotY = if (rootHeight > 0) {
                                        ((textPosition?.y ?: 0f) + textHeight / 2) / rootHeight
                                    } else 0.5f
                                    
                                    navController.navigate(MarketplaceScreen(pivotX = 0.5f, pivotY = pivotY))
                                }
                        }
                    )
                }
            }

            if (subjectCount != 0 && totalCount != 0) {
                item {
                    // Prepare histogram data
                    val histogramData = subjects.mapNotNull { subject ->
                        val counts = viewModel.getSubjectAttendanceCounts(subject.id)
                            .collectAsState(initial = SubjectAttendance()).value
                        
                        if (counts.totalCount > 0) {
                            val percentage = (counts.presentCount.toFloat() / counts.totalCount.toFloat()) * 100
                            SubjectHistogramData(
                                subject = subject,
                                percentage = percentage
                            )
                        } else null
                    }

                    AttendanceHistogramCard(
                        modifier = Modifier.padding(top = 35.dp, bottom = 20.dp),
                        histogramData = histogramData,
                        onBarClick = { subject ->
                            editingSubject = subject
                        }
                    )
                }
            }

            items(subjects.size, key = { index -> subjects[index].id }) { index ->

                val counts by viewModel.getSubjectAttendanceCounts(subjects[index].id)
                    .collectAsState(initial = SubjectAttendance())

                val progress = counts.presentCount.toFloat() / counts.totalCount.toFloat()
                
                // Calculate grouped card corner radius
                val isFirst = index == 0
                val isLast = index == subjects.size - 1
                val isOnly = subjects.size == 1
                
                val cornerRadius = when {
                    isOnly -> 25.dp // Single card - all corners rounded
                    isFirst -> 25.dp // First card - will use custom shape
                    isLast -> 25.dp // Last card - will use custom shape
                    else -> 10.dp // Middle cards - small corners
                }
                
                // Determine the shape based on position
                val cardShape = when {
                    isOnly -> RoundedCornerShape(25.dp)
                    isFirst -> RoundedCornerShape(
                        topStart = 25.dp,
                        topEnd = 25.dp,
                        bottomStart = 10.dp,
                        bottomEnd = 10.dp
                    )
                    isLast -> RoundedCornerShape(
                        topStart = 10.dp,
                        topEnd = 10.dp,
                        bottomStart = 25.dp,
                        bottomEnd = 25.dp
                    )
                    else -> RoundedCornerShape(10.dp)
                }

                SubjectCard(
                    modifier = Modifier
                        .padding(horizontal = 15.dp)
                        .animateItem(),
                    cardStyle = LocalSettings.current.subjectCardStyle,
                    cornerRadius = cornerRadius,
                    customShape = cardShape,
                    subjectId = subjects[index].id,
                    subject = subjects[index].subject,
                    subjectCode = subjects[index].subjectCode,
                    progress = progress,
                    isTotalCountZero = counts.totalCount == 0,
                    selectedCardsCount = selectedCardsCount,
                    navigate = {
                        navController.navigate(
                            CalendarScreen(
                                subjectId = subjects[index].id,
                                subject = subjects[index].subject
                            )
                        )
                    },
                    onLongClicked = { isLongClicked ->
                        if (isLongClicked) selectedCardsCount++ else selectedCardsCount--
                    },
                )
            }

            item {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(25.dp)
                )
            }
        }

        // Wide pill-shaped button at bottom
        // Material 3 Expressive FAB
        AnimatedVisibility(
            visible = selectedCardsCount == 0 && !isDialogOpen, // Hide when selecting cards or dialog is open (container transform simulation)
            enter = androidx.compose.animation.scaleIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) + fadeIn(animationSpec = tween(400)),
            exit = androidx.compose.animation.scaleOut(
                animationSpec = tween(200)
            ) + fadeOut(animationSpec = tween(200)),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 25.dp, bottom = 25.dp)
        ) {
            
            ExtendedFloatingActionButton(
                onClick = {
                    isDialogOpen = true
                    weakHaptic()
                },
                expanded = isExpanded,
                icon = { Icon(Icons.Rounded.Add, "Add Subject") },
                text = { Text(text = stringResource(R.string.add_subject)) },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }



    if (isDialogOpen) {
        AddSubjectDialog(
            onDismiss = {
                isDialogOpen = false
            })
    }

    if (editingSubject != null) {
        EditChartNameBottomSheet(
            subject = editingSubject!!,
            onDismissRequest = { editingSubject = null },
            onSave = { newLabel ->
                viewModel.updateSubjectLabel(
                    subjectId = editingSubject!!.id,
                    newLabel = newLabel,
                    currentName = editingSubject!!.subject,
                    currentCode = editingSubject!!.subjectCode
                )
                editingSubject = null
            }
        )
    }

    if (showNotificationPermissionDialog) {
        NotificationPermDialog(
            onDismiss = {
                showNotificationPermissionDialog = false
                settingsViewModel.setBoolean(
                    SettingsKeys.NOTIFICATION_PERMISSION_DIALOG_SHOWN,
                    true
                )
            },
            onConfirm = {
                viewModel.requestNotificationPermission()
                showNotificationPermissionDialog = false
                settingsViewModel.setBoolean(
                    SettingsKeys.NOTIFICATION_PERMISSION_DIALOG_SHOWN,
                    true
                )
            })
    }

    // Update sheet triggered from header button
    if (showUpdateSheet) {
        UpdateBottomSheet(
            onDismiss = { showUpdateSheet = false },
            latestVersion = latestVersion,
            releaseNotes = releaseNotes,
            apkUrl = apkUrl,
            viewModel = autoUpdateViewModel
        )
    }
    }
}
}
