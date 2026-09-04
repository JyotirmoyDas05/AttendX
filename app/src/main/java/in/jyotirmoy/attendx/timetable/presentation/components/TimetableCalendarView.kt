@file:OptIn(ExperimentalSharedTransitionApi::class)

package `in`.jyotirmoy.attendx.timetable.presentation.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import `in`.jyotirmoy.attendx.core.presentation.components.pager.InfiniteSwipePager
import `in`.jyotirmoy.attendx.timetable.data.model.TimeTableScheduleWithSubject
import `in`.jyotirmoy.attendx.timetable.domain.classesOn
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime

data class PositionedSchedule(
    val scheduleWithSubject: TimeTableScheduleWithSubject,
    val subColumnIndex: Int,
    val totalSubColumns: Int
)

fun calculatePositionedSchedules(schedules: List<TimeTableScheduleWithSubject>): List<PositionedSchedule> {
    if (schedules.isEmpty()) return emptyList()
    val sorted = schedules.sortedBy { it.schedule.startTime }
    val clusters = mutableListOf<MutableList<TimeTableScheduleWithSubject>>()

    for (item in sorted) {
        val overlappingCluster = clusters.firstOrNull { cluster ->
            cluster.any { existing ->
                item.schedule.startTime < existing.schedule.endTime && item.schedule.endTime > existing.schedule.startTime
            }
        }
        if (overlappingCluster != null) {
            overlappingCluster.add(item)
        } else {
            clusters.add(mutableListOf(item))
        }
    }

    val result = mutableListOf<PositionedSchedule>()
    for (cluster in clusters) {
        val totalSubColumns = cluster.size
        cluster.forEachIndexed { index, item ->
            result.add(
                PositionedSchedule(
                    scheduleWithSubject = item,
                    subColumnIndex = index,
                    totalSubColumns = totalSubColumns
                )
            )
        }
    }
    return result
}

/** Grid bounds, shared by the gutter and the columns so they cannot drift. */
val TimetableHours = (8..21).toList()
const val GRID_START_MINUTES = 8 * 60
const val GRID_END_MINUTES = 22 * 60

/**
 * Swipeable day / three-day / week grid.
 *
 * Two things are deliberately hoisted out of here. The vertical scroll position
 * and the zoom level belong to the screen, so that swiping to another day or
 * switching between Day and Week keeps you at the same time of day instead of
 * snapping back. And the time gutter sits outside the pager: it is identical on
 * every page, and rendering one per page would register the same shared-element
 * key several times over.
 *
 * @param anchorDate the date in focus; the visible window is derived from it
 * @param numDays 1, 3 or 7 — also the number of days a swipe travels
 * @param animatedVisibilityScope the view-switch transition this grid lives in,
 *   used to morph the gutter, columns and cards into the next view
 */
