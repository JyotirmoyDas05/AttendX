package `in`.jyotirmoy.attendx.tools.presentation.pomodoro

import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import `in`.jyotirmoy.attendx.R
import `in`.jyotirmoy.attendx.core.common.LocalFloatingNavBarPadding
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.components.AlarmDialog
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.components.AlwaysOnDisplay
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.components.TopicBottomSheet
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.model.TimerMode
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.screens.PomodoroSettingsScreen
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.screens.PomodoroStatsScreen
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.screens.PomodoroTimerScreen
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.settings.SettingsViewModel
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.stats.StatsViewModel

enum class PomodoroSubScreen {
    TIMER, STATS, SETTINGS, AOD
}

data class PomodoroNavItem(
    val screen: PomodoroSubScreen,
    val unselectedIcon: Int,
    val selectedIcon: Int,
    val labelRes: Int
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PomodoroScreen(
    timerViewModel: PomodoroViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    statsViewModel: StatsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme

    val timerState by timerViewModel.timerState.collectAsStateWithLifecycle()
    val settingsState by settingsViewModel.settingsState.collectAsStateWithLifecycle()
    val progress by timerViewModel.progress.collectAsStateWithLifecycle()
    val topics by timerViewModel.topics.collectAsStateWithLifecycle()
    val selectedTopic by timerViewModel.selectedTopic.collectAsStateWithLifecycle()
    val isCreateTopicSheetOpen by timerViewModel.isCreateTopicSheetOpen.collectAsStateWithLifecycle()
    val isEditTopicSheetOpen by timerViewModel.isEditTopicSheetOpen.collectAsStateWithLifecycle()

    var currentScreen by remember { mutableStateOf(PomodoroSubScreen.TIMER) }

    val primary by animateColorAsState(
        if (timerState.timerMode == TimerMode.FOCUS) colorScheme.primary else colorScheme.tertiary,
        label = "toolbarPrimary"
    )
    val onPrimary by animateColorAsState(
        if (timerState.timerMode == TimerMode.FOCUS) colorScheme.onPrimary else colorScheme.onTertiary,
        label = "toolbarOnPrimary"
    )
    val primaryContainer by animateColorAsState(
        if (timerState.timerMode == TimerMode.FOCUS) colorScheme.primaryContainer else colorScheme.tertiaryContainer,
        label = "toolbarPrimaryContainer"
    )
    val onPrimaryContainer by animateColorAsState(
        if (timerState.timerMode == TimerMode.FOCUS) colorScheme.onPrimaryContainer else colorScheme.onTertiaryContainer,
        label = "toolbarOnPrimaryContainer"
    )

    val navItems = remember {
        listOf(
            PomodoroNavItem(
                PomodoroSubScreen.TIMER,
                R.drawable.timer_outlined,
                R.drawable.timer_filled,
                R.string.timer
            ),
            PomodoroNavItem(
                PomodoroSubScreen.STATS,
                R.drawable.monitoring,
                R.drawable.monitoring_filled,
                R.string.stats
            ),
            PomodoroNavItem(
                PomodoroSubScreen.SETTINGS,
                R.drawable.settings,
                R.drawable.settings_filled,
                R.string.settings
            )
        )
    }

    if (timerState.alarmRinging) {
        AlarmDialog(
            stopAlarm = {
                val intent = Intent(context, PomodoroService::class.java).apply {
                    action = PomodoroService.Actions.STOP_ALARM.toString()
                }
                context.startService(intent)
            }
        )
    }

    if (isCreateTopicSheetOpen) {
        TopicBottomSheet(
            initialTopic = null,
            onDismiss = timerViewModel::closeCreateTopicSheet,
            onSaveTopic = timerViewModel::saveTopic
        )
    }

    if (isEditTopicSheetOpen) {
        TopicBottomSheet(
            initialTopic = selectedTopic,
            onDismiss = timerViewModel::closeEditTopicSheet,
            onSaveTopic = timerViewModel::saveTopic
        )
    }

    if (currentScreen == PomodoroSubScreen.AOD) {
        AlwaysOnDisplay(
            timerState = timerState,
            progress = { progress },
            onDismiss = { currentScreen = PomodoroSubScreen.TIMER }
        )
    } else {
        Scaffold(
            bottomBar = {
                val floatingNavBarPadding = LocalFloatingNavBarPadding.current
                val navBarPadding = if (floatingNavBarPadding > 0.dp) floatingNavBarPadding else 16.dp

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = navBarPadding + 8.dp)
                        .zIndex(2f),
                    contentAlignment = Alignment.Center
                ) {
                    HorizontalFloatingToolbar(
                        expanded = true,
                        colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(
                            toolbarContainerColor = primaryContainer,
                            toolbarContentColor = onPrimaryContainer
                        )
                    ) {
                        navItems.forEach { item ->
                            val selected = currentScreen == item.screen
                            TooltipBox(
                                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                                    TooltipAnchorPosition.Above
                                ),
                                tooltip = { PlainTooltip { Text(stringResource(item.labelRes)) } },
                                state = rememberTooltipState()
                            ) {
                                ToggleButton(
                                    checked = selected,
                                    onCheckedChange = { if (!selected) currentScreen = item.screen },
                                    colors = ToggleButtonDefaults.toggleButtonColors(
                                        containerColor = primaryContainer,
                                        contentColor = onPrimaryContainer,
                                        checkedContainerColor = primary,
                                        checkedContentColor = onPrimary
                                    ),
                                    shapes = ToggleButtonDefaults.shapes(CircleShape, CircleShape, CircleShape),
                                    modifier = Modifier.height(56.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Crossfade(targetState = selected, label = "iconCrossfade") { isSel ->
                                            Icon(
                                                painter = painterResource(if (isSel) item.selectedIcon else item.unselectedIcon),
                                                contentDescription = stringResource(item.labelRes),
                                                modifier = Modifier.padding(horizontal = 4.dp)
                                            )
                                        }
                                        AnimatedVisibility(
                                            visible = selected,
                                            enter = expandHorizontally(),
                                            exit = shrinkHorizontally()
                                        ) {
                                            Text(
                                                text = stringResource(item.labelRes),
                                                fontFamily = `in`.jyotirmoy.attendx.tools.presentation.pomodoro.theme.NunitoFontFamily,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Clip,
                                                modifier = Modifier.padding(start = ButtonDefaults.IconSpacing, end = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        ) { contentPadding ->
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    fadeIn().togetherWith(fadeOut())
                },
                label = "screenTransition",
                modifier = Modifier.fillMaxSize()
            ) { screen ->
                when (screen) {
                    PomodoroSubScreen.TIMER -> {
                        PomodoroTimerScreen(
                            timerState = timerState,
                            settingsState = settingsState,
                            selectedTopic = selectedTopic,
                            topics = topics,
                            contentPadding = contentPadding,
                            progress = { progress },
                            onAction = timerViewModel::onAction,
                            onSelectTopic = timerViewModel::selectTopic,
                            onCreateTopic = timerViewModel::openCreateTopicSheet,
                            onNavigateAod = { currentScreen = PomodoroSubScreen.AOD }
                        )
                    }
                    PomodoroSubScreen.STATS -> {
                        PomodoroStatsScreen(
                            contentPadding = contentPadding,
                            viewModel = statsViewModel
                        )
                    }
                    PomodoroSubScreen.SETTINGS -> {
                        PomodoroSettingsScreen(
                            contentPadding = contentPadding,
                            viewModel = settingsViewModel,
                            topics = topics,
                            selectedTopic = selectedTopic,
                            onSelectTopic = timerViewModel::selectTopic,
                            onCreateTopic = timerViewModel::openCreateTopicSheet,
                            onEditTopic = timerViewModel::openEditTopicSheet
                        )
                    }
                    PomodoroSubScreen.AOD -> {}
                }
            }
        }
    }
}
