package `in`.jyotirmoy.attendx.tools.presentation.pomodoro.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilledTonalIconToggleButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import `in`.jyotirmoy.attendx.R
import `in`.jyotirmoy.attendx.tools.data.model.Topic
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.components.TOPIC_SHAPES
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.model.SettingsAction
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.model.SettingsState
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.settings.SettingsViewModel
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.settings.backup.BackupBottomSheet
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.settings.backup.BackupRestoreState
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.settings.backup.RestoreBottomSheet
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.settings.backup.restartApp
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.settings.components.MinuteInputField
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.settings.components.MinutesInputTransformation2Digits
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.settings.components.MinutesInputTransformation3Digits
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.settings.components.ResetDataDialog
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.settings.components.SYSTEM_DEFAULT_AMPLITUDE
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.settings.components.SettingsSwitchItem
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.settings.components.SliderListItem
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.settings.components.rememberPlatformVibrator
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.settings.components.rememberRingtoneNameProviderCallback
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.settings.components.rememberRingtonePickerLauncherCallback
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.theme.NunitoSansFontFamily
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.theme.PomodoroColors.detailPaneTopBarColors
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.theme.PomodoroColors.listItemColors
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.theme.PomodoroColors.switchColors
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.theme.PomodoroColors.topBarColors
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.theme.PomodoroSegmentedListItem
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.theme.PomodoroShapeDefaults.PANE_MAX_WIDTH
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.theme.PomodoroShapeDefaults.bottomListItemShape
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.theme.PomodoroShapeDefaults.cardShape
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.theme.PomodoroShapeDefaults.getListItemShape
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.theme.PomodoroShapeDefaults.middleListItemShape
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.theme.PomodoroShapeDefaults.topListItemShape
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.util.millisecondsToHoursMinutes
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.math.roundToLong

