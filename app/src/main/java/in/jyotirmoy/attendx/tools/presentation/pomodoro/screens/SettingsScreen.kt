package `in`.jyotirmoy.attendx.tools.presentation.pomodoro.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import `in`.jyotirmoy.attendx.R
import `in`.jyotirmoy.attendx.tools.data.model.Topic
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.model.SettingsAction
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.settings.SettingsViewModel
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.util.millisecondsToHoursMinutes
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.util.millisecondsToMinutes

enum class SettingsSubScreen {
    MAIN,
    TIMER,
    ALARM,
    APPEARANCE,
    BACKUP_RESTORE
}

@OptIn(ExperimentalMaterial3Api::class)
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

    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    when (currentSubScreen) {
        SettingsSubScreen.MAIN -> {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = stringResource(R.string.settings),
                                style = typography.headlineMedium.copy(
                                    fontFamily = `in`.jyotirmoy.attendx.tools.presentation.pomodoro.theme.NunitoSansFontFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 34.sp
                                )
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                },
                modifier = modifier
            ) { innerPadding ->
                LazyColumn(
                    contentPadding = PaddingValues(
                        top = innerPadding.calculateTopPadding(),
                        bottom = contentPadding.calculateBottomPadding() + 24.dp,
                        start = 16.dp,
                        end = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Timer Settings Group
                    item {
                        SettingsNavigationItem(
                            iconRes = R.drawable.clocks,
                            title = stringResource(R.string.timer),
                            subtitle = "Durations, Do Not Disturb, Always On Display...",
                            onClick = { currentSubScreen = SettingsSubScreen.TIMER }
                        )
                    }

                    // Topics Item
                    item {
                        SettingsNavigationItem(
                            iconRes = R.drawable.folder,
                            title = "Topics",
                            subtitle = "Create and manage topics",
                            onClick = onCreateTopic
                        )
                    }

                    // Alarm Settings Group
                    item {
                        SettingsNavigationItem(
                            iconRes = R.drawable.alarm,
                            title = stringResource(R.string.alarm),
                            subtitle = "Alarm sound, Sound, Vibration, Headphone mode...",
                            onClick = { currentSubScreen = SettingsSubScreen.ALARM }
                        )
                    }

                    // Backup & Restore
                    item {
                        SettingsNavigationItem(
                            iconRes = R.drawable.backup,
                            title = "Backup and restore",
                            subtitle = "Backup, Restore, Reset stats",
                            onClick = { currentSubScreen = SettingsSubScreen.BACKUP_RESTORE }
                        )
                    }
                }
            }
        }

        SettingsSubScreen.TIMER -> {
            Scaffold(
                topBar = {
                    TopAppBar(
                        navigationIcon = {
                            IconButton(
                                onClick = { currentSubScreen = SettingsSubScreen.MAIN },
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(colorScheme.surfaceContainerHighest)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = colorScheme.onSurface
                                )
                            }
                        },
                        title = {
                            Column {
                                Text(
                                    text = stringResource(R.string.timer),
                                    style = typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 28.sp)
                                )
                                Text(
                                    text = stringResource(R.string.settings),
                                    style = typography.bodySmall,
                                    color = colorScheme.outline
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                },
                modifier = modifier
            ) { innerPadding ->
                LazyColumn(
                    contentPadding = PaddingValues(
                        top = innerPadding.calculateTopPadding(),
                        bottom = contentPadding.calculateBottomPadding() + 24.dp,
                        start = 16.dp,
                        end = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Topic Chips Row (Screenshot 12)
                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                        ) {
                            topics.forEach { topic ->
                                val isSelected = topic.id == selectedTopic.id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onSelectTopic(topic) },
                                    label = { Text(topic.name, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = colorScheme.primaryContainer,
                                        selectedLabelColor = colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                            FilterChip(
                                selected = false,
                                onClick = onCreateTopic,
                                label = {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add Topic",
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                shape = RoundedCornerShape(16.dp)
                            )
                        }
                    }

                    // Info Card
                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainerLow),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = "Long press the timer clock to enable infinite focus mode",
                                    style = typography.bodyMedium,
                                    color = colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // 3 Giant Duration Tiles (Screenshot 12)
                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            DurationTile(
                                label = stringResource(R.string.focus),
                                minutes = (settingsState.focusTime / 60000).toInt(),
                                onMinutesChanged = { mins ->
                                    viewModel.onAction(SettingsAction.SaveFocusTime(mins.toLong() * 60000L))
                                },
                                modifier = Modifier.weight(1f)
                            )
                            DurationTile(
                                label = stringResource(R.string.short_break),
                                minutes = (settingsState.shortBreakTime / 60000).toInt(),
                                onMinutesChanged = { mins ->
                                    viewModel.onAction(SettingsAction.SaveShortBreakTime(mins.toLong() * 60000L))
                                },
                                modifier = Modifier.weight(1f)
                            )
                            DurationTile(
                                label = stringResource(R.string.long_break),
                                minutes = (settingsState.longBreakTime / 60000).toInt(),
                                onMinutesChanged = { mins ->
                                    viewModel.onAction(SettingsAction.SaveLongBreakTime(mins.toLong() * 60000L))
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Session Length Card
                    item {
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = stringResource(R.string.session_length),
                                    style = typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Focus intervals in one session: ${settingsState.sessionLength}",
                                    style = typography.bodySmall,
                                    color = colorScheme.outline
                                )
                                Spacer(Modifier.height(12.dp))
                                Slider(
                                    value = settingsState.sessionLength.toFloat(),
                                    onValueChange = { count ->
                                        viewModel.onAction(SettingsAction.SaveSessionLength(count.toInt()))
                                    },
                                    valueRange = 2f..10f,
                                    steps = 7,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    // Auto Start Next Timer
                    item {
                        SettingsToggleCard(
                            iconRes = R.drawable.autoplay,
                            title = stringResource(R.string.auto_start_next_timer),
                            description = "Start next timer after stopping an alarm",
                            checked = settingsState.autostartNextSession,
                            onCheckedChange = { viewModel.onAction(SettingsAction.SaveAutostartNextSession(it)) }
                        )
                    }

                    // Do Not Disturb
                    item {
                        SettingsToggleCard(
                            iconRes = R.drawable.dnd,
                            title = "Do Not Disturb",
                            description = "Turn on DND when running a Focus timer",
                            checked = settingsState.dndEnabled,
                            onCheckedChange = { viewModel.onAction(SettingsAction.SaveDndEnabled(it)) }
                        )
                    }

                    // Edit Topic Card (Screenshot 13)
                    item {
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainer),
                            onClick = onEditTopic,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(colorScheme.primary)
                                )
                                Spacer(Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = "Edit topic",
                                        style = typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "Name, color and shape",
                                        style = typography.bodySmall,
                                        color = colorScheme.outline
                                    )
                                }
                            }
                        }
                    }

                    // Daily Focus Goal
                    item {
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = stringResource(R.string.daily_focus_goal),
                                        style = typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = if (settingsState.focusGoal > 0) millisecondsToHoursMinutes(settingsState.focusGoal) else "Disabled",
                                        style = typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = colorScheme.primary
                                    )
                                }
                                Spacer(Modifier.height(12.dp))
                                Slider(
                                    value = (settingsState.focusGoal / 60000).toFloat(),
                                    onValueChange = { mins ->
                                        viewModel.onAction(SettingsAction.SaveFocusGoal(mins.toLong() * 60000L))
                                    },
                                    valueRange = 0f..480f,
                                    steps = 31,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    // Always On Display
                    item {
                        SettingsToggleCard(
                            iconRes = R.drawable.aod,
                            title = stringResource(R.string.always_on_display),
                            description = "Tap anywhere when viewing the timer to switch to AOD mode",
                            checked = settingsState.aodEnabled,
                            onCheckedChange = { viewModel.onAction(SettingsAction.SaveAodEnabled(it)) }
                        )
                    }

                    // Secure AOD
                    item {
                        SettingsToggleCard(
                            iconRes = R.drawable.mobile_lock_portrait,
                            title = stringResource(R.string.secure_aod),
                            description = "Automatically lock your device after a timeout, while keeping the AOD visible",
                            checked = settingsState.secureAod,
                            onCheckedChange = { viewModel.onAction(SettingsAction.SaveSecureAod(it)) }
                        )
                    }
                }
            }
        }

        SettingsSubScreen.ALARM -> {
            Scaffold(
                topBar = {
                    TopAppBar(
                        navigationIcon = {
                            IconButton(
                                onClick = { currentSubScreen = SettingsSubScreen.MAIN },
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(colorScheme.surfaceContainerHighest)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = colorScheme.onSurface
                                )
                            }
                        },
                        title = {
                            Column {
                                Text(
                                    text = stringResource(R.string.alarm),
                                    style = typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 28.sp)
                                )
                                Text(
                                    text = stringResource(R.string.settings),
                                    style = typography.bodySmall,
                                    color = colorScheme.outline
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                },
                modifier = modifier
            ) { innerPadding ->
                LazyColumn(
                    contentPadding = PaddingValues(
                        top = innerPadding.calculateTopPadding(),
                        bottom = contentPadding.calculateBottomPadding() + 24.dp,
                        start = 16.dp,
                        end = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        SettingsToggleCard(
                            iconRes = R.drawable.alarm,
                            title = stringResource(R.string.alarm),
                            description = stringResource(R.string.alarm_desc),
                            checked = settingsState.alarmEnabled,
                            onCheckedChange = { viewModel.onAction(SettingsAction.SaveAlarmEnabled(it)) }
                        )
                    }
                    item {
                        SettingsToggleCard(
                            iconRes = R.drawable.mobile_vibrate,
                            title = stringResource(R.string.vibrate),
                            description = stringResource(R.string.vibrate_desc),
                            checked = settingsState.vibrateEnabled,
                            onCheckedChange = { viewModel.onAction(SettingsAction.SaveVibrateEnabled(it)) }
                        )
                    }
                    item {
                        SettingsToggleCard(
                            iconRes = R.drawable.music_note,
                            title = stringResource(R.string.media_volume_for_alarm),
                            description = stringResource(R.string.media_volume_for_alarm_desc),
                            checked = settingsState.mediaVolumeForAlarm,
                            onCheckedChange = { viewModel.onAction(SettingsAction.SaveMediaVolumeForAlarm(it)) }
                        )
                    }
                }
            }
        }

        SettingsSubScreen.BACKUP_RESTORE -> {
            Scaffold(
                topBar = {
                    TopAppBar(
                        navigationIcon = {
                            IconButton(
                                onClick = { currentSubScreen = SettingsSubScreen.MAIN },
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(colorScheme.surfaceContainerHighest)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = colorScheme.onSurface
                                )
                            }
                        },
                        title = {
                            Column {
                                Text(
                                    text = "Backup and restore",
                                    style = typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 28.sp)
                                )
                                Text(
                                    text = stringResource(R.string.settings),
                                    style = typography.bodySmall,
                                    color = colorScheme.outline
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                },
                modifier = modifier
            ) { innerPadding ->
                LazyColumn(
                    contentPadding = PaddingValues(
                        top = innerPadding.calculateTopPadding(),
                        bottom = contentPadding.calculateBottomPadding() + 24.dp,
                        start = 16.dp,
                        end = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainerLow),
                            onClick = { viewModel.onAction(SettingsAction.AskEraseData) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.restore_default),
                                    contentDescription = null,
                                    tint = colorScheme.error,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = stringResource(R.string.reset_data),
                                        style = typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = colorScheme.error
                                    )
                                    Text(
                                        text = stringResource(R.string.reset_data_dialog_text),
                                        style = typography.bodySmall,
                                        color = colorScheme.outline
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        else -> {}
    }

    if (isShowingEraseDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onAction(SettingsAction.CancelEraseData) },
            title = { Text(stringResource(R.string.reset_data_dialog_title)) },
            text = { Text(stringResource(R.string.reset_data_dialog_text)) },
            confirmButton = {
                TextButton(onClick = { viewModel.onAction(SettingsAction.EraseData) }) {
                    Text(stringResource(R.string.reset_data), color = colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onAction(SettingsAction.CancelEraseData) }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun SettingsNavigationItem(
    iconRes: Int,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainer),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        text = title,
                        style = typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = colorScheme.onSurface
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = typography.bodySmall,
                        color = colorScheme.outline,
                        maxLines = 1
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = colorScheme.outline
            )
        }
    }
}

@Composable
private fun DurationTile(
    label: String,
    minutes: Int,
    onMinutesChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainer),
        modifier = modifier.height(130.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Text(
                text = label,
                style = typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                color = colorScheme.outline,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "$minutes",
                style = typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 38.sp
                ),
                color = colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SettingsToggleCard(
    iconRes: Int,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f).padding(end = 12.dp)
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = if (checked) colorScheme.primary else colorScheme.outline,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        text = title,
                        style = typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = colorScheme.onSurface
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = description,
                        style = typography.bodySmall,
                        color = colorScheme.outline
                    )
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}