@Composable
fun SharedTransitionScope.TimetableCalendarView(
    weeklyClasses: Map<Int, List<TimeTableScheduleWithSubject>>,
    anchorDate: LocalDate,
    numDays: Int,
    animatedVisibilityScope: AnimatedVisibilityScope,
    verticalScrollState: ScrollState,
    hourHeight: Float,
    onHourHeightChange: (Float) -> Unit,
    onAnchorDateChange: (LocalDate) -> Unit,
    onClassClick: (TimeTableScheduleWithSubject, LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    onClassLongClick: (TimeTableScheduleWithSubject) -> Unit = {},
    onEmptySlotClick: (date: LocalDate, startMinutes: Int) -> Unit = { _, _ -> },
    currentClassId: Int? = null,
    bottomPadding: Dp = 100.dp
) {
    val timeColumnWidth = 46.dp
    val surfaceColor = MaterialTheme.colorScheme.surfaceContainerLow
    val gridDividerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)

    var currentMinutes by remember {
        val now = LocalTime.now()
        mutableIntStateOf(now.hour * 60 + now.minute)
    }
    var todayDate by remember { mutableStateOf(LocalDate.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            val now = LocalTime.now()
            todayDate = LocalDate.now()
            currentMinutes = now.hour * 60 + now.minute
            delay(10_000L)
        }
    }

    // Kept fresh for the gesture handler below, which outlives any single
    // composition and would otherwise pinch against a stale height.
    val latestHourHeight by rememberUpdatedState(hourHeight)
    val latestOnHourHeightChange by rememberUpdatedState(onHourHeightChange)

    val windowStart = CalendarPaging.windowStart(anchorDate, numDays)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(surfaceColor)
            // Pinch-to-zoom only. Single-finger drags are left untouched so the
            // pager below can read them; consuming multi-touch on the initial
            // pass stops a zoom being mistaken for a page swipe.
            //
            // The gesture block is never re-keyed, so it must not read the hour
            // height through the parameter — that value is captured once and the
            // zoom would forever scale from the height the screen opened at.
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(pass = PointerEventPass.Initial)
                    do {
                        val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                        if (event.changes.size >= 2) {
                            val zoom = event.calculateZoom()
                            if (zoom != 1f) {
                                latestOnHourHeightChange(
                                    (latestHourHeight * zoom)
                                        .coerceIn(MIN_HOUR_HEIGHT, MAX_HOUR_HEIGHT)
                                )
                            }
                            event.changes.forEach { it.consume() }
                        }
                    } while (event.changes.any { it.pressed })
                }
            }
    ) {
        InfiniteSwipePager(
            reference = windowStart,
            offsetOf = { current, base -> CalendarPaging.pageOffsetOf(current, base, numDays) },
            referenceAt = { base, offset -> CalendarPaging.windowStartAt(base, offset, numDays) },
            onReferenceChange = onAnchorDateChange,
            modifier = Modifier.fillMaxSize(),
            key = { it }
        ) { pageStart ->
            val dates = CalendarPaging.windowDates(pageStart, numDays)

            // Only the settled page takes part in the view-switch morph. Its
            // neighbours are composed ahead of time and would otherwise register
            // the same day and class keys twice over.
            val isCurrentPage = pageStart == windowStart

            Column(modifier = Modifier.fillMaxSize()) {
                TimetableDaysHeader(
                    dates = dates,
                    timeColumnWidth = timeColumnWidth,
                    today = todayDate,
                    onDayClick = onAnchorDateChange,
                    badgeModifier = { date ->
                        if (isCurrentPage) {
                            Modifier.sharedElement(
                                sharedContentState = rememberSharedContentState(
                                    key = DayHeaderKey(date)
                                ),
                                animatedVisibilityScope = animatedVisibilityScope,
                                renderInOverlayDuringTransition = false
                            )
                        } else {
                            Modifier
                        }
                    }
                )

                HorizontalDivider(color = gridDividerColor, thickness = 0.5.dp)

                Row(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
                    TimetableTimeColumn(
                        hours = TimetableHours,
                        hourHeight = hourHeight.dp,
                        width = timeColumnWidth,
                        scrollState = verticalScrollState,
                        modifier = Modifier
                            .padding(top = 4.dp, bottom = bottomPadding)
                            .then(
                                // One gutter per view, so it slides and rescales
                                // between Day and Week instead of cross-fading.
                                if (isCurrentPage) {
                                    Modifier.sharedElement(
                                        sharedContentState = rememberSharedContentState(
                                            key = TIME_COLUMN_KEY
                                        ),
                                        animatedVisibilityScope = animatedVisibilityScope,
                                        renderInOverlayDuringTransition = false
                                    )
                                } else {
                                    Modifier
                                }
                            )
                    )

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(verticalScrollState)
                            .padding(top = 4.dp, end = 4.dp, bottom = bottomPadding)
                    ) {
                        dates.forEach { date ->
                            DayColumn(
                                date = date,
                                todayDate = todayDate,
                                classes = classesOn(weeklyClasses, date),
                                hourHeight = hourHeight,
                                currentMinutes = currentMinutes,
                                currentClassId = currentClassId,
                                isCurrentPage = isCurrentPage,
                                animatedVisibilityScope = animatedVisibilityScope,
                                onClassClick = onClassClick,
                                onClassLongClick = onClassLongClick,
                                onEmptySlotClick = onEmptySlotClick,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SharedTransitionScope.DayColumn(
    date: LocalDate,
    todayDate: LocalDate,
    classes: List<TimeTableScheduleWithSubject>,
    hourHeight: Float,
    currentMinutes: Int,
    currentClassId: Int?,
    isCurrentPage: Boolean,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onClassClick: (TimeTableScheduleWithSubject, LocalDate) -> Unit,
    onClassLongClick: (TimeTableScheduleWithSubject) -> Unit,
    onEmptySlotClick: (date: LocalDate, startMinutes: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val isToday = date == todayDate
    val positionedCards = remember(classes) { calculatePositionedSchedules(classes) }
    val indicatorColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .height(hourHeight.dp * TimetableHours.size)
            // Keyed by date, so the column for a day shown in both the outgoing
            // and incoming view expands or contracts into place rather than
            // fading out and a different one fading in.
            .then(
                if (isCurrentPage) {
                    Modifier.sharedElement(
                        sharedContentState = rememberSharedContentState(key = DayColumnKey(date)),
                        animatedVisibilityScope = animatedVisibilityScope,
                        // Without this the column is lifted into the shared
                        // transition overlay, where the default ParentClip
                        // resolves to null (there is no parent shared element)
                        // and it paints over the app bar mid-transition.
                        renderInOverlayDuringTransition = false
                    )
                } else {
                    Modifier
                }
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TimetableHours.forEach { hour ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(hourHeight.dp)
                        .padding(horizontal = 1.dp, vertical = 1.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (isToday) MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.95f)
                            else MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f)
                        )
                        .clickable { onEmptySlotClick(date, hour * 60) }
                )
            }
        }

        positionedCards.forEach { posItem ->
            val item = posItem.scheduleWithSubject
            val durationMin = (item.schedule.endTime - item.schedule.startTime).coerceAtLeast(30)
            val offsetMin = item.schedule.startTime - GRID_START_MINUTES

            val topOffsetDp = (offsetMin.toFloat() / 60f) * hourHeight
            val heightDp = (durationMin.toFloat() / 60f) * hourHeight

            Box(
                modifier = Modifier
                    .offset(y = topOffsetDp.dp)
                    .fillMaxWidth()
                    .height(heightDp.dp)
                    .padding(horizontal = 1.dp, vertical = 1.dp)
                    // Keyed by class *and* date: the same weekly class appears on
                    // every page, so the id alone would collide across weeks.
                    .then(
                        if (isCurrentPage) {
                            Modifier.sharedElement(
                                sharedContentState = rememberSharedContentState(
                                    key = ClassCardKey(item.schedule.id, date)
                                ),
                                animatedVisibilityScope = animatedVisibilityScope,
                                renderInOverlayDuringTransition = false
                            )
                        } else {
                            Modifier
                        }
                    )
            ) {
                TimetableClassCard(
                    classItem = item,
                    onClick = { onClassClick(it, date) },
                    onLongClick = { onClassLongClick(it) },
                    isCurrentClass = item.schedule.id == currentClassId,
                    isOverlapping = posItem.totalSubColumns > 1,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        if (isToday && currentMinutes in GRID_START_MINUTES until GRID_END_MINUTES) {
            val offsetYDp = ((currentMinutes - GRID_START_MINUTES).toFloat() / 60f) * hourHeight
            Box(
                modifier = Modifier
                    .offset(y = (offsetYDp - 1).dp)
                    .fillMaxWidth()
                    .height(6.dp)
                    .zIndex(10f),
                contentAlignment = Alignment.CenterStart
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(indicatorColor)
                )
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(indicatorColor)
                )
            }
        }
    }
}

/**
 * Shared-element keys. Data classes rather than strings so a day column and a
 * header badge for the same date cannot accidentally match each other.
 */
private const val TIME_COLUMN_KEY = "timetable-time-column"

private data class DayColumnKey(val date: LocalDate)

private data class DayHeaderKey(val date: LocalDate)

private data class ClassCardKey(val scheduleId: Int, val date: LocalDate)

/** Zoom bounds for the hour rows, in dp. */
const val MIN_HOUR_HEIGHT = 48f
const val MAX_HOUR_HEIGHT = 150f
const val DEFAULT_HOUR_HEIGHT = 72f
