package `in`.jyotirmoy.attendx.tools.presentation.pomodoro.screens

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Label
import androidx.compose.material.icons.rounded.NewLabel
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.jyotirmoy.attendx.R
import `in`.jyotirmoy.attendx.tools.data.model.Topic
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.model.SettingsState
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.model.TimerAction
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.model.TimerMode
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.model.TimerState
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.theme.NunitoFontFamily
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.theme.NunitoSansFontFamily
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PomodoroTimerScreen(
    timerState: TimerState,
    settingsState: SettingsState,
    selectedTopic: Topic,
    topics: List<Topic>,
    contentPadding: PaddingValues,
    progress: () -> Float,
    onAction: (TimerAction) -> Unit,
    onSelectTopic: (Topic) -> Unit,
    onCreateTopic: () -> Unit,
    onNavigateAod: () -> Unit,
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val shapes = MaterialTheme.shapes

    var isTopicMenuOpen by remember { mutableStateOf(false) }

    val isFocus = timerState.timerMode == TimerMode.FOCUS
    val isNextFocus = timerState.nextTimerMode == TimerMode.FOCUS

    val color by animateColorAsState(
        if (isFocus) colorScheme.primary else colorScheme.tertiary,
        label = "modeColor"
    )
    val onColor by animateColorAsState(
        if (isFocus) colorScheme.onPrimary else colorScheme.onTertiary,
        label = "modeOnColor"
    )
    val colorContainer by animateColorAsState(
        if (isFocus) colorScheme.secondaryContainer else colorScheme.tertiaryContainer,
        label = "modeColorContainer"
    )
    val onColorContainer by animateColorAsState(
        if (isFocus) colorScheme.onSecondaryContainer else colorScheme.onTertiaryContainer,
        label = "modeOnColorContainer"
    )

    val clockFontSize by animateFloatAsState(
        targetValue = if (!timerState.infiniteFocus) {
            if (timerState.timeStr.length < 6) 80f else 66f
        } else {
            if (timerState.timeStr.length < 6) 98f else 82f
        },
        label = "clockFontSize"
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {}
    )

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(colorScheme.surfaceContainerHighest)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AnimatedContent(
                            targetState = timerState.timerMode,
                            transitionSpec = {
                                slideInVertically(initialOffsetY = { -it })
                                    .togetherWith(slideOutVertically(targetOffsetY = { it }))
                            },
                            contentAlignment = Alignment.Center
                        ) { mode ->
                            Text(
                                text = when (mode) {
                                    TimerMode.FOCUS -> if (timerState.infiniteFocus) stringResource(R.string.infinite_focus) else stringResource(R.string.focus)
                                    TimerMode.SHORT_BREAK -> stringResource(R.string.short_break)
                                    TimerMode.LONG_BREAK -> stringResource(R.string.long_break)
                                    else -> stringResource(R.string.pomodoro_timer)
                                },
                                style = typography.headlineMedium.copy(
                                    fontFamily = NunitoSansFontFamily,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (mode == TimerMode.FOCUS) colorScheme.primary else colorScheme.tertiary
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                        Text(
                            text = selectedTopic.name,
                            style = typography.bodyMedium.copy(
                                fontFamily = NunitoFontFamily,
                                fontWeight = FontWeight.Medium,
                                color = colorScheme.onSurfaceVariant
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                },
                actions = {
                    Box(modifier = Modifier.padding(end = 8.dp)) {
                        IconButton(
                            onClick = { isTopicMenuOpen = true },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(colorScheme.surfaceContainerHighest)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Label,
                                contentDescription = "Select Topic",
                                tint = colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = isTopicMenuOpen,
                            onDismissRequest = { isTopicMenuOpen = false },
                            shape = RoundedCornerShape(20.dp),
                            containerColor = colorScheme.surfaceContainerHigh
                        ) {
                            topics.forEach { topic ->
                                val isSelected = topic.id == selectedTopic.id
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = topic.name,
                                            fontFamily = NunitoFontFamily,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) colorScheme.primary else colorScheme.onSurface
                                        )
                                    },
                                    onClick = {
                                        onSelectTopic(topic)
                                        isTopicMenuOpen = false
                                    },
                                    modifier = if (isSelected) {
                                        Modifier
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(colorScheme.primaryContainer)
                                    } else {
                                        Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Create new topic",
                                        fontFamily = NunitoFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colorScheme.primary
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.NewLabel,
                                        contentDescription = null,
                                        tint = colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                onClick = {
                                    isTopicMenuOpen = false
                                    onCreateTopic()
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = contentPadding.calculateBottomPadding() + 8.dp)
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 4.dp,
                bottom = contentPadding.calculateBottomPadding() + 16.dp,
                start = 16.dp,
                end = 16.dp
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .widthIn(max = 380.dp)
                            .fillMaxWidth(0.92f)
                            .aspectRatio(1f)
                    ) {
                        this@Column.AnimatedVisibility(
                            visible = !timerState.infiniteFocus,
                            enter = fadeIn() + scaleIn(initialScale = 0.8f),
                            exit = fadeOut() + scaleOut(targetScale = 0.8f)
                        ) {
                            if (isFocus) {
                                CircularProgressIndicator(
                                    progress = progress,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp),
                                    color = color,
                                    trackColor = colorContainer.copy(alpha = 0.45f),
                                    strokeWidth = 20.dp,
                                    gapSize = 8.dp
                                )
                            } else {
                                CircularWavyProgressIndicator(
                                    progress = progress,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp),
                                    color = color,
                                    trackColor = colorContainer.copy(alpha = 0.45f),
                                    stroke = Stroke(
                                        width = with(LocalDensity.current) { 20.dp.toPx() },
                                        cap = StrokeCap.Round
                                    ),
                                    trackStroke = Stroke(
                                        width = with(LocalDensity.current) { 20.dp.toPx() },
                                        cap = StrokeCap.Round
                                    ),
                                    wavelength = 60.dp,
                                    gapSize = 8.dp
                                )
                            }
                        }

                        var expanded by remember { mutableStateOf(timerState.showBrandTitle) }
                        val timerResetSettingsInfo = stringResource(R.string.timer_settings_reset_info)

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(shapes.large)
                                .combinedClickable(
                                    onClick = {
                                        expanded = !expanded
                                        if (settingsState.aodEnabled) {
                                            onNavigateAod()
                                        }
                                    },
                                    onLongClick = {
                                        if (!timerState.timerRunning) {
                                            onAction(TimerAction.SetInfiniteFocus(!timerState.infiniteFocus))
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        } else {
                                            Toast.makeText(context, timerResetSettingsInfo, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                        ) {
                            Text(
                                text = timerState.timeStr,
                                style = TextStyle(
                                    fontFamily = NunitoFontFamily,
                                    fontSize = clockFontSize.sp,
                                    letterSpacing = (-2.5).sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.onSurface
                                ),
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                            AnimatedVisibility(
                                visible = expanded,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                Text(
                                    text = stringResource(
                                        R.string.timer_session_count,
                                        timerState.currentFocusCount,
                                        timerState.totalFocusCount
                                    ),
                                    style = typography.titleMedium.copy(fontFamily = NunitoFontFamily),
                                    color = colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }

            item {
                val interactionSources = remember { List(3) { MutableInteractionSource() } }

                ButtonGroup(
                    overflowIndicator = { state ->
                        ButtonGroupDefaults.OverflowIndicator(
                            state,
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = colorContainer,
                                contentColor = onColorContainer
                            ),
                            modifier = Modifier.size(64.dp, 96.dp)
                        )
                    },
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    customItem(
                        {
                            FilledIconToggleButton(
                                checked = timerState.timerRunning,
                                onCheckedChange = { checked ->
                                    onAction(TimerAction.ToggleTimer)
                                    if (checked) haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                                    else haptic.performHapticFeedback(HapticFeedbackType.ToggleOff)

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && checked) {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                },
                                colors = IconButtonDefaults.filledIconToggleButtonColors(
                                    checkedContainerColor = color,
                                    checkedContentColor = onColor,
                                    containerColor = colorContainer,
                                    contentColor = onColorContainer
                                ),
                                shapes = IconButtonDefaults.toggleableShapes(),
                                interactionSource = interactionSources[0],
                                modifier = Modifier
                                    .size(width = 120.dp, height = 96.dp)
                                    .animateWidth(interactionSources[0])
                            ) {
                                Icon(
                                    painter = painterResource(
                                        if (timerState.timerRunning) R.drawable.pause_large else R.drawable.play_large
                                    ),
                                    contentDescription = if (timerState.timerRunning) stringResource(R.string.pause) else stringResource(R.string.play),
                                    tint = if (timerState.timerRunning) onColor else onColorContainer,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        },
                        { state ->
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(if (timerState.timerRunning) R.drawable.pause else R.drawable.play),
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                text = {
                                    Text(
                                        if (timerState.timerRunning) stringResource(R.string.pause) else stringResource(R.string.play)
                                    )
                                },
                                onClick = {
                                    onAction(TimerAction.ToggleTimer)
                                    state.dismiss()
                                }
                            )
                        }
                    )

                    customItem(
                        {
                            val timerResetMessage = stringResource(R.string.timer_reset_message)
                            val undo = stringResource(R.string.undo)

                            FilledTonalIconButton(
                                onClick = {
                                    onAction(TimerAction.ResetTimer)
                                    haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                                    scope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            message = timerResetMessage,
                                            actionLabel = undo,
                                            withDismissAction = true,
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            onAction(TimerAction.UndoReset)
                                        }
                                    }
                                },
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = colorContainer,
                                    contentColor = onColorContainer
                                ),
                                shapes = IconButtonDefaults.shapes(),
                                interactionSource = interactionSources[1],
                                modifier = Modifier
                                    .size(90.dp, 96.dp)
                                    .animateWidth(interactionSources[1])
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.restart_large),
                                    contentDescription = stringResource(R.string.restart),
                                    tint = onColorContainer,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        },
                        { state ->
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(R.drawable.restart_large),
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                text = { Text(stringResource(R.string.restart)) },
                                onClick = {
                                    onAction(TimerAction.ResetTimer)
                                    state.dismiss()
                                }
                            )
                        }
                    )

                    customItem(
                        {
                            FilledTonalIconButton(
                                onClick = {
                                    onAction(TimerAction.SkipTimer(fromButton = true))
                                    haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                                },
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = colorContainer,
                                    contentColor = onColorContainer
                                ),
                                shapes = IconButtonDefaults.shapes(),
                                interactionSource = interactionSources[2],
                                modifier = Modifier
                                    .size(width = 64.dp, height = 96.dp)
                                    .animateWidth(interactionSources[2])
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.skip_next_large),
                                    contentDescription = stringResource(R.string.skip_to_next),
                                    tint = onColorContainer,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        },
                        { state ->
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(R.drawable.skip_next),
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                text = { Text(stringResource(R.string.skip_to_next)) },
                                onClick = {
                                    onAction(TimerAction.SkipTimer(fromButton = true))
                                    state.dismiss()
                                }
                            )
                        }
                    )
                }
            }

            // Up Next Section
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.up_next),
                        style = typography.titleSmall.copy(
                            fontFamily = NunitoFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.outline
                        )
                    )
                    Spacer(Modifier.height(4.dp))
                    AnimatedContent(
                        targetState = timerState.nextTimeStr,
                        transitionSpec = {
                            slideInVertically(initialOffsetY = { -it })
                                .togetherWith(slideOutVertically(targetOffsetY = { it }))
                        },
                        label = "nextTimeAnim"
                    ) { nextTime ->
                        Text(
                            text = nextTime,
                            style = TextStyle(
                                fontFamily = NunitoFontFamily,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isNextFocus) colorScheme.primary else colorScheme.tertiary
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                    AnimatedContent(
                        targetState = timerState.nextTimerMode,
                        transitionSpec = {
                            slideInVertically(initialOffsetY = { -it })
                                .togetherWith(slideOutVertically(targetOffsetY = { it }))
                        },
                        label = "nextModeAnim"
                    ) { nextMode ->
                        Text(
                            text = when (nextMode) {
                                TimerMode.FOCUS -> stringResource(R.string.focus)
                                TimerMode.SHORT_BREAK -> stringResource(R.string.short_break)
                                else -> stringResource(R.string.long_break)
                            },
                            style = typography.titleMedium.copy(
                                fontFamily = NunitoFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                color = colorScheme.onSurface
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
