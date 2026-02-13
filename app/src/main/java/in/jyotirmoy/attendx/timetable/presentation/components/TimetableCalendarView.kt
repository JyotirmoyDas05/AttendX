package `in`.jyotirmoy.attendx.timetable.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.jyotirmoy.attendx.core.presentation.util.bounceClick
import `in`.jyotirmoy.attendx.timetable.data.model.TimeTableScheduleWithSubject
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.LocalDate

/**
 * Google Calendar-style timetable grid view
 */
@Composable
fun TimetableCalendarView(
    weeklySchedule: Map<Int, List<TimeTableScheduleWithSubject>>,
    onClassClick: (TimeTableScheduleWithSubject) -> Unit,
    modifier: Modifier = Modifier,
    snapToNow: Boolean = false, // triggers snap when toggled
    onSnapConsumed: () -> Unit = {}
) {
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val timeSlots = listOf(
        "08:00", "09:00", "10:00", "11:00", "12:00", "13:00",
        "14:00", "15:00", "16:00", "17:00", "18:00", "19:00",
        "20:00", "21:00"
    )

    val cellWidth = 100.dp
    val hourHeight = 80.dp
    val timeColumnWidth = 52.dp
    val headerHeight = 48.dp
    val density = LocalDensity.current

    val gridStartMinutes = 8 * 60  // 08:00
    val gridEndMinutes = 22 * 60   // 22:00

    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()

    // Grid style
    val gridLineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    val surfaceColor = MaterialTheme.colorScheme.surface
    val timeLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)

    // Current time indicator
    val now = remember { LocalTime.now() }
    val todayDow = remember { LocalDate.now().dayOfWeek.value } // 1=Mon..7=Sun
    val currentMinutes = now.hour * 60 + now.minute
    val indicatorColor = MaterialTheme.colorScheme.primary

    val hourHeightPx = with(density) { hourHeight.toPx() }
    val cellWidthPx = with(density) { cellWidth.toPx() }
    val strokePx = with(density) { 0.5.dp.toPx() }
    val totalGridWidth = cellWidth * days.size
    val totalGridHeight = hourHeight * timeSlots.size

    // Auto-scroll to current time on initial composition
    LaunchedEffect(Unit) {
        if (currentMinutes in gridStartMinutes..gridEndMinutes) {
            val scrollTarget = ((currentMinutes - gridStartMinutes).toFloat() / 60f * hourHeightPx).toInt()
            // Center the current time vertically (scroll a bit above it)
            val centered = (scrollTarget - hourHeightPx).toInt().coerceAtLeast(0)
            verticalScrollState.scrollTo(centered)
        }
        // Scroll horizontally if today's column isn't fully visible
        if (todayDow in 1..6) {
            val hTarget = ((todayDow - 1) * cellWidthPx).toInt().coerceAtLeast(0)
            horizontalScrollState.scrollTo(hTarget)
        }
    }

    // Snap-to-now when triggered externally
    LaunchedEffect(snapToNow) {
        if (snapToNow) {
            if (currentMinutes in gridStartMinutes..gridEndMinutes) {
                val scrollTarget = ((currentMinutes - gridStartMinutes).toFloat() / 60f * hourHeightPx).toInt()
                val centered = (scrollTarget - hourHeightPx).toInt().coerceAtLeast(0)
                verticalScrollState.animateScrollTo(centered)
            }
            if (todayDow in 1..6) {
                val hTarget = ((todayDow - 1) * cellWidthPx).toInt().coerceAtLeast(0)
                horizontalScrollState.animateScrollTo(hTarget)
            }
            onSnapConsumed()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(surfaceColor)
    ) {
        // Day Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerHeight)
        ) {
            Spacer(modifier = Modifier.width(timeColumnWidth))

            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(horizontalScrollState)
            ) {
                days.forEachIndexed { index, day ->
                    val dayNum = index + 1
                    val isToday = dayNum == todayDow
                    Box(
                        modifier = Modifier
                            .width(cellWidth)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                            color = if (isToday) indicatorColor else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = gridLineColor, thickness = 0.5.dp)

        // Scrollable Body
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(verticalScrollState)
                .padding(top = 8.dp)
        ) {
            // Time Labels (clean, no grid lines)
            Column(modifier = Modifier.width(timeColumnWidth)) {
                timeSlots.forEach { time ->
                    Box(
                        modifier = Modifier
                            .width(timeColumnWidth)
                            .height(hourHeight)
                    ) {
                        Text(
                            text = time,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = timeLabelColor,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(end = 8.dp)
                                .offset(y = (-7).dp)
                        )
                    }
                }
            }

            // Grid + Cards + Time Indicator
            Box(
                modifier = Modifier.horizontalScroll(horizontalScrollState)
            ) {
                // Grid lines
                Box(
                    modifier = Modifier
                        .width(totalGridWidth)
                        .height(totalGridHeight)
                        .drawBehind {
                            for (i in 0..timeSlots.size) {
                                val y = i * hourHeightPx
                                drawLine(gridLineColor, Offset(0f, y), Offset(size.width, y), strokePx)
                            }
                            for (i in 0..days.size) {
                                val x = i * cellWidthPx
                                drawLine(gridLineColor, Offset(x, 0f), Offset(x, size.height), strokePx)
                            }
                        }
                )

                // Class cards
                weeklySchedule.forEach { (dayOfWeek, schedules) ->
                    val columnIndex = dayOfWeek - 1
                    if (columnIndex in days.indices) {
                        schedules.forEach { item ->
                            val startTime = item.schedule.startTime
                            val endTime = item.schedule.endTime
                            val durationMin = (endTime - startTime).coerceAtLeast(30)
                            val offsetMin = startTime - gridStartMinutes

                            val yPx = (offsetMin.toFloat() / 60f) * hourHeightPx
                            val xPx = columnIndex * cellWidthPx
                            val heightDp = hourHeight * (durationMin.toFloat() / 60f)

                            Box(
                                modifier = Modifier
                                    .offset { IntOffset(xPx.toInt(), yPx.toInt()) }
                                    .width(cellWidth)
                                    .height(heightDp)
                                    .padding(horizontal = 2.dp, vertical = 1.dp)
                            ) {
                                ClassCell(
                                    classItem = item,
                                    onClick = { onClassClick(it) },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }

                // Current time indicator line
                if (currentMinutes in gridStartMinutes until gridEndMinutes && todayDow in 1..6) {
                    val indicatorYPx = ((currentMinutes - gridStartMinutes).toFloat() / 60f) * hourHeightPx
                    val todayColumnIndex = todayDow - 1

                    // Red dot on left edge of today's column
                    Box(
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    (todayColumnIndex * cellWidthPx - with(density) { 4.dp.toPx() }).toInt(),
                                    (indicatorYPx - with(density) { 4.dp.toPx() }).toInt()
                                )
                            }
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(indicatorColor)
                    )

                    // Red line across today's column
                    Box(
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    (todayColumnIndex * cellWidthPx).toInt(),
                                    indicatorYPx.toInt()
                                )
                            }
                            .width(cellWidth)
                            .height(2.dp)
                            .background(indicatorColor)
                    )
                }
            }
        }
    }
}