enum class SettingsSubScreen {
    MAIN,
    TIMER,
    TOPICS,
    ALARM,
    BACKUP_RESTORE
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PomodoroSettingsScreen(
    contentPadding: PaddingValues,
    viewModel: SettingsViewModel,
    topics: List<Topic> = emptyList(),
    selectedTopic: Topic = Topic(name = "Default"),
    onSelectTopic: (Topic) -> Unit = {},
    onCreateTopic: () -> Unit = {},
    onEditTopic: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var currentSubScreen by remember { mutableStateOf(SettingsSubScreen.MAIN) }
    val settingsState by viewModel.settingsState.collectAsStateWithLifecycle()
    val isShowingEraseDialog by viewModel.isShowingEraseDialog.collectAsStateWithLifecycle()

    BackHandler(enabled = currentSubScreen != SettingsSubScreen.MAIN) {
        currentSubScreen = SettingsSubScreen.MAIN
    }

    if (isShowingEraseDialog) {
        ResetDataDialog(
            resetData = { viewModel.onAction(SettingsAction.EraseData) },
            onDismiss = { viewModel.onAction(SettingsAction.CancelEraseData) }
        )
    }

    AnimatedContent(
        targetState = currentSubScreen,
        transitionSpec = {
            if (targetState != SettingsSubScreen.MAIN) {
                (slideInHorizontally(initialOffsetX = { it }) + fadeIn())
                    .togetherWith(slideOutHorizontally(targetOffsetX = { -it / 4 }) + fadeOut())
            } else {
                (slideInHorizontally(initialOffsetX = { -it / 4 }) + fadeIn())
                    .togetherWith(slideOutHorizontally(targetOffsetX = { it }) + fadeOut())
            }
        },
        label = "settingsNavTransition",
        modifier = modifier
    ) { subScreen ->
        when (subScreen) {
            SettingsSubScreen.MAIN -> {
                SettingsMainView(
                    contentPadding = contentPadding,
                    onNavigate = { currentSubScreen = it },
                    onAskEraseData = { viewModel.onAction(SettingsAction.AskEraseData) }
                )
            }
            SettingsSubScreen.TIMER -> {
                TimerSettingsView(
                    settingsState = settingsState,
                    contentPadding = contentPadding,
                    topics = topics,
                    selectedTopic = selectedTopic,
                    onSelectTopic = onSelectTopic,
                    onCreateTopic = onCreateTopic,
                    onEditTopic = onEditTopic,
                    onAction = viewModel::onAction,
                    onBack = { currentSubScreen = SettingsSubScreen.MAIN }
                )
            }
            SettingsSubScreen.TOPICS -> {
                TopicsSettingsView(
                    contentPadding = contentPadding,
                    topics = topics,
                    selectedTopic = selectedTopic,
                    onSelectTopic = onSelectTopic,
                    onCreateTopic = onCreateTopic,
                    onEditTopic = onEditTopic,
                    onBack = { currentSubScreen = SettingsSubScreen.MAIN }
                )
            }
            SettingsSubScreen.ALARM -> {
                AlarmSettingsView(
                    settingsState = settingsState,
                    contentPadding = contentPadding,
                    onAction = viewModel::onAction,
                    onBack = { currentSubScreen = SettingsSubScreen.MAIN }
                )
            }
            SettingsSubScreen.BACKUP_RESTORE -> {
                BackupRestoreSettingsView(
                    contentPadding = contentPadding,
                    onBack = { currentSubScreen = SettingsSubScreen.MAIN }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsMainView(
    contentPadding: PaddingValues,
    onNavigate: (SettingsSubScreen) -> Unit,
    onAskEraseData: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.settings),
                        style = typography.headlineMedium.copy(
                            fontFamily = NunitoSansFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp,
                            lineHeight = 32.sp
                        )
                    )
                },
                colors = topBarColors,
                scrollBehavior = scrollBehavior
            )
        },
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding(),
                bottom = contentPadding.calculateBottomPadding() + 24.dp,
                start = 16.dp,
                end = 16.dp
            ),
            modifier = Modifier
                .background(topBarColors.containerColor)
                .fillMaxSize()
        ) {
            item { Spacer(Modifier.height(14.dp)) }

            // Group 1: Timer, Topics, Alarm
            item {
                PomodoroSegmentedListItem(
                    leadingContent = {
                        Icon(painterResource(R.drawable.clocks), null)
                    },
                    supportingContent = {
                        Text(
                            stringResource(R.string.durations) + ", " +
                                    stringResource(R.string.dnd) + ", " +
                                    stringResource(R.string.always_on_display),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    trailingContent = {
                        Icon(painterResource(R.drawable.arrow_forward_big), null)
                    },
                    shape = getListItemShape(0, 3),
                    onClick = { onNavigate(SettingsSubScreen.TIMER) }
                ) { Text(stringResource(R.string.timer)) }
            }

            item {
                PomodoroSegmentedListItem(
                    leadingContent = {
                        Icon(painterResource(R.drawable.folder), null)
                    },
                    supportingContent = {
                        Text(
                            stringResource(R.string.topics_desc),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    trailingContent = {
                        Icon(painterResource(R.drawable.arrow_forward_big), null)
                    },
                    shape = getListItemShape(1, 3),
                    onClick = { onNavigate(SettingsSubScreen.TOPICS) }
                ) { Text(stringResource(R.string.topics)) }
            }

            item {
                PomodoroSegmentedListItem(
                    leadingContent = {
                        Icon(painterResource(R.drawable.alarm), null)
                    },
                    supportingContent = {
                        Text(
                            stringResource(R.string.alarm_sound) + ", " +
                                    stringResource(R.string.sound) + ", " +
                                    stringResource(R.string.vibrate) + ", " +
                                    stringResource(R.string.media_volume_for_alarm),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    trailingContent = {
                        Icon(painterResource(R.drawable.arrow_forward_big), null)
                    },
                    shape = getListItemShape(2, 3),
                    onClick = { onNavigate(SettingsSubScreen.ALARM) }
                ) { Text(stringResource(R.string.alarm)) }
            }

            item { Spacer(Modifier.height(12.dp)) }

            // Group 2: Backup and restore
            item {
                PomodoroSegmentedListItem(
                    leadingContent = {
                        Icon(painterResource(R.drawable.backup), null)
                    },
                    supportingContent = {
                        Text(
                            stringResource(R.string.backup) + ", " + stringResource(R.string.restore),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    trailingContent = {
                        Icon(painterResource(R.drawable.arrow_forward_big), null)
                    },
                    shape = getListItemShape(0, 1),
                    onClick = { onNavigate(SettingsSubScreen.BACKUP_RESTORE) }
                ) { Text(stringResource(R.string.backup_and_restore)) }
            }

            item { Spacer(Modifier.height(12.dp)) }

            // Reset Data
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    TextButton(
                        onClick = onAskEraseData,
                    ) {
                        Text(stringResource(R.string.reset_data))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TopicsSettingsView(
    contentPadding: PaddingValues,
    topics: List<Topic>,
    selectedTopic: Topic,
    onSelectTopic: (Topic) -> Unit,
    onCreateTopic: () -> Unit,
    onEditTopic: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(detailPaneTopBarColors.containerColor)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                stringResource(R.string.topics),
                                style = typography.headlineSmall.copy(
                                    fontFamily = NunitoSansFontFamily,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                stringResource(R.string.settings),
                                style = typography.bodySmall,
                                color = colorScheme.outline
                            )
                        }
                    },
                    navigationIcon = {
                        FilledTonalIconButton(
                            onClick = onBack,
                            shapes = IconButtonDefaults.shapes(),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = listItemColors.containerColor
                            ),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Icon(
                                painterResource(R.drawable.arrow_back),
                                contentDescription = stringResource(R.string.back)
                            )
                        }
                    },
                    colors = detailPaneTopBarColors,
                    scrollBehavior = scrollBehavior
                )
            },
            containerColor = detailPaneTopBarColors.containerColor,
            modifier = modifier
                .widthIn(max = PANE_MAX_WIDTH)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        ) { innerPadding ->
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding(),
                    bottom = contentPadding.calculateBottomPadding() + 24.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
                modifier = Modifier.fillMaxSize()
            ) {
                // Info tip row — same as Tomato's timer info row
                item {
                    Column {
                        Spacer(Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                painterResource(R.drawable.info),
                                contentDescription = null,
                                tint = colorScheme.onSurfaceVariant
                            )
                            Text(
                                stringResource(R.string.topics_desc),
                                style = typography.bodyMedium,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                }

                // Create new topic — standalone card
                item {
                    ListItem(
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null
                            )
                        },
                        headlineContent = { Text(stringResource(R.string.create_new_topic)) },
                        colors = listItemColors,
                        modifier = Modifier
                            .clip(cardShape)
                            .clickable(onClick = onCreateTopic)
                    )
                }

                item { Spacer(Modifier.height(12.dp)) }

                // Topic list — segmented list items
                if (topics.isNotEmpty()) {
                    itemsIndexed(topics) { index, topic ->
                        val isSelected = topic.id == selectedTopic.id
                        val topicColor = remember(topic.colorHex) {
                            try {
                                Color(android.graphics.Color.parseColor(topic.colorHex))
                            } catch (_: Exception) {
                                Color(0xFF3B82F6)
                            }
                        }
                        val shapeIndex = topic.shapeIndex.coerceIn(0, TOPIC_SHAPES.size - 1)
                        val badgeShape = TOPIC_SHAPES[shapeIndex]()

                        ListItem(
                            leadingContent = {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(badgeShape)
                                        .background(topicColor)
                                )
                            },
                            headlineContent = {
                                Text(
                                    topic.name,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            supportingContent = {
                                Text(
                                    "${topic.focusTime / 60000}m / ${topic.shortBreakTime / 60000}m / ${topic.longBreakTime / 60000}m"
                                )
                            },
                            trailingContent = {
                                if (isSelected) {
                                    Icon(
                                        painterResource(R.drawable.check),
                                        contentDescription = null,
                                        tint = colorScheme.primary
                                    )
                                } else {
                                    FilledTonalIconButton(
                                        onClick = {
                                            onSelectTopic(topic)
                                            onEditTopic()
                                        },
                                        modifier = Modifier.size(36.dp),
                                        shapes = IconButtonDefaults.shapes()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            },
                            colors = if (isSelected) ListItemDefaults.colors(
                                containerColor = colorScheme.primaryContainer
                            ) else listItemColors,
                            modifier = Modifier
                                .clip(getListItemShape(index, topics.size))
                                .clickable {
                                    if (isSelected) {
                                        onEditTopic()
                                    } else {
                                        onSelectTopic(topic)
                                    }
                                }
                        )
                    }
                }

                item { Spacer(Modifier.height(12.dp)) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TimerSettingsView(
    settingsState: SettingsState,
    contentPadding: PaddingValues,
    topics: List<Topic>,
    selectedTopic: Topic,
    onSelectTopic: (Topic) -> Unit,
    onCreateTopic: () -> Unit,
    onEditTopic: () -> Unit,
    onAction: (SettingsAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val focusTimeInputFieldState = rememberTextFieldState(
        (settingsState.focusTime / 60000L).toString()
    )
    val shortBreakTimeInputFieldState = rememberTextFieldState(
        (settingsState.shortBreakTime / 60000L).toString()
    )
    val longBreakTimeInputFieldState = rememberTextFieldState(
        (settingsState.longBreakTime / 60000L).toString()
    )
    val sessionsSliderState = rememberSliderState(
        value = settingsState.sessionLength.toFloat().coerceIn(1f, 10f),
        valueRange = 1f..10f,
        steps = 8
    )

    // Sync input states when external settings change
    LaunchedEffect(settingsState.focusTime) {
        val mins = (settingsState.focusTime / 60000L).toString()
        if (focusTimeInputFieldState.text.toString() != mins) {
            focusTimeInputFieldState.setTextAndPlaceCursorAtEnd(mins)
        }
    }
    LaunchedEffect(settingsState.shortBreakTime) {
        val mins = (settingsState.shortBreakTime / 60000L).toString()
        if (shortBreakTimeInputFieldState.text.toString() != mins) {
            shortBreakTimeInputFieldState.setTextAndPlaceCursorAtEnd(mins)
        }
    }
    LaunchedEffect(settingsState.longBreakTime) {
        val mins = (settingsState.longBreakTime / 60000L).toString()
        if (longBreakTimeInputFieldState.text.toString() != mins) {
            longBreakTimeInputFieldState.setTextAndPlaceCursorAtEnd(mins)
        }
    }

    // Auto save text changes with debounce
    LaunchedEffect(Unit) {
        snapshotFlow { focusTimeInputFieldState.text.toString() }
            .distinctUntilChanged()
            .collect { txt ->
                val mins = txt.toLongOrNull()
                if (mins != null && mins > 0) {
                    onAction(SettingsAction.SaveFocusTime(mins * 60000L))
                }
            }
    }
    LaunchedEffect(Unit) {
        snapshotFlow { shortBreakTimeInputFieldState.text.toString() }
            .distinctUntilChanged()
            .collect { txt ->
                val mins = txt.toLongOrNull()
                if (mins != null && mins > 0) {
                    onAction(SettingsAction.SaveShortBreakTime(mins * 60000L))
                }
            }
    }
    LaunchedEffect(Unit) {
        snapshotFlow { longBreakTimeInputFieldState.text.toString() }
            .distinctUntilChanged()
            .collect { txt ->
                val mins = txt.toLongOrNull()
                if (mins != null && mins > 0) {
                    onAction(SettingsAction.SaveLongBreakTime(mins * 60000L))
                }
            }
    }
    LaunchedEffect(Unit) {
        snapshotFlow { sessionsSliderState.value.roundToInt() }
            .distinctUntilChanged()
            .collect { length ->
                if (length != settingsState.sessionLength) {
                    onAction(SettingsAction.SaveSessionLength(length))
                }
            }
    }

    val switchItems = remember(
        settingsState.dndEnabled,
        settingsState.aodEnabled,
        settingsState.autostartNextSession,
        settingsState.secureAod,
        settingsState.singleProgressBar
    ) {
        listOf(
            listOf(
                SettingsSwitchItem(
                    checked = settingsState.autostartNextSession,
                    icon = R.drawable.autoplay,
                    label = R.string.auto_start_next_timer,
                    description = R.string.auto_start_next_timer_desc,
                    onClick = { onAction(SettingsAction.SaveAutostartNextSession(it)) }
                ),
                SettingsSwitchItem(
                    checked = settingsState.dndEnabled,
                    icon = R.drawable.dnd,
                    label = R.string.dnd,
                    description = R.string.dnd_desc,
                    onClick = { onAction(SettingsAction.SaveDndEnabled(it)) }
                )
            ),
            listOf(
                SettingsSwitchItem(
                    checked = settingsState.aodEnabled,
                    icon = R.drawable.aod,
                    label = R.string.always_on_display,
                    description = R.string.always_on_display_desc,
                    onClick = { onAction(SettingsAction.SaveAodEnabled(it)) }
                ),
                SettingsSwitchItem(
                    checked = settingsState.secureAod,
                    enabled = settingsState.aodEnabled,
                    icon = R.drawable.mobile_lock_portrait,
                    label = R.string.secure_aod,
                    description = R.string.secure_aod_desc,
                    onClick = { onAction(SettingsAction.SaveSecureAod(it)) }
                )
            )
        )
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(detailPaneTopBarColors.containerColor)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                stringResource(R.string.timer),
                                style = typography.headlineSmall.copy(
                                    fontFamily = NunitoSansFontFamily,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                stringResource(R.string.settings),
                                style = typography.bodySmall,
                                color = colorScheme.outline
                            )
                        }
                    },
                    navigationIcon = {
                        FilledTonalIconButton(
                            onClick = onBack,
                            shapes = IconButtonDefaults.shapes(),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = listItemColors.containerColor
                            ),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Icon(
                                painterResource(R.drawable.arrow_back),
                                contentDescription = stringResource(R.string.back)
                            )
                        }
                    },
                    colors = detailPaneTopBarColors,
                    scrollBehavior = scrollBehavior
                )
            },
            containerColor = detailPaneTopBarColors.containerColor,
            modifier = modifier
                .widthIn(max = PANE_MAX_WIDTH)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        ) { innerPadding ->
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding(),
                    bottom = contentPadding.calculateBottomPadding() + 24.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
                modifier = Modifier.fillMaxSize()
            ) {
                // Info banner tip
                item {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Icon(
                            painterResource(R.drawable.info),
                            contentDescription = null,
                            tint = colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            stringResource(R.string.settings_infinite_focus_tip),
                            style = typography.bodyMedium,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                }

                // Topic Selector Row
                if (topics.isNotEmpty()) {
                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(bottom = 8.dp)
                        ) {
                            topics.forEach { topic ->
                                val isSelected = topic.id == selectedTopic.id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onSelectTopic(topic) },
                                    label = {
                                        Text(
                                            topic.name,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = colorScheme.primaryContainer,
                                        selectedLabelColor = colorScheme.onPrimaryContainer
                                    )
                                )
                            }

                            FilledTonalIconButton(
                                onClick = onCreateTopic,
                                modifier = Modifier.size(32.dp),
                                shapes = IconButtonDefaults.shapes()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Topic",
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            FilledTonalIconButton(
                                onClick = onEditTopic,
                                modifier = Modifier.size(32.dp),
                                shapes = IconButtonDefaults.shapes()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Topic",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // 3 Giant Minute Input Fields
                item {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                stringResource(R.string.focus),
                                style = typography.titleSmall
                            )
                            MinuteInputField(
                                state = focusTimeInputFieldState,
                                enabled = true,
                                shape = RoundedCornerShape(
                                    topStart = topListItemShape.topStart,
                                    bottomStart = topListItemShape.topStart,
                                    topEnd = topListItemShape.bottomStart,
                                    bottomEnd = topListItemShape.bottomStart
                                ),
                                inputTransformation = MinutesInputTransformation3Digits,
                                imeAction = ImeAction.Next
                            )
                        }
                        Spacer(Modifier.width(2.dp))
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                stringResource(R.string.short_break),
                                style = typography.titleSmall
                            )
                            MinuteInputField(
                                state = shortBreakTimeInputFieldState,
                                enabled = true,
                                shape = RoundedCornerShape(middleListItemShape.topStart),
                                inputTransformation = MinutesInputTransformation2Digits,
                                imeAction = ImeAction.Next
                            )
                        }
                        Spacer(Modifier.width(2.dp))
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                stringResource(R.string.long_break),
                                style = typography.titleSmall
                            )
                            MinuteInputField(
                                state = longBreakTimeInputFieldState,
                                enabled = true,
                                shape = RoundedCornerShape(
                                    topStart = bottomListItemShape.topStart,
                                    bottomStart = bottomListItemShape.topStart,
                                    topEnd = bottomListItemShape.bottomStart,
                                    bottomEnd = bottomListItemShape.bottomStart
                                ),
                                inputTransformation = MinutesInputTransformation3Digits,
                                imeAction = ImeAction.Done
                            )
                        }
                    }
                }

                item { Spacer(Modifier.height(12.dp)) }

                // Session Length Card
                item {
                    Column(Modifier.background(listItemColors.containerColor, topListItemShape)) {
                        ListItem(
                            leadingContent = {
                                Icon(painterResource(R.drawable.clocks), null)
                            },
                            headlineContent = {
                                Text(stringResource(R.string.session_length))
                            },
                            supportingContent = {
                                Text(
                                    stringResource(
                                        R.string.session_length_desc,
                                        sessionsSliderState.value.toInt()
                                    )
                                )
                            },
                            colors = listItemColors,
                            modifier = Modifier.clip(cardShape)
                        )
                        Slider(
                            state = sessionsSliderState,
                            modifier = Modifier
                                .padding(start = (16 * 2 + 24).dp, end = 16.dp, bottom = 12.dp)
                        )
                    }
                }

                // Daily Focus Goal
                item {
                    val hmf = stringResource(R.string.hours_and_minutes_format)
                    SliderListItem(
                        value = settingsState.focusGoal.toFloat(),
                        valueRange = 0f..16 * 60 * 60 * 1000f,
                        enabled = true,
                        label = stringResource(R.string.daily_focus_goal),
                        trailingLabel = { millisecondsToHoursMinutes(it.toLong(), hmf) },
                        icon = { Icon(painterResource(R.drawable.flag), null) },
                        shape = bottomListItemShape
                    ) {
                        val goal = it.toLong()
                        onAction(SettingsAction.SaveFocusGoal(goal - (goal % (30 * 60 * 1000))))
                    }
                }

                item { Spacer(Modifier.height(12.dp)) }

                // Switch Items (Autoplay, DND)
                itemsIndexed(switchItems[0]) { index, item ->
                    ListItem(
                        leadingContent = {
                            Icon(
                                painterResource(item.icon),
                                contentDescription = null,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        },
                        headlineContent = { Text(stringResource(item.label)) },
                        supportingContent = { Text(stringResource(item.description)) },
                        trailingContent = {
                            Switch(
                                checked = item.checked,
                                enabled = item.enabled,
                                onCheckedChange = { item.onClick(it) },
                                thumbContent = {
                                    Icon(
                                        painter = painterResource(if (item.checked) R.drawable.check else R.drawable.clear),
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize)
                                    )
                                },
                                colors = switchColors
                            )
                        },
                        colors = listItemColors,
                        modifier = Modifier.clip(
                            when (index) {
                                0 -> topListItemShape
                                switchItems[0].size - 1 -> bottomListItemShape
                                else -> middleListItemShape
                            }
                        )
                    )
                }

                item { Spacer(Modifier.height(12.dp)) }

                // Switch Items (AOD, Secure AOD)
                itemsIndexed(switchItems[1]) { index, item ->
                    ListItem(
                        leadingContent = {
                            Icon(
                                painterResource(item.icon),
                                contentDescription = null,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        },
                        headlineContent = { Text(stringResource(item.label)) },
                        supportingContent = { Text(stringResource(item.description)) },
                        trailingContent = {
                            Switch(
                                checked = item.checked,
                                onCheckedChange = { item.onClick(it) },
                                enabled = item.enabled,
                                thumbContent = {
                                    Icon(
                                        painter = painterResource(if (item.checked) R.drawable.check else R.drawable.clear),
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize)
                                    )
                                },
                                colors = switchColors
                            )
                        },
                        colors = listItemColors,
                        modifier = Modifier.clip(
                            when (index) {
                                0 -> topListItemShape
                                switchItems[1].size - 1 -> bottomListItemShape
                                else -> middleListItemShape
                            }
                        )
                    )
                }

                // Simplified progress
                item { Spacer(Modifier.height(12.dp)) }
                item {
                    ListItem(
                        leadingContent = {
                            Icon(painterResource(R.drawable.view_day), null)
                        },
                        headlineContent = { Text(stringResource(R.string.session_only_progress)) },
                        supportingContent = {
                            var expanded by remember { mutableStateOf(false) }
                            Text(
                                stringResource(R.string.session_only_progress_desc),
                                maxLines = if (expanded) Int.MAX_VALUE else 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .clickable { expanded = !expanded }
                                    .animateContentSize(motionScheme.defaultSpatialSpec())
                            )
                        },
                        trailingContent = {
                            Switch(
                                checked = settingsState.singleProgressBar,
                                onCheckedChange = {
                                    onAction(SettingsAction.SaveSingleProgressBar(it))
                                },
                                thumbContent = {
                                    Icon(
                                        painter = painterResource(if (settingsState.singleProgressBar) R.drawable.check else R.drawable.clear),
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize)
                                    )
                                },
                                colors = switchColors
                            )
                        },
                        colors = listItemColors,
                        modifier = Modifier.clip(cardShape)
                    )
                }

                // Expandable Pomodoro Info Button
                item {
                    var expanded by remember { mutableStateOf(false) }
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier
                            .padding(vertical = 6.dp)
                            .fillMaxWidth()
                    ) {
                        FilledTonalIconToggleButton(
                            checked = expanded,
                            onCheckedChange = { expanded = it },
                            shapes = IconButtonDefaults.toggleableShapes(),
                            modifier = Modifier.width(52.dp)
                        ) {
                            Icon(
                                painterResource(R.drawable.info),
                                null
                            )
                        }
                        AnimatedVisibility(expanded) {
                            Text(
                                stringResource(R.string.pomodoro_info),
                                style = typography.bodyMedium,
                                color = colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }

                item { Spacer(Modifier.height(12.dp)) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AlarmSettingsView(
    settingsState: SettingsState,
    contentPadding: PaddingValues,
    onAction: (SettingsAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    var alarmName by remember { mutableStateOf("...") }
    var vibrationPlaying by remember { mutableStateOf(false) }
    val msFormat = stringResource(R.string.milliseconds_format)

    val ringtoneNameProviderCallback = rememberRingtoneNameProviderCallback()
    val ringtonePickerLauncherCallback = rememberRingtonePickerLauncherCallback(
        alarmSoundFilePath = settingsState.alarmSoundUri,
        onResult = onAction
    )

    LaunchedEffect(settingsState.alarmSoundUri) {
        alarmName = ringtoneNameProviderCallback(settingsState.alarmSoundUri)
    }

    val vibrator = rememberPlatformVibrator()
    val hasVibrator = vibrator.hasVibrator
    val hasAmplitudeControl = vibrator.hasAmplitudeControl

    DisposableEffect(Unit) {
        onDispose { vibrator.cancel() }
    }

    val switchItems = remember(
        settingsState.alarmEnabled,
        settingsState.vibrateEnabled,
        settingsState.mediaVolumeForAlarm
    ) {
        listOf(
            buildList {
                add(
                    SettingsSwitchItem(
                        checked = settingsState.alarmEnabled,
                        icon = R.drawable.alarm_on,
                        label = R.string.sound,
                        description = R.string.alarm_desc,
                        onClick = { onAction(SettingsAction.SaveAlarmEnabled(it)) }
                    )
                )
                if (hasVibrator) add(
                    SettingsSwitchItem(
                        checked = settingsState.vibrateEnabled,
                        icon = R.drawable.mobile_vibrate,
                        label = R.string.vibrate,
                        description = R.string.vibrate_desc,
                        onClick = { onAction(SettingsAction.SaveVibrateEnabled(it)) }
                    )
                )
            },
            listOf(
                SettingsSwitchItem(
                    checked = settingsState.mediaVolumeForAlarm,
                    collapsible = true,
                    icon = R.drawable.music_note,
                    label = R.string.media_volume_for_alarm,
                    description = R.string.media_volume_for_alarm_desc,
                    onClick = { onAction(SettingsAction.SaveMediaVolumeForAlarm(it)) }
                )
            )
        )
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(detailPaneTopBarColors.containerColor)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                stringResource(R.string.alarm),
                                style = typography.headlineSmall.copy(
                                    fontFamily = NunitoSansFontFamily,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                stringResource(R.string.settings),
                                style = typography.bodySmall,
                                color = colorScheme.outline
                            )
                        }
                    },
                    navigationIcon = {
                        FilledTonalIconButton(
                            onClick = onBack,
                            shapes = IconButtonDefaults.shapes(),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = listItemColors.containerColor
                            ),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Icon(
                                painterResource(R.drawable.arrow_back),
                                contentDescription = stringResource(R.string.back)
                            )
                        }
                    },
                    colors = detailPaneTopBarColors,
                    scrollBehavior = scrollBehavior
                )
            },
            containerColor = detailPaneTopBarColors.containerColor,
            modifier = modifier
                .widthIn(max = PANE_MAX_WIDTH)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        ) { innerPadding ->
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding(),
                    bottom = contentPadding.calculateBottomPadding() + 24.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
                modifier = Modifier.fillMaxSize()
            ) {
                item { Spacer(Modifier.height(14.dp)) }

                // Alarm sound picker
                item {
                    PomodoroSegmentedListItem(
                        onClick = ringtonePickerLauncherCallback,
                        leadingContent = {
                            Icon(painterResource(R.drawable.alarm), null)
                        },
                        content = { Text(stringResource(R.string.alarm_sound)) },
                        supportingContent = { Text(alarmName) },
                        trailingContent = {
                            Icon(
                                painterResource(R.drawable.arrow_forward_big),
                                null
                            )
                        },
                        shape = getListItemShape(0, 3)
                    )
                }

                // Sound & Vibrate switches
                switchItems.forEachIndexed { baseIndex, items ->
                    itemsIndexed(items) { index, item ->
                        PomodoroSegmentedListItem(
                            onClick = { item.onClick(!item.checked) },
                            leadingContent = {
                                Icon(painterResource(item.icon), contentDescription = null)
                            },
                            content = { Text(stringResource(item.label)) },
                            supportingContent = {
                                if (item.collapsible) {
                                    var expanded by remember { mutableStateOf(false) }
                                    Text(
                                        stringResource(item.description),
                                        maxLines = if (expanded) Int.MAX_VALUE else 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier
                                            .clickable { expanded = !expanded }
                                            .animateContentSize(motionScheme.defaultSpatialSpec())
                                    )
                                } else {
                                    Text(stringResource(item.description))
                                }
                            },
                            trailingContent = {
                                Switch(
                                    checked = item.checked,
                                    onCheckedChange = { item.onClick(it) },
                                    thumbContent = {
                                        Icon(
                                            painter = painterResource(if (item.checked) R.drawable.check else R.drawable.clear),
                                            contentDescription = null,
                                            modifier = Modifier.size(SwitchDefaults.IconSize)
                                        )
                                    },
                                    colors = switchColors
                                )
                            },
                            shape = if (baseIndex == 0)
                                getListItemShape(index + 1, items.size + 1)
                            else getListItemShape(index, items.size)
                        )
                    }

                    if (baseIndex != switchItems.lastIndex) {
                        item { Spacer(Modifier.height(12.dp)) }
                    }
                }

                // Vibration Pattern Customizer
                if (hasVibrator) {
                    item { Spacer(Modifier.height(12.dp)) }

                    item {
                        val interactionSources = remember { List(2) { MutableInteractionSource() } }

                        ListItem(
                            headlineContent = { Text(stringResource(R.string.vibration_pattern)) },
                            trailingContent = {
                                ButtonGroup(
                                    overflowIndicator = {},
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    customItem(
                                        buttonGroupContent = {
                                            FilledIconToggleButton(
                                                checked = vibrationPlaying,
                                                onCheckedChange = {
                                                    vibrationPlaying = it
                                                    if (it) {
                                                        vibrator.playWaveform(
                                                            settingsState.vibrationOnDuration,
                                                            settingsState.vibrationOffDuration,
                                                            settingsState.vibrationAmplitude
                                                        )
                                                    } else {
                                                        vibrator.cancel()
                                                    }
                                                },
                                                interactionSource = interactionSources[0],
                                                modifier = Modifier
                                                    .size(52.dp, 40.dp)
                                                    .animateWidth(interactionSources[0])
                                            ) {
                                                if (vibrationPlaying)
                                                    Icon(painterResource(R.drawable.stop), null)
                                                else
                                                    Icon(painterResource(R.drawable.play), null)
                                            }
                                        },
                                        menuContent = {}
                                    )
                                    customItem(
                                        buttonGroupContent = {
                                            FilledTonalIconButton(
                                                onClick = {
                                                    onAction(SettingsAction.SaveVibrationOnDuration(1000L))
                                                    onAction(SettingsAction.SaveVibrationOffDuration(1000L))
                                                    onAction(SettingsAction.SaveVibrationAmplitude(SYSTEM_DEFAULT_AMPLITUDE))
                                                },
                                                shapes = IconButtonDefaults.shapes(),
                                                interactionSource = interactionSources[1],
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .animateWidth(interactionSources[1])
                                            ) {
                                                Icon(
                                                    painterResource(R.drawable.restore_default),
                                                    null
                                                )
                                            }
                                        },
                                        menuContent = {}
                                    )
                                }
                            },
                            colors = listItemColors,
                            modifier = Modifier.clip(topListItemShape)
                        )
                    }

                    item {
                        SliderListItem(
                            value = settingsState.vibrationOnDuration.toFloat(),
                            valueRange = 10f..5000f,
                            enabled = true,
                            label = stringResource(R.string.duration),
                            trailingLabel = { String.format(msFormat, it.roundToInt()) },
                            trailingIcon = { Icon(painterResource(R.drawable.airwave), null) },
                            shape = middleListItemShape
                        ) { onAction(SettingsAction.SaveVibrationOnDuration(it.roundToLong())) }
                    }

                    item {
                        SliderListItem(
                            value = settingsState.vibrationOffDuration.toFloat(),
                            valueRange = 10f..5000f,
                            enabled = true,
                            label = stringResource(R.string.gap),
                            trailingLabel = { String.format(msFormat, it.roundToInt()) },
                            trailingIcon = { Icon(painterResource(R.drawable.menu), null) },
                            shape = if (hasAmplitudeControl) middleListItemShape else bottomListItemShape
                        ) { onAction(SettingsAction.SaveVibrationOffDuration(it.roundToLong())) }
                    }

                    if (hasAmplitudeControl) {
                        item {
                            val systemDefaultText = stringResource(R.string.system_default)
                            SliderListItem(
                                value = if (settingsState.vibrationAmplitude == SYSTEM_DEFAULT_AMPLITUDE) 127f
                                else settingsState.vibrationAmplitude.toFloat(),
                                valueRange = 2f..255f,
                                enabled = true,
                                label = stringResource(R.string.vibration_strength),
                                trailingLabel = {
                                    if (settingsState.vibrationAmplitude == SYSTEM_DEFAULT_AMPLITUDE)
                                        systemDefaultText
                                    else "${((it * 100) / 255f).roundToInt()}%"
                                },
                                trailingIcon = { Icon(painterResource(R.drawable.bolt), null) },
                                shape = bottomListItemShape
                            ) { onAction(SettingsAction.SaveVibrationAmplitude(it.roundToInt())) }
                        }
                    }
                }

                item { Spacer(Modifier.height(14.dp)) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun BackupRestoreSettingsView(
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    var showDialog by remember { mutableIntStateOf(0) }
    var backupState by remember { mutableStateOf(BackupRestoreState.CHOOSE_FILE) }

    if (showDialog == 1) {
        BackupBottomSheet(
            backupState = backupState,
            onDismissRequest = { showDialog = 0 },
            onStartBackup = {
                scope.launch {
                    backupState = BackupRestoreState.LOADING
                    delay(1500)
                    backupState = BackupRestoreState.DONE
                }
            },
            resetBackupState = { backupState = BackupRestoreState.CHOOSE_FILE }
        )
    } else if (showDialog == 2) {
        RestoreBottomSheet(
            restoreState = backupState,
            onDismissRequest = {
                if (backupState == BackupRestoreState.DONE) {
                    restartApp(context)
                } else {
                    showDialog = 0
                }
            },
            onStartRestore = {
                scope.launch {
                    backupState = BackupRestoreState.LOADING
                    delay(1500)
                    backupState = BackupRestoreState.DONE
                }
            },
            resetRestoreState = { backupState = BackupRestoreState.CHOOSE_FILE }
        )
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(detailPaneTopBarColors.containerColor)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                stringResource(R.string.backup_and_restore),
                                style = typography.headlineSmall.copy(
                                    fontFamily = NunitoSansFontFamily,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                stringResource(R.string.settings),
                                style = typography.bodySmall,
                                color = colorScheme.outline
                            )
                        }
                    },
                    navigationIcon = {
                        FilledTonalIconButton(
                            onClick = onBack,
                            shapes = IconButtonDefaults.shapes(),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = listItemColors.containerColor
                            ),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Icon(
                                painterResource(R.drawable.arrow_back),
                                contentDescription = stringResource(R.string.back)
                            )
                        }
                    },
                    colors = detailPaneTopBarColors,
                    scrollBehavior = scrollBehavior
                )
            },
            containerColor = detailPaneTopBarColors.containerColor,
            modifier = modifier
                .widthIn(max = PANE_MAX_WIDTH)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        ) { innerPadding ->
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding(),
                    bottom = contentPadding.calculateBottomPadding() + 24.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
                modifier = Modifier.fillMaxSize()
            ) {
                item { Spacer(Modifier.height(14.dp)) }

                item {
                    PomodoroSegmentedListItem(
                        onClick = { showDialog = 1 },
                        content = { Text(stringResource(R.string.backup)) },
                        supportingContent = { Text(stringResource(R.string.backup_desc)) },
                        leadingContent = { Icon(painterResource(R.drawable.backup), null) },
                        selected = showDialog == 1,
                        shape = getListItemShape(0, 2)
                    )
                }

                item {
                    PomodoroSegmentedListItem(
                        onClick = { showDialog = 2 },
                        content = { Text(stringResource(R.string.restore)) },
                        supportingContent = { Text(stringResource(R.string.restore_desc)) },
                        leadingContent = { Icon(painterResource(R.drawable.restore), null) },
                        selected = showDialog == 2,
                        shape = getListItemShape(1, 2)
                    )
                }
            }
        }
    }
}
