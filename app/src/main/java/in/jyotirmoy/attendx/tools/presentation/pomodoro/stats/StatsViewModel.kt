package `in`.jyotirmoy.attendx.tools.presentation.pomodoro.stats

import androidx.compose.animation.core.spring
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patrykandpatrick.vico.compose.cartesian.VicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.VicoZoomState
import com.patrykandpatrick.vico.core.cartesian.AutoScrollCondition
import com.patrykandpatrick.vico.core.cartesian.Scroll
import com.patrykandpatrick.vico.core.cartesian.Zoom
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.jyotirmoy.attendx.tools.data.model.Stat
import `in`.jyotirmoy.attendx.tools.data.repository.StatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val statRepository: StatRepository
) : ViewModel() {

    val chartScrollStates = List(3) {
        VicoScrollState(
            scrollEnabled = true,
            initialScroll = Scroll.Absolute.End,
            autoScroll = Scroll.Absolute.End,
            autoScrollCondition = AutoScrollCondition.OnModelGrowth,
            autoScrollAnimationSpec = spring(0.8f, 380f)
        )
    }

    val chartZoomStates = List(3) {
        VicoZoomState(
            zoomEnabled = true,
            initialZoom = Zoom.fixed(),
            minZoom = Zoom.min(Zoom.Content, Zoom.fixed()),
            maxZoom = Zoom.max(Zoom.fixed(10f), Zoom.Content)
        )
    }

    val lastWeekChartProducer = CartesianChartModelProducer()
    val lastWeekXLabelKey = ExtraStore.Key<List<String>>()

    val lastMonthChartProducer = CartesianChartModelProducer()
    val lastMonthXLabelKey = ExtraStore.Key<List<String>>()

    val lastYearChartProducer = CartesianChartModelProducer()
    val lastYearXLabelKey = ExtraStore.Key<List<String>>()

    private val yearDayFormatter = DateTimeFormatter.ofPattern("d MMM")

    val todayStat: StateFlow<Stat?> = statRepository.getTodayStat()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allTimeTotalFocus: StateFlow<Long?> = statRepository.getAllTimeTotalFocusTime()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val lastWeekStatsFlow = statRepository.getLastNDaysStats(7).filter { it.isNotEmpty() }
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000), replay = 1)
    val lastMonthStatsFlow = statRepository.getLastNDaysStats(31).filter { it.isNotEmpty() }
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000), replay = 1)
    val lastYearStatsFlow = statRepository.getLastNDaysStats(365).filter { it.isNotEmpty() }
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000), replay = 1)

    val last7DaysStats: StateFlow<List<Stat>> = statRepository.getLastNDaysStats(7)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val last30DaysStats: StateFlow<List<Stat>> = statRepository.getLastNDaysStats(30)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val last365DaysStats: StateFlow<List<Stat>> = statRepository.getLastNDaysStats(365)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch(Dispatchers.Default) {
            lastWeekStatsFlow.collect { list ->
                val reversed = list.reversed()
                val keys = reversed.map {
                    it.date.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault())
                }
                val values = reversed.map { it.totalFocusTime() }

                lastWeekChartProducer.runTransaction {
                    columnSeries { series(values) }
                    extras { it[lastWeekXLabelKey] = keys }
                }
            }
        }

        viewModelScope.launch(Dispatchers.Default) {
            lastMonthStatsFlow.collect { list ->
                val reversed = list.reversed()
                val keys = reversed.map { it.date.dayOfMonth.toString() }
                val values = reversed.map { it.totalFocusTime() }

                lastMonthChartProducer.runTransaction {
                    columnSeries { series(values) }
                    extras { it[lastMonthXLabelKey] = keys }
                }
            }
        }

        viewModelScope.launch(Dispatchers.Default) {
            lastYearStatsFlow.collect { list ->
                val reversed = list.reversed()
                val keys = reversed.map { it.date.format(yearDayFormatter) }
                val values = reversed.map { it.totalFocusTime() }

                lastYearChartProducer.runTransaction {
                    lineSeries { series(values) }
                    extras { it[lastYearXLabelKey] = keys }
                }
            }
        }
    }

    val lastYearMaxFocus: StateFlow<Long> = lastYearStatsFlow
        .map { list ->
            list.maxByOrNull { it.totalFocusTime() }?.totalFocusTime() ?: Long.MAX_VALUE
        }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Long.MAX_VALUE
        )

    val lastWeekFocusHistoryValues: StateFlow<List<Pair<String, List<Long>>>> =
        lastWeekStatsFlow
            .map { value ->
                value.reversed().map {
                    Pair(
                        it.date.dayOfWeek.getDisplayName(
                            TextStyle.NARROW,
                            Locale.getDefault()
                        ),
                        listOf(it.focusTimeQ1, it.focusTimeQ2, it.focusTimeQ3, it.focusTimeQ4)
                    )
                }
            }
            .flowOn(Dispatchers.Default)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    val lastWeekFocusBreakdownValues: StateFlow<Pair<List<Long>, Long>> =
        statRepository.getLastNDaysAverageFocusTimes(7)
            .map {
                Pair(
                    listOf(
                        it?.focusTimeQ1 ?: 0L,
                        it?.focusTimeQ2 ?: 0L,
                        it?.focusTimeQ3 ?: 0L,
                        it?.focusTimeQ4 ?: 0L
                    ),
                    it?.breakTime ?: 0L
                )
            }
            .flowOn(Dispatchers.Default)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = Pair(listOf(0L, 0L, 0L, 0L), 0L)
            )

    val lastMonthCalendarData: StateFlow<List<Stat?>> =
        lastMonthStatsFlow
            .map { list ->
                val reversedList = list.reversed()
                buildList {
                    repeat(reversedList.first().date.dayOfWeek.value - DayOfWeek.MONDAY.value) {
                        add(null)
                    }
                    addAll(reversedList)
                }
            }
            .flowOn(Dispatchers.Default)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    val lastMonthFocusBreakdownValues: StateFlow<Pair<List<Long>, Long>> =
        statRepository.getLastNDaysAverageFocusTimes(30)
            .map {
                Pair(
                    listOf(
                        it?.focusTimeQ1 ?: 0L,
                        it?.focusTimeQ2 ?: 0L,
                        it?.focusTimeQ3 ?: 0L,
                        it?.focusTimeQ4 ?: 0L
                    ),
                    it?.breakTime ?: 0L
                )
            }
            .flowOn(Dispatchers.Default)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = Pair(listOf(0L, 0L, 0L, 0L), 0L)
            )

    val lastYearFocusHeatmapData: StateFlow<List<Stat?>> =
        lastYearStatsFlow
            .map { list ->
                val reversedList = list.reversed()
                buildList {
                    repeat(reversedList.first().date.dayOfWeek.value - DayOfWeek.MONDAY.value) {
                        add(null)
                    }
                    reversedList.indices.forEach {
                        if (it > 0 && reversedList[it].date.month != reversedList[it - 1].date.month) {
                            repeat(7) { add(null) }
                        }
                        add(reversedList[it])
                    }
                }
            }
            .flowOn(Dispatchers.Default)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    val lastYearFocusBreakdownValues: StateFlow<Pair<List<Long>, Long>> =
        statRepository.getLastNDaysAverageFocusTimes(365)
            .map {
                Pair(
                    listOf(
                        it?.focusTimeQ1 ?: 0L,
                        it?.focusTimeQ2 ?: 0L,
                        it?.focusTimeQ3 ?: 0L,
                        it?.focusTimeQ4 ?: 0L
                    ),
                    it?.breakTime ?: 0L
                )
            }
            .flowOn(Dispatchers.Default)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = Pair(listOf(0L, 0L, 0L, 0L), 0L)
            )
}
