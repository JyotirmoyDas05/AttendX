package `in`.jyotirmoy.attendx.timetable.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import `in`.jyotirmoy.attendx.timetable.data.model.TimeTableScheduleWithSubject
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.abs

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

@Composable
fun TimetableCalendarView(
    weeklySchedule: Map<Int, List<TimeTableScheduleWithSubject>>,
    onClassClick: (TimeTableScheduleWithSubject) -> Unit,
    onClassLongClick: (TimeTableScheduleWithSubject) -> Unit = {},
    onEmptySlotClick: (dayOfWeek: Int, startMinutes: Int) -> Unit = { _, _ -> },
    onShiftDays: (delta: Int) -> Unit = {},
    modifier: Modifier = Modifier,
    numDays: Int = 7, // 1 (Day), 3 (Three-Day), 7 (Week)
    startDay: Int = 1,
    currentClassId: Int? = null,
    snapToNow: Boolean = false,
    onSnapConsumed: () -> Unit = {}
) {
    val displayedDays = when (numDays) {
        1 -> listOf(startDay.coerceIn(1, 7))
        3 -> (0..2).map { offset ->
            val d = (startDay + offset - 1) % 7 + 1
            if (d < 1) d + 7 else d
        }
        else -> listOf(1, 2, 3, 4, 5, 6, 7) // Full Week: Mon to Sun
    }

    val hours = (8..21).toList()
    var hourHeightDp by remember { mutableFloatStateOf(72f) }
    val timeColumnWidth = 46.dp
    val density = LocalDensity.current

    val gridStartMinutes = 8 * 60  // 08:00
    val gridEndMinutes = 22 * 60   // 22:00

    val verticalScrollState = rememberScrollState()

    val surfaceColor = MaterialTheme.colorScheme.surfaceContainerLow
    val gridDividerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
    val indicatorColor = MaterialTheme.colorScheme.primary

    // Realtime live time that ticks every 10 seconds
    var todayDow by remember { mutableIntStateOf(LocalDate.now().dayOfWeek.value) }
    var currentMinutes by remember {
        val now = LocalTime.now()
        mutableIntStateOf(now.hour * 60 + now.minute)
    }

    LaunchedEffect(Unit) {
        while (true) {
            val now = LocalTime.now()
            todayDow = LocalDate.now().dayOfWeek.value
            currentMinutes = now.hour * 60 + now.minute
            delay(10_000L)
        }
    }

    val hourHeightPx = with(density) { hourHeightDp.dp.toPx() }

    // Auto-scroll to current time on launch
    LaunchedEffect(Unit) {
        if (currentMinutes in gridStartMinutes..gridEndMinutes) {
            val scrollTarget = ((currentMinutes - gridStartMinutes).toFloat() / 60f * hourHeightPx).toInt()
            val centered = (scrollTarget - hourHeightPx).toInt().coerceAtLeast(0)
            verticalScrollState.scrollTo(centered)
        }
    }

    // Snap to now
    LaunchedEffect(snapToNow) {
        if (snapToNow) {
            if (currentMinutes in gridStartMinutes..gridEndMinutes) {
                val scrollTarget = ((currentMinutes - gridStartMinutes).toFloat() / 60f * hourHeightPx).toInt()
                val centered = (scrollTarget - hourHeightPx).toInt().coerceAtLeast(0)
                verticalScrollState.animateScrollTo(centered)
            }
            onSnapConsumed()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(surfaceColor)
            // Multi-touch pinch-to-zoom for vertical time scale granularity & horizontal swipe to shift days
            .pointerInput(numDays) {
                val swipeThresholdPx = with(density) { 72.dp.toPx() }
                awaitEachGesture {
                    var wasMultiTouch = false
                    var totalPanX = 0f
                    var totalPanY = 0f

                    awaitFirstDown(pass = PointerEventPass.Initial)
                    do {
                        val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                        if (event.changes.size >= 2) {
                            wasMultiTouch = true
                            val zoomChange = event.calculateZoom()
                            hourHeightDp = (hourHeightDp * zoomChange).coerceIn(48f, 150f)
                            event.changes.forEach { it.consume() }
                        } else if (!wasMultiTouch && numDays < 7) {
                            val pan = event.calculatePan()
                            totalPanX += pan.x
                            totalPanY += pan.y
                        }
                    } while (event.changes.any { it.pressed })

                    // Only trigger day shift if it was purely a horizontal swipe (not multi-touch/zoom and not vertical scroll)
                    if (!wasMultiTouch && numDays < 7) {
                        val absX = abs(totalPanX)
                        val absY = abs(totalPanY)
                        if (absX >= swipeThresholdPx && absX > absY * 1.8f) {
                            if (totalPanX < 0) {
                                onShiftDays(if (numDays == 1) 1 else 3)
                            } else {
                                onShiftDays(if (numDays == 1) -1 else -3)
                            }
                        }
                    }
                }
            }
    ) {
        // Days Header Row (Fixed time spacer + evenly weighted day headers)
        TimetableDaysHeader(
            dayIndices = displayedDays,
            timeColumnWidth = timeColumnWidth,
            currentDayOfWeek = todayDow,
            onDayClick = { /* Can switch focus */ }
        )

        HorizontalDivider(color = gridDividerColor, thickness = 0.5.dp)

        // Scrollable Grid Body
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // Time Column Gutter (12-hour AM/PM format matching XCalendar)
            TimetableTimeColumn(
                hours = hours,
                hourHeight = hourHeightDp.dp,
                width = timeColumnWidth,
                scrollState = verticalScrollState,
                modifier = Modifier.padding(top = 4.dp, bottom = 56.dp)
            )

            // Multi-Day Columns Area
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(verticalScrollState)
                    .padding(top = 4.dp, end = 4.dp, bottom = 56.dp)
            ) {
                displayedDays.forEach { dayOfWeek ->
                    val isToday = dayOfWeek == todayDow
                    val daySchedules = weeklySchedule[dayOfWeek] ?: emptyList()
                    val positionedCards = calculatePositionedSchedules(daySchedules)

                    // Individual Day Column (weight 1f matches XCalendar)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(hourHeightDp.dp * hours.size)
                    ) {
                        // Background Rounded Time Slot Cells
                        Column(modifier = Modifier.fillMaxSize()) {
                            hours.forEach { hour ->
                                val slotStartMin = hour * 60
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(hourHeightDp.dp)
                                        .padding(horizontal = 1.dp, vertical = 1.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (isToday) MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.95f)
                                            else MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f)
                                        )
                                        .clickable {
                                            onEmptySlotClick(dayOfWeek, slotStartMin)
                                        }
                                )
                            }
                        }

                        // Scheduled Class Cards Overlay
                        positionedCards.forEach { posItem ->
                            val item = posItem.scheduleWithSubject
                            val startTime = item.schedule.startTime
                            val endTime = item.schedule.endTime
                            val durationMin = (endTime - startTime).coerceAtLeast(30)
                            val offsetMin = startTime - gridStartMinutes

                            val topOffsetDp = (offsetMin.toFloat() / 60f) * hourHeightDp
                            val heightDp = (durationMin.toFloat() / 60f) * hourHeightDp

                            Box(
                                modifier = Modifier
                                    .offset(y = topOffsetDp.dp)
                                    .fillMaxWidth()
                                    .height(heightDp.dp)
                                    .padding(horizontal = 1.dp, vertical = 1.dp)
                            ) {
                                TimetableClassCard(
                                    classItem = item,
                                    onClick = { onClassClick(it) },
                                    onLongClick = { onClassLongClick(it) },
                                    isCurrentClass = item.schedule.id == currentClassId,
                                    isOverlapping = posItem.totalSubColumns > 1,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        // Live Current Time Indicator Marker (Neutral/Primary aligned line and centered dot)
                        if (isToday && currentMinutes in gridStartMinutes until gridEndMinutes) {
                            val offsetYDp = ((currentMinutes - gridStartMinutes).toFloat() / 60f) * hourHeightDp
                            Box(
                                modifier = Modifier
                                    .offset(y = (offsetYDp - 1).dp)
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .zIndex(10f),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                // Time beam line across today's column
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(2.dp)
                                        .background(indicatorColor)
                                )
                                // Centered circle pin
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
            }
        }
    }
}
