package `in`.jyotirmoy.attendx.tools.presentation.pomodoro.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ShowChart
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TonalToggleButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import `in`.jyotirmoy.attendx.R
import `in`.jyotirmoy.attendx.tools.data.model.Stat
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.components.FocusBreakRatioVisualization
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.components.FocusBreakdownChart
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.components.FocusHistoryCalendar
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.components.HeatmapWithWeekLabels
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.components.HorizontalStackedBar
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.components.TimeColumnChart
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.components.TimeLineChart
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.stats.StatsViewModel
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.theme.NunitoFontFamily
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.theme.NunitoSansFontFamily
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.util.millisecondsToHoursMinutes
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.util.millisecondsToMinutes

enum class StatsViewMode { MAIN, THIS_WEEK, THIS_MONTH, THIS_YEAR }

val StatsViewMode.isDetail get() = this != StatsViewMode.MAIN

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroStatsScreen(
    contentPadding: PaddingValues,
    viewModel: StatsViewModel,
    modifier: Modifier = Modifier
) {
    var viewMode by remember { mutableStateOf(StatsViewMode.MAIN) }

    BackHandler(enabled = viewMode.isDetail) {
        viewMode = StatsViewMode.MAIN
    }

    val todayStat by viewModel.todayStat.collectAsStateWithLifecycle()
    val allTimeTotalFocus by viewModel.allTimeTotalFocus.collectAsStateWithLifecycle()
    val lastWeekBreakdown by viewModel.lastWeekFocusBreakdownValues.collectAsStateWithLifecycle()
    val lastMonthBreakdown by viewModel.lastMonthFocusBreakdownValues.collectAsStateWithLifecycle()
    val lastYearBreakdown by viewModel.lastYearFocusBreakdownValues.collectAsStateWithLifecycle()
    val lastWeekHistory by viewModel.lastWeekFocusHistoryValues.collectAsStateWithLifecycle()
    val lastMonthCalendarData by viewModel.lastMonthCalendarData.collectAsStateWithLifecycle()
    val lastYearHeatmapData by viewModel.lastYearFocusHeatmapData.collectAsStateWithLifecycle()
    val lastYearMaxFocus by viewModel.lastYearMaxFocus.collectAsStateWithLifecycle()

    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    val topListItemShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
    val middleListItemShape = RoundedCornerShape(4.dp)
    val bottomListItemShape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 28.dp, bottomEnd = 28.dp)
    val cardShape = RoundedCornerShape(28.dp)

    val hoursMinutesFormat = stringResource(R.string.hours_and_minutes_format)
    val hoursFormat = stringResource(R.string.hours_format)
    val minutesFormat = stringResource(R.string.minutes_format)

    AnimatedContent(
        targetState = viewMode,
        transitionSpec = {
            if (targetState.isDetail) {
                // Forward: new content fades+scales in, old fades out
                (fadeIn() + scaleIn(initialScale = 0.95f))
                    .togetherWith(fadeOut() + scaleOut(targetScale = 1.05f))
            } else {
                // Back: old content fades+scales in from slightly bigger
                (fadeIn() + scaleIn(initialScale = 1.05f))
                    .togetherWith(fadeOut() + scaleOut(targetScale = 0.95f))
            }
        },
        label = "statsNavTransition",
        modifier = modifier
    ) { mode ->
    @Suppress("UNUSED_EXPRESSION")
    when (mode) {
        StatsViewMode.MAIN -> {
            val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = stringResource(R.string.stats),
                                style = typography.headlineMedium.copy(
                                    fontFamily = NunitoSansFontFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 32.sp
                                )
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                        scrollBehavior = scrollBehavior
                    )
                },
                modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
            ) { innerPadding ->
                LazyColumn(
                    contentPadding = PaddingValues(
                        top = innerPadding.calculateTopPadding() + 4.dp,
                        bottom = contentPadding.calculateBottomPadding() + 24.dp,
                        start = 16.dp, end = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Text(
                            text = stringResource(R.string.today),
                            style = typography.titleLarge.copy(fontFamily = NunitoSansFontFamily, fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                        )
                    }

                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(colorScheme.primaryContainer)
                                    .padding(18.dp)
                            ) {
                                Column {
                                    Text(
                                        text = stringResource(R.string.focus),
                                        style = typography.titleMedium.copy(
                                            fontFamily = NunitoFontFamily,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = colorScheme.onPrimaryContainer
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = millisecondsToHoursMinutes(
                                            todayStat?.totalFocusTime() ?: 0L,
                                            hoursMinutesFormat
                                        ),
                                        style = typography.displaySmall.copy(
                                            fontFamily = NunitoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 28.sp
                                        ),
                                        color = colorScheme.onPrimaryContainer,
                                        maxLines = 1
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(colorScheme.tertiaryContainer)
                                    .padding(18.dp)
                            ) {
                                Column {
                                    Text(
                                        text = stringResource(R.string.break_),
                                        style = typography.titleMedium.copy(
                                            fontFamily = NunitoFontFamily,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = colorScheme.onTertiaryContainer
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = millisecondsToHoursMinutes(
                                            todayStat?.totalBreakTime() ?: 0L,
                                            hoursMinutesFormat
                                        ),
                                        style = typography.displaySmall.copy(
                                            fontFamily = NunitoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 28.sp
                                        ),
                                        color = colorScheme.onTertiaryContainer,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    // This Week Card
                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(topListItemShape)
                                .background(colorScheme.surfaceContainerHighest)
                                .clickable { viewMode = StatsViewMode.THIS_WEEK }
                                .padding(start = 20.dp, top = 20.dp, bottom = 20.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = stringResource(R.string.last_week),
                                    style = typography.titleLarge.copy(
                                        fontFamily = NunitoSansFontFamily,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(Modifier.weight(1f))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ShowChart,
                                    contentDescription = null,
                                    tint = colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(end = 20.dp).size(22.dp)
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = millisecondsToHoursMinutes(
                                        lastWeekBreakdown.first.sum(),
                                        hoursMinutesFormat
                                    ),
                                    style = typography.displaySmall.copy(
                                        fontFamily = NunitoFontFamily,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = stringResource(R.string.focus_per_day_avg),
                                    style = typography.titleSmall.copy(fontFamily = NunitoFontFamily),
                                    color = colorScheme.outline,
                                    modifier = Modifier.padding(bottom = 5.2.dp)
                                )
                            }

                            TimeColumnChart(
                                modelProducer = viewModel.lastWeekChartProducer,
                                hoursFormat = hoursFormat,
                                hoursMinutesFormat = hoursMinutesFormat,
                                minutesFormat = minutesFormat,
                                xValueFormatter = remember(viewModel.lastWeekXLabelKey) {
                                    CartesianValueFormatter { context, x, _ ->
                                        context.model.extraStore[viewModel.lastWeekXLabelKey][x.toInt()]
                                    }
                                },
                                goal = 0,
                                zoomState = viewModel.chartZoomStates[0],
                                scrollState = viewModel.chartScrollStates[0]
                            )
                        }
                    }

                    // This Month Card
                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(middleListItemShape)
                                .background(colorScheme.surfaceContainerHighest)
                                .clickable { viewMode = StatsViewMode.THIS_MONTH }
                                .padding(start = 20.dp, top = 20.dp, bottom = 20.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = stringResource(R.string.last_month),
                                    style = typography.titleLarge.copy(
                                        fontFamily = NunitoSansFontFamily,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(Modifier.weight(1f))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ShowChart,
                                    contentDescription = null,
                                    tint = colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(end = 20.dp).size(22.dp)
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = millisecondsToHoursMinutes(
                                        lastMonthBreakdown.first.sum(),
                                        hoursMinutesFormat
                                    ),
                                    style = typography.displaySmall.copy(
                                        fontFamily = NunitoFontFamily,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = stringResource(R.string.focus_per_day_avg),
                                    style = typography.titleSmall.copy(fontFamily = NunitoFontFamily),
                                    color = colorScheme.outline,
                                    modifier = Modifier.padding(bottom = 5.2.dp)
                                )
                            }

                            TimeColumnChart(
                                modelProducer = viewModel.lastMonthChartProducer,
                                hoursFormat = hoursFormat,
                                hoursMinutesFormat = hoursMinutesFormat,
                                minutesFormat = minutesFormat,
                                thickness = 8.dp,
                                xValueFormatter = remember(viewModel.lastMonthXLabelKey) {
                                    CartesianValueFormatter { context, x, _ ->
                                        context.model.extraStore[viewModel.lastMonthXLabelKey][x.toInt()]
                                    }
                                },
                                goal = 0,
                                zoomState = viewModel.chartZoomStates[1],
                                scrollState = viewModel.chartScrollStates[1]
                            )
                        }
                    }

                    // This Year Card
                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(bottomListItemShape)
                                .background(colorScheme.surfaceContainerHighest)
                                .clickable { viewMode = StatsViewMode.THIS_YEAR }
                                .padding(start = 20.dp, top = 20.dp, bottom = 20.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = stringResource(R.string.last_year),
                                    style = typography.titleLarge.copy(
                                        fontFamily = NunitoSansFontFamily,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(Modifier.weight(1f))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ShowChart,
                                    contentDescription = null,
                                    tint = colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(end = 20.dp).size(22.dp)
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = millisecondsToHoursMinutes(
                                        lastYearBreakdown.first.sum(),
                                        hoursMinutesFormat
                                    ),
                                    style = typography.displaySmall.copy(
                                        fontFamily = NunitoFontFamily,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = stringResource(R.string.focus_per_day_avg),
                                    style = typography.titleSmall.copy(fontFamily = NunitoFontFamily),
                                    color = colorScheme.outline,
                                    modifier = Modifier.padding(bottom = 5.2.dp)
                                )
                            }

                            TimeLineChart(
                                modelProducer = viewModel.lastYearChartProducer,
                                hoursFormat = hoursFormat,
                                hoursMinutesFormat = hoursMinutesFormat,
                                minutesFormat = minutesFormat,
                                xValueFormatter = remember(viewModel.lastYearXLabelKey) {
                                    CartesianValueFormatter { context, x, _ ->
                                        context.model.extraStore[viewModel.lastYearXLabelKey][x.toInt()]
                                    }
                                },
                                goal = 0,
                                zoomState = viewModel.chartZoomStates[2],
                                scrollState = viewModel.chartScrollStates[2]
                            )
                        }
                    }

                    item { Spacer(Modifier.height(10.dp)) }

                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(cardShape)
                                .background(colorScheme.surfaceContainerHighest)
                                .padding(20.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.lifetime),
                                style = typography.headlineSmall.copy(
                                    fontFamily = NunitoSansFontFamily,
                                    fontWeight = FontWeight.Bold
                                )
                            )

                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = millisecondsToHoursMinutes(
                                        allTimeTotalFocus ?: 0L,
                                        hoursMinutesFormat
                                    ),
                                    style = typography.displaySmall.copy(
                                        fontFamily = NunitoFontFamily,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = stringResource(R.string.total),
                                    style = typography.titleSmall.copy(fontFamily = NunitoFontFamily),
                                    color = colorScheme.outline,
                                    modifier = Modifier.padding(bottom = 5.2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        StatsViewMode.THIS_WEEK -> {
            WeekDetailScreen(
                contentPadding = contentPadding,
                viewModel = viewModel,
                focusBreakdownValues = lastWeekBreakdown,
                focusHistoryValues = lastWeekHistory,
                hoursMinutesFormat = hoursMinutesFormat,
                hoursFormat = hoursFormat,
                minutesFormat = minutesFormat,
                onBack = { viewMode = StatsViewMode.MAIN }
            )
        }

        StatsViewMode.THIS_MONTH -> {
            MonthDetailScreen(
                contentPadding = contentPadding,
                viewModel = viewModel,
                focusBreakdownValues = lastMonthBreakdown,
                calendarData = lastMonthCalendarData,
                hoursMinutesFormat = hoursMinutesFormat,
                hoursFormat = hoursFormat,
                minutesFormat = minutesFormat,
                onBack = { viewMode = StatsViewMode.MAIN }
            )
        }

        StatsViewMode.THIS_YEAR -> {
            YearDetailScreen(
                contentPadding = contentPadding,
                viewModel = viewModel,
                focusBreakdownValues = lastYearBreakdown,
                focusHeatmapData = lastYearHeatmapData,
                heatmapMaxValue = lastYearMaxFocus,
                hoursMinutesFormat = hoursMinutesFormat,
                hoursFormat = hoursFormat,
                minutesFormat = minutesFormat,
                onBack = { viewMode = StatsViewMode.MAIN }
            )
        }
    } // end when
    } // end AnimatedContent
}


// ----- Week Detail Screen -----
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WeekDetailScreen(
    contentPadding: PaddingValues,
    viewModel: StatsViewModel,
    focusBreakdownValues: Pair<List<Long>, Long>,
    focusHistoryValues: List<Pair<String, List<Long>>>,
    hoursMinutesFormat: String,
    hoursFormat: String,
    minutesFormat: String,
    onBack: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    val summaryAnalysisModelProducer = remember { CartesianChartModelProducer() }
    var breakdownChartExpanded by remember { mutableStateOf(false) }

    val rankList = remember(focusBreakdownValues) {
        val sortedIndices = focusBreakdownValues.first.indices.sortedByDescending { focusBreakdownValues.first[it] }
        val ranks = MutableList(focusBreakdownValues.first.size) { 0 }
        sortedIndices.forEachIndexed { rank, originalIndex -> ranks[originalIndex] = rank }
        ranks
    }
    val focusDuration = remember(focusBreakdownValues) { focusBreakdownValues.first.sum() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.last_week),
                        style = typography.titleLarge.copy(
                            fontFamily = NunitoSansFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                subtitle = {
                    Text(text = stringResource(R.string.stats), fontFamily = NunitoFontFamily)
                },
                navigationIcon = {
                    FilledTonalIconButton(onClick = onBack, shapes = IconButtonDefaults.shapes()) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                scrollBehavior = scrollBehavior
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = contentPadding.calculateBottomPadding() + 24.dp,
                start = 16.dp, end = 16.dp
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Spacer(Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = millisecondsToHoursMinutes(focusDuration, hoursMinutesFormat),
                        style = typography.displaySmall.copy(
                            fontFamily = NunitoFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = stringResource(R.string.focus_per_day_avg),
                        style = typography.titleSmall.copy(fontFamily = NunitoFontFamily),
                        color = colorScheme.outline,
                        modifier = Modifier.padding(bottom = 5.2.dp)
                    )
                }
            }

            item {
                TimeColumnChart(
                    modelProducer = viewModel.lastWeekChartProducer,
                    hoursFormat = hoursFormat,
                    hoursMinutesFormat = hoursMinutesFormat,
                    minutesFormat = minutesFormat,
                    xValueFormatter = remember(viewModel.lastWeekXLabelKey) {
                        CartesianValueFormatter { context, x, _ ->
                            context.model.extraStore[viewModel.lastWeekXLabelKey][x.toInt()]
                        }
                    },
                    goal = 0,
                    zoomState = viewModel.chartZoomStates[0],
                    scrollState = viewModel.chartScrollStates[0]
                )
            }

            item { Spacer(Modifier.height(8.dp)) }

            item {
                Text(
                    text = stringResource(R.string.focus_breakdown),
                    style = typography.headlineSmall.copy(
                        fontFamily = NunitoSansFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = stringResource(R.string.focus_breakdown_desc),
                    style = typography.bodySmall,
                    color = colorScheme.onSurfaceVariant
                )
            }

            item {
                HorizontalStackedBar(
                    values = focusBreakdownValues.first,
                    minutesFormat = minutesFormat,
                    hoursMinutesFormat = hoursMinutesFormat,
                    rankList = rankList
                )
            }

            item {
                Row {
                    focusBreakdownValues.first.fastForEach {
                        Text(
                            text = if (it <= 60 * 60 * 1000) millisecondsToMinutes(it, minutesFormat)
                            else millisecondsToHoursMinutes(it, hoursMinutesFormat),
                            style = typography.bodyLarge.copy(fontFamily = NunitoFontFamily),
                            textAlign = TextAlign.Center,
                            color = colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item {
                val iconRotation by animateFloatAsState(if (breakdownChartExpanded) 180f else 0f)
                Column(modifier = Modifier.fillMaxWidth()) {
                    TonalToggleButton(
                        checked = breakdownChartExpanded,
                        onCheckedChange = { breakdownChartExpanded = it },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowDown,
                            contentDescription = stringResource(R.string.more_info),
                            modifier = Modifier.rotate(iconRotation)
                        )
                        Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                        Text(stringResource(R.string.show_chart), fontFamily = NunitoFontFamily)
                    }

                    AnimatedVisibility(breakdownChartExpanded) {
                        LaunchedEffect(focusBreakdownValues.first) {
                            summaryAnalysisModelProducer.runTransaction {
                                columnSeries { series(focusBreakdownValues.first) }
                            }
                        }

                        FocusBreakdownChart(
                            modelProducer = summaryAnalysisModelProducer,
                            hoursFormat = hoursFormat,
                            minutesFormat = minutesFormat,
                            hoursMinutesFormat = hoursMinutesFormat,
                            modifier = Modifier.padding(top = 16.dp, bottom = 24.dp)
                        )
                    }
                }
            }

            item {
                Text(
                    text = stringResource(R.string.focus_break_ratio),
                    style = typography.headlineSmall.copy(
                        fontFamily = NunitoSansFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            item {
                FocusBreakRatioVisualization(
                    focusDuration = focusDuration,
                    breakDuration = focusBreakdownValues.second
                )
            }

            item { Spacer(Modifier.height(8.dp)) }

            item {
                Text(
                    text = stringResource(R.string.focus_history),
                    style = typography.headlineSmall.copy(
                        fontFamily = NunitoSansFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = stringResource(R.string.focus_history_desc),
                    style = typography.bodySmall,
                    color = colorScheme.onSurfaceVariant
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row {
                        Spacer(Modifier.width(18.dp))
                        (1..9 step 2).forEach {
                            Text(
                                text = "${it * 10}%\n|",
                                style = typography.labelSmall.copy(fontFamily = NunitoFontFamily),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    focusHistoryValues.fastForEach {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = it.first,
                                style = typography.labelSmall.copy(fontFamily = NunitoFontFamily),
                                modifier = Modifier.size(18.dp)
                            )
                            HorizontalStackedBar(
                                values = it.second,
                                minutesFormat = minutesFormat,
                                hoursMinutesFormat = hoursMinutesFormat,
                                rankList = rankList
                            )
                        }
                    }
                }
            }
        }
    }
}

// ----- Month Detail Screen -----
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MonthDetailScreen(
    contentPadding: PaddingValues,
    viewModel: StatsViewModel,
    focusBreakdownValues: Pair<List<Long>, Long>,
    calendarData: List<Stat?>,
    hoursMinutesFormat: String,
    hoursFormat: String,
    minutesFormat: String,
    onBack: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    val summaryAnalysisModelProducer = remember { CartesianChartModelProducer() }
    var breakdownChartExpanded by remember { mutableStateOf(false) }

    val rankList = remember(focusBreakdownValues) {
        val sortedIndices = focusBreakdownValues.first.indices.sortedByDescending { focusBreakdownValues.first[it] }
        val ranks = MutableList(focusBreakdownValues.first.size) { 0 }
        sortedIndices.forEachIndexed { rank, originalIndex -> ranks[originalIndex] = rank }
        ranks
    }
    val focusDuration = remember(focusBreakdownValues) { focusBreakdownValues.first.sum() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.last_month),
                        style = typography.titleLarge.copy(
                            fontFamily = NunitoSansFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                subtitle = {
                    Text(text = stringResource(R.string.stats), fontFamily = NunitoFontFamily)
                },
                navigationIcon = {
                    FilledTonalIconButton(onClick = onBack, shapes = IconButtonDefaults.shapes()) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                scrollBehavior = scrollBehavior
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = contentPadding.calculateBottomPadding() + 24.dp,
                start = 16.dp, end = 16.dp
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Spacer(Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = millisecondsToHoursMinutes(focusDuration, hoursMinutesFormat),
                        style = typography.displaySmall.copy(
                            fontFamily = NunitoFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = stringResource(R.string.focus_per_day_avg),
                        style = typography.titleSmall.copy(fontFamily = NunitoFontFamily),
                        color = colorScheme.outline,
                        modifier = Modifier.padding(bottom = 5.2.dp)
                    )
                }
            }

            item {
                TimeColumnChart(
                    modelProducer = viewModel.lastMonthChartProducer,
                    hoursFormat = hoursFormat,
                    hoursMinutesFormat = hoursMinutesFormat,
                    minutesFormat = minutesFormat,
                    xValueFormatter = remember(viewModel.lastMonthXLabelKey) {
                        CartesianValueFormatter { context, x, _ ->
                            context.model.extraStore[viewModel.lastMonthXLabelKey][x.toInt()]
                        }
                    },
                    goal = 0,
                    zoomState = viewModel.chartZoomStates[1],
                    scrollState = viewModel.chartScrollStates[1]
                )
            }

            item { Spacer(Modifier.height(8.dp)) }

            item {
                Text(
                    text = stringResource(R.string.focus_breakdown),
                    style = typography.headlineSmall.copy(
                        fontFamily = NunitoSansFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = stringResource(R.string.focus_breakdown_desc),
                    style = typography.bodySmall,
                    color = colorScheme.onSurfaceVariant
                )
            }

            item {
                HorizontalStackedBar(
                    values = focusBreakdownValues.first,
                    minutesFormat = minutesFormat,
                    hoursMinutesFormat = hoursMinutesFormat,
                    rankList = rankList
                )
            }

            item {
                Row {
                    focusBreakdownValues.first.fastForEach {
                        Text(
                            text = if (it <= 60 * 60 * 1000) millisecondsToMinutes(it, minutesFormat)
                            else millisecondsToHoursMinutes(it, hoursMinutesFormat),
                            style = typography.bodyLarge.copy(fontFamily = NunitoFontFamily),
                            textAlign = TextAlign.Center,
                            color = colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item {
                val iconRotation by animateFloatAsState(if (breakdownChartExpanded) 180f else 0f)
                Column(modifier = Modifier.fillMaxWidth()) {
                    TonalToggleButton(
                        checked = breakdownChartExpanded,
                        onCheckedChange = { breakdownChartExpanded = it },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowDown,
                            contentDescription = stringResource(R.string.more_info),
                            modifier = Modifier.rotate(iconRotation)
                        )
                        Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                        Text(stringResource(R.string.show_chart), fontFamily = NunitoFontFamily)
                    }

                    AnimatedVisibility(breakdownChartExpanded) {
                        LaunchedEffect(focusBreakdownValues.first) {
                            summaryAnalysisModelProducer.runTransaction {
                                columnSeries { series(focusBreakdownValues.first) }
                            }
                        }

                        FocusBreakdownChart(
                            modelProducer = summaryAnalysisModelProducer,
                            hoursFormat = hoursFormat,
                            minutesFormat = minutesFormat,
                            hoursMinutesFormat = hoursMinutesFormat,
                            modifier = Modifier.padding(top = 16.dp, bottom = 24.dp)
                        )
                    }
                }
            }

            item {
                Text(
                    text = stringResource(R.string.focus_break_ratio),
                    style = typography.headlineSmall.copy(
                        fontFamily = NunitoSansFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            item {
                FocusBreakRatioVisualization(
                    focusDuration = focusDuration,
                    breakDuration = focusBreakdownValues.second
                )
            }

            item { Spacer(Modifier.height(8.dp)) }

            item {
                Text(
                    text = stringResource(R.string.focus_history_calendar),
                    style = typography.headlineSmall.copy(
                        fontFamily = NunitoSansFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = stringResource(R.string.focus_history_calendar_desc),
                    style = typography.bodySmall,
                    color = colorScheme.onSurfaceVariant
                )
            }

            item {
                FocusHistoryCalendar(
                    data = calendarData,
                    averageRankList = rankList,
                    minutesFormat = minutesFormat,
                    hoursMinutesFormat = hoursMinutesFormat
                )
            }
        }
    }
}

// ----- Year Detail Screen -----
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun YearDetailScreen(
    contentPadding: PaddingValues,
    viewModel: StatsViewModel,
    focusBreakdownValues: Pair<List<Long>, Long>,
    focusHeatmapData: List<Stat?>,
    heatmapMaxValue: Long,
    hoursMinutesFormat: String,
    hoursFormat: String,
    minutesFormat: String,
    onBack: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    val summaryAnalysisModelProducer = remember { CartesianChartModelProducer() }
    var breakdownChartExpanded by remember { mutableStateOf(false) }

    val rankList = remember(focusBreakdownValues) {
        val sortedIndices = focusBreakdownValues.first.indices.sortedByDescending { focusBreakdownValues.first[it] }
        val ranks = MutableList(focusBreakdownValues.first.size) { 0 }
        sortedIndices.forEachIndexed { rank, originalIndex -> ranks[originalIndex] = rank }
        ranks
    }
    val focusDuration = remember(focusBreakdownValues) { focusBreakdownValues.first.sum() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.last_year),
                        style = typography.titleLarge.copy(
                            fontFamily = NunitoSansFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                subtitle = {
                    Text(text = stringResource(R.string.stats), fontFamily = NunitoFontFamily)
                },
                navigationIcon = {
                    FilledTonalIconButton(onClick = onBack, shapes = IconButtonDefaults.shapes()) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                scrollBehavior = scrollBehavior
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = contentPadding.calculateBottomPadding() + 24.dp,
                start = 16.dp, end = 16.dp
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Spacer(Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = millisecondsToHoursMinutes(focusDuration, hoursMinutesFormat),
                        style = typography.displaySmall.copy(
                            fontFamily = NunitoFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = stringResource(R.string.focus_per_day_avg),
                        style = typography.titleSmall.copy(fontFamily = NunitoFontFamily),
                        color = colorScheme.outline,
                        modifier = Modifier.padding(bottom = 5.2.dp)
                    )
                }
            }

            item {
                TimeLineChart(
                    modelProducer = viewModel.lastYearChartProducer,
                    hoursFormat = hoursFormat,
                    hoursMinutesFormat = hoursMinutesFormat,
                    minutesFormat = minutesFormat,
                    xValueFormatter = remember(viewModel.lastYearXLabelKey) {
                        CartesianValueFormatter { context, x, _ ->
                            context.model.extraStore[viewModel.lastYearXLabelKey][x.toInt()]
                        }
                    },
                    goal = 0,
                    zoomState = viewModel.chartZoomStates[2],
                    scrollState = viewModel.chartScrollStates[2]
                )
            }

            item { Spacer(Modifier.height(8.dp)) }

            item {
                Text(
                    text = stringResource(R.string.focus_breakdown),
                    style = typography.headlineSmall.copy(
                        fontFamily = NunitoSansFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = stringResource(R.string.focus_breakdown_desc),
                    style = typography.bodySmall,
                    color = colorScheme.onSurfaceVariant
                )
            }

            item {
                HorizontalStackedBar(
                    values = focusBreakdownValues.first,
                    minutesFormat = minutesFormat,
                    hoursMinutesFormat = hoursMinutesFormat,
                    rankList = rankList
                )
            }

            item {
                Row {
                    focusBreakdownValues.first.fastForEach {
                        Text(
                            text = if (it <= 60 * 60 * 1000) millisecondsToMinutes(it, minutesFormat)
                            else millisecondsToHoursMinutes(it, hoursMinutesFormat),
                            style = typography.bodyLarge.copy(fontFamily = NunitoFontFamily),
                            textAlign = TextAlign.Center,
                            color = colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item {
                val iconRotation by animateFloatAsState(if (breakdownChartExpanded) 180f else 0f)
                Column(modifier = Modifier.fillMaxWidth()) {
                    TonalToggleButton(
                        checked = breakdownChartExpanded,
                        onCheckedChange = { breakdownChartExpanded = it },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowDown,
                            contentDescription = stringResource(R.string.more_info),
                            modifier = Modifier.rotate(iconRotation)
                        )
                        Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                        Text(stringResource(R.string.show_chart), fontFamily = NunitoFontFamily)
                    }

                    AnimatedVisibility(breakdownChartExpanded) {
                        LaunchedEffect(focusBreakdownValues.first) {
                            summaryAnalysisModelProducer.runTransaction {
                                columnSeries { series(focusBreakdownValues.first) }
                            }
                        }

                        FocusBreakdownChart(
                            modelProducer = summaryAnalysisModelProducer,
                            hoursFormat = hoursFormat,
                            minutesFormat = minutesFormat,
                            hoursMinutesFormat = hoursMinutesFormat,
                            modifier = Modifier.padding(top = 16.dp, bottom = 24.dp)
                        )
                    }
                }
            }

            item {
                Text(
                    text = stringResource(R.string.focus_break_ratio),
                    style = typography.headlineSmall.copy(
                        fontFamily = NunitoSansFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            item {
                FocusBreakRatioVisualization(
                    focusDuration = focusDuration,
                    breakDuration = focusBreakdownValues.second
                )
            }

            item { Spacer(Modifier.height(8.dp)) }

            item {
                Text(
                    text = stringResource(R.string.focus_history_heatmap),
                    style = typography.headlineSmall.copy(
                        fontFamily = NunitoSansFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = stringResource(R.string.focus_history_heatmap_desc),
                    style = typography.bodySmall,
                    color = colorScheme.onSurfaceVariant
                )
            }

            item {
                HeatmapWithWeekLabels(
                    data = focusHeatmapData,
                    averageRankList = rankList,
                    maxValue = heatmapMaxValue,
                    minutesFormat = minutesFormat,
                    hoursMinutesFormat = hoursMinutesFormat
                )
            }
        }
    }
}
