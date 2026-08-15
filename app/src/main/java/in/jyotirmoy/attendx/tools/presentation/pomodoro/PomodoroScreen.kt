package `in`.jyotirmoy.attendx.tools.presentation.pomodoro

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.jyotirmoy.attendx.core.presentation.components.text.AutoResizeableText
import `in`.jyotirmoy.attendx.core.presentation.util.springyPressEffect
import `in`.jyotirmoy.attendx.core.utils.HapticUtils.weakHaptic


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PomodoroScreen(
    viewModel: PomodoroViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val view = LocalView.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // mode-based animated color
    val modeColor by animateColorAsState(
        targetValue = when (state.mode) {
            PomodoroMode.FOCUS -> MaterialTheme.colorScheme.primary
            PomodoroMode.SHORT_BREAK -> MaterialTheme.colorScheme.tertiary
            PomodoroMode.LONG_BREAK -> MaterialTheme.colorScheme.secondary
        },
        animationSpec = tween(400),
        label = "modeColor"
    )

    val isPlaying = state.isRunning && !state.isPaused

    val resetInteraction = remember { MutableInteractionSource() }
    val playPauseInteraction = remember { MutableInteractionSource() }
    val skipInteraction = remember { MutableInteractionSource() }

    var resetRotationTarget by remember { mutableFloatStateOf(0f) }
    val resetRotation by animateFloatAsState(
        targetValue = resetRotationTarget,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = 800f
        ),
        label = "resetRotation"
    )

    @Composable
    fun ControlsRow(modifier: Modifier = Modifier) {
        Row(
            modifier = modifier.padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ToggleButton(
                checked = false,
                onCheckedChange = {
                    view.weakHaptic()
                    resetRotationTarget -= 360f
                    viewModel.resetTimer()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp)
                    .springyPressEffect(
                        interactionSource = resetInteraction,
                        neighborInteractionSources = listOf(playPauseInteraction),
                        scaleDown = 0.90f
                    )
                    .semantics { role = Role.Button },
                interactionSource = resetInteraction,
                shapes = ButtonGroupDefaults.connectedLeadingButtonShapes(),
                colors = ToggleButtonDefaults.toggleButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    checkedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    checkedContentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Icon(
                    Icons.Rounded.Refresh,
                    contentDescription = "Reset",
                    modifier = Modifier
                        .size(28.dp)
                        .graphicsLayer { rotationZ = resetRotation }
                )
            }

            ToggleButton(
                checked = isPlaying,
                onCheckedChange = {
                    view.weakHaptic()
                    if (isPlaying) viewModel.pauseTimer()
                    else viewModel.startTimer()
                },
                modifier = Modifier
                    .weight(1.8f)
                    .height(64.dp)
                    .springyPressEffect(
                        interactionSource = playPauseInteraction,
                        neighborInteractionSources = listOf(resetInteraction, skipInteraction),
                        scaleDown = 0.85f
                    )
                    .semantics { role = Role.Button },
                interactionSource = playPauseInteraction,
                shapes = ButtonGroupDefaults.connectedMiddleButtonShapes(),
                colors = ToggleButtonDefaults.toggleButtonColors(
                    containerColor = modeColor.copy(alpha = 0.2f),
                    contentColor = modeColor,
                    checkedContainerColor = modeColor,
                    checkedContentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                AnimatedContent(
                    targetState = isPlaying,
                    transitionSpec = {
                        (scaleIn(
                            animationSpec = spring(
                                dampingRatio = 0.6f,
                                stiffness = 800f
                            ),
                            initialScale = 0.0f
                        ) + fadeIn(
                            animationSpec = spring(
                                dampingRatio = 1.0f,
                                stiffness = 1600f
                            )
                        )).togetherWith(
                            scaleOut(
                                animationSpec = spring(
                                    dampingRatio = 1.0f,
                                    stiffness = 1600f
                                ),
                                targetScale = 0.0f
                            ) + fadeOut(
                                animationSpec = spring(
                                    dampingRatio = 1.0f,
                                    stiffness = 1600f
                                )
                            )
                        ).using(SizeTransform(clip = false))
                    },
                    label = "playPauseIcon"
                ) { playing ->
                    Icon(
                        if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (playing) "Pause" else "Play",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            ToggleButton(
                checked = false,
                onCheckedChange = {
                    view.weakHaptic()
                    viewModel.skipSession()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp)
                    .springyPressEffect(
                        interactionSource = skipInteraction,
                        neighborInteractionSources = listOf(playPauseInteraction),
                        scaleDown = 0.90f
                    )
                    .semantics { role = Role.Button },
                interactionSource = skipInteraction,
                shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(),
                colors = ToggleButtonDefaults.toggleButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    checkedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    checkedContentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Icon(
                    Icons.Rounded.SkipNext,
                    contentDescription = "Skip",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }

    @Composable
    fun ModeSelector(modifier: Modifier = Modifier) {
        SingleChoiceSegmentedButtonRow(
            modifier = modifier.fillMaxWidth()
        ) {
            PomodoroMode.entries.forEachIndexed { index, mode ->
                SegmentedButton(
                    selected = state.mode == mode,
                    onClick = { viewModel.changeMode(mode) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = PomodoroMode.entries.size
                    ),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = modeColor.copy(alpha = 0.15f),
                        activeContentColor = modeColor
                    )
                ) {
                    Text(mode.label)
                }
            }
        }
    }

    @Composable
    fun TimerDisplay(modifier: Modifier = Modifier) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
        ) {
            val progress by animateFloatAsState(
                targetValue = if (state.totalTime > 0) {
                    state.timeLeft.toFloat() / state.totalTime.toFloat()
                } else 1f,
                animationSpec = tween(300),
                label = "progress"
            )

            CircularWavyProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxSize(),
                color = modeColor,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize(0.65f)
            ) {
                AutoResizeableText(
                    text = formatTime(state.timeLeft),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 60.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = modeColor,
                    maxLines = 1,
                    softWrap = false,
                    minFontSize = 12.sp
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Pomodoro Timer") })
        }
    ) { padding ->
        if (isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TimerDisplay(
                    modifier = Modifier
                        .fillMaxHeight(0.85f)
                        .aspectRatio(1f)
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 24.dp),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ModeSelector()
                    Spacer(modifier = Modifier.height(16.dp))
                    ControlsRow(modifier = Modifier.fillMaxWidth())
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                ModeSelector()

                TimerDisplay(
                    modifier = Modifier
                        .size(280.dp)
                        .aspectRatio(1f)
                )

                ControlsRow(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

private fun formatTime(millis: Long): String {
    val totalSeconds = millis / 1000
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return String.format("%02d:%02d", m, s)
}