@Composable
private fun ClassCell(
    classItem: TimeTableScheduleWithSubject?,
    onClick: (TimeTableScheduleWithSubject) -> Unit,
    modifier: Modifier = Modifier
) {
    if (classItem == null) return

    val scale = remember { androidx.compose.animation.core.Animatable(0.5f) }
    val alpha = remember { androidx.compose.animation.core.Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = androidx.compose.animation.core.spring(
                    dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                    stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                )
            )
        }
        launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = androidx.compose.animation.core.tween(300)
            )
        }
    }

    Card(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                this.alpha = alpha.value
            }
            .bounceClick(onClick = { onClick(classItem) }),
        colors = CardDefaults.cardColors(
            containerColor = generateSubjectColor(classItem.subject.subject).copy(alpha = 0.9f),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = classItem.subject.subject,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            classItem.schedule.room?.let { room ->
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = room,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

private fun generateSubjectColor(subjectName: String): Color {
    val colors = listOf(
        Color(0xFF7986CB), // Lavender
        Color(0xFF33B679), // Sage
        Color(0xFF8E24AA), // Grape
        Color(0xFFE67C73), // Flamingo
        Color(0xFFF6BF26), // Banana
        Color(0xFFD50000), // Tomato
        Color(0xFF039BE5), // Peacock
        Color(0xFF616161), // Graphite
        Color(0xFF3F51B5), // Blueberry
        Color(0xFF0B8043), // Basil
        Color(0xFFE4C441), // Citron
        Color(0xFF795548)  // Cocoa
    )
    return colors[kotlin.math.abs(subjectName.hashCode()) % colors.size]
}
