package `in`.jyotirmoy.attendx.tools.presentation.pomodoro.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.jyotirmoy.attendx.tools.data.model.Stat
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.theme.NunitoFontFamily
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.util.millisecondsToHoursMinutes
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.util.millisecondsToMinutes
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

// ----- Full Week Column Chart (matching TimeColumnChart) -----
@Composable
fun WeekColumnChart(
    stats: List<Stat>,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val barColor = colorScheme.secondary   // below goal = secondary
    val goalLineColor = colorScheme.primary
    val axisTextColor = colorScheme.onSurface
    val markerBg = colorScheme.inverseSurface
    val markerTextColor = colorScheme.inverseOnSurface

    val today = remember { LocalDate.now() }
    val days = remember(stats, today) {
        (6 downTo 0).map { offset ->
            val date = today.minusDays(offset.toLong())
            val stat = stats.find { it.date == date }
            val focusTime = stat?.totalFocusTime() ?: 0L
            val dayLabel = date.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault())
            Pair(dayLabel, focusTime)
        }
    }

    val maxTime = remember(days) {
        val max = days.maxOfOrNull { it.second } ?: 0L
        if (max > 0L) ((max / (6 * 60 * 1000L) + 1) * 6 * 60 * 1000L).coerceAtLeast(18 * 60 * 1000L)
        else 18 * 60 * 1000L
    }

    val yLabels = remember(maxTime) {
        val steps = 4
        (0 until steps).map { i ->
            val ms = maxTime * i / (steps - 1)
            val minutes = ms / 60000L
            if (minutes >= 60) "${minutes / 60}h" else "${minutes}m"
        }.reversed()
    }

    // Animate bars in
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(days) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, spring(dampingRatio = 0.7f, stiffness = 300f))
    }

    var selectedIndex by remember { mutableIntStateOf(-1) }

    Row(
        modifier = modifier.fillMaxWidth().height(250.dp)
    ) {
        // Y-axis labels
        Box(
            modifier = Modifier.width(40.dp).fillMaxHeight().padding(bottom = 20.dp)
        ) {
            yLabels.forEachIndexed { index, label ->
                Text(
                    text = label,
                    fontFamily = NunitoFontFamily,
                    fontSize = 10.sp,
                    color = axisTextColor.copy(alpha = 0.7f),
                    modifier = Modifier.align(
                        when (index) {
                            0 -> Alignment.TopEnd
                            yLabels.size - 1 -> Alignment.BottomEnd
                            1 -> Alignment.CenterEnd.let { Alignment.CenterEnd }
                            else -> Alignment.CenterEnd
                        }
                    )
                )
            }
        }

        // Chart canvas
        Canvas(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .pointerInput(days) {
                    detectTapGestures { offset ->
                        val slotWidth = size.width.toFloat() / days.size
                        val idx = (offset.x / slotWidth).toInt().coerceIn(0, days.size - 1)
                        selectedIndex = if (selectedIndex == idx) -1 else idx
                    }
                }
        ) {
            val chartBottom = size.height - 20.dp.toPx()
            val chartTop = 8.dp.toPx()
            val chartHeight = chartBottom - chartTop
            val totalSlots = days.size
            val slotWidth = size.width / totalSlots
            val barWidth = 36.dp.toPx().coerceAtMost(slotWidth * 0.6f)
            val cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)

            val textPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.argb(
                    (axisTextColor.alpha * 180).toInt(),
                    (axisTextColor.red * 255).toInt(),
                    (axisTextColor.green * 255).toInt(),
                    (axisTextColor.blue * 255).toInt()
                )
                textSize = 10.sp.toPx()
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
            }

            // Dashed horizontal y-axis lines
            val yStep = chartHeight / 3f
            (0..3).forEach { i ->
                val y = chartTop + yStep * i
                drawLine(
                    color = axisTextColor.copy(alpha = 0.08f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            days.forEachIndexed { index, (dayLabel, focusTime) ->
                val centerX = index * slotWidth + slotWidth / 2
                val left = centerX - barWidth / 2
                val ratio = ((focusTime.toFloat() / maxTime.toFloat()) * animProgress.value).coerceIn(0f, 1f)
                val barHeight = (chartHeight * ratio).coerceAtLeast(0f)
                val top = chartBottom - barHeight

                // Track background
                drawRoundRect(
                    color = axisTextColor.copy(alpha = 0.06f),
                    topLeft = Offset(left, chartTop),
                    size = Size(barWidth, chartHeight),
                    cornerRadius = cornerRadius
                )

                if (ratio > 0f) {
                    val isSelected = selectedIndex == index
                    drawRoundRect(
                        color = if (isSelected) goalLineColor else barColor,
                        topLeft = Offset(left, top),
                        size = Size(barWidth, barHeight),
                        cornerRadius = cornerRadius
                    )
                }

                // X-axis label
                drawContext.canvas.nativeCanvas.drawText(
                    dayLabel,
                    centerX,
                    size.height - 4.dp.toPx(),
                    textPaint
                )

                // Selected marker tooltip
                if (selectedIndex == index && focusTime > 0) {
                    val labelText = millisecondsToHoursMinutes(focusTime)
                    val markerPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.argb(
                            (markerTextColor.alpha * 255).toInt(),
                            (markerTextColor.red * 255).toInt(),
                            (markerTextColor.green * 255).toInt(),
                            (markerTextColor.blue * 255).toInt()
                        )
                        textSize = 10.sp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                    }
                    val markerWidth = markerPaint.measureText(labelText) + 16.dp.toPx()
                    val markerHeight = 22.dp.toPx()
                    val markerTop = (chartBottom - chartHeight * ((focusTime.toFloat() / maxTime.toFloat()).coerceIn(0f, 1f))) - markerHeight - 4.dp.toPx()
                    val markerLeft = (centerX - markerWidth / 2).coerceIn(0f, size.width - markerWidth)
                    drawRoundRect(
                        color = markerBg,
                        topLeft = Offset(markerLeft, markerTop),
                        size = Size(markerWidth, markerHeight),
                        cornerRadius = CornerRadius(4.dp.toPx())
                    )
                    drawContext.canvas.nativeCanvas.drawText(
                        labelText, markerLeft + markerWidth / 2,
                        markerTop + markerHeight / 2 + markerPaint.textSize / 3,
                        markerPaint
                    )
                }
            }
        }
    }
}

// ----- Full Month Column Chart (thinner bars, 30 days) -----
@Composable
fun MonthColumnChart(
    stats: List<Stat>,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val barColor = colorScheme.secondary
    val goalLineColor = colorScheme.primary
    val axisTextColor = colorScheme.onSurface
    val markerBg = colorScheme.inverseSurface
    val markerTextColor = colorScheme.inverseOnSurface

    val today = remember { LocalDate.now() }
    val days = remember(stats, today) {
        (29 downTo 0).map { offset ->
            val date = today.minusDays(offset.toLong())
            val stat = stats.find { it.date == date }
            val focusTime = stat?.totalFocusTime() ?: 0L
            val dayLabel = "${date.dayOfMonth}"
            Triple(dayLabel, focusTime, date)
        }
    }

    val maxTime = remember(days) {
        val max = days.maxOfOrNull { it.second } ?: 0L
        if (max > 0L) ((max / (6 * 60 * 1000L) + 1) * 6 * 60 * 1000L).coerceAtLeast(18 * 60 * 1000L)
        else 18 * 60 * 1000L
    }

    val yLabels = remember(maxTime) {
        val steps = 4
        (0 until steps).map { i ->
            val ms = maxTime * i / (steps - 1)
            val minutes = ms / 60000L
            if (minutes >= 60) "${minutes / 60}h" else "${minutes}m"
        }.reversed()
    }

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(days) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, spring(dampingRatio = 0.7f, stiffness = 300f))
    }

    var selectedIndex by remember { mutableIntStateOf(-1) }

    Row(modifier = modifier.fillMaxWidth().height(250.dp)) {
        Box(modifier = Modifier.width(40.dp).fillMaxHeight().padding(bottom = 20.dp)) {
            yLabels.forEachIndexed { index, label ->
                Text(
                    text = label, fontFamily = NunitoFontFamily, fontSize = 10.sp,
                    color = axisTextColor.copy(alpha = 0.7f),
                    modifier = Modifier.align(when (index) {
                        0 -> Alignment.TopEnd; yLabels.size - 1 -> Alignment.BottomEnd; else -> Alignment.CenterEnd
                    })
                )
            }
        }

        Canvas(
            modifier = Modifier.weight(1f).fillMaxHeight().pointerInput(days) {
                detectTapGestures { offset ->
                    val slotWidth = size.width.toFloat() / days.size
                    val idx = (offset.x / slotWidth).toInt().coerceIn(0, days.size - 1)
                    selectedIndex = if (selectedIndex == idx) -1 else idx
                }
            }
        ) {
            val chartBottom = size.height - 20.dp.toPx()
            val chartTop = 8.dp.toPx()
            val chartHeight = chartBottom - chartTop
            val slotWidth = size.width / days.size
            val barWidth = 6.dp.toPx().coerceAtMost(slotWidth * 0.8f)
            val cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)

            val textPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.argb(
                    (axisTextColor.alpha * 160).toInt(),
                    (axisTextColor.red * 255).toInt(),
                    (axisTextColor.green * 255).toInt(),
                    (axisTextColor.blue * 255).toInt()
                )
                textSize = 9.sp.toPx()
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
            }

            val yStep = chartHeight / 3f
            (0..3).forEach { i ->
                drawLine(color = axisTextColor.copy(alpha = 0.08f), start = Offset(0f, chartTop + yStep * i), end = Offset(size.width, chartTop + yStep * i), strokeWidth = 1.dp.toPx())
            }

            days.forEachIndexed { index, (dayLabel, focusTime, _) ->
                val centerX = index * slotWidth + slotWidth / 2
                val left = centerX - barWidth / 2
                val ratio = ((focusTime.toFloat() / maxTime.toFloat()) * animProgress.value).coerceIn(0f, 1f)
                val barHeight = (chartHeight * ratio).coerceAtLeast(0f)
                val top = chartBottom - barHeight

                drawRoundRect(color = axisTextColor.copy(alpha = 0.06f), topLeft = Offset(left, chartTop), size = Size(barWidth, chartHeight), cornerRadius = cornerRadius)

                if (ratio > 0f) {
                    drawRoundRect(
                        color = if (selectedIndex == index) goalLineColor else barColor,
                        topLeft = Offset(left, top), size = Size(barWidth, barHeight), cornerRadius = cornerRadius
                    )
                    // Only show label for focused days
                    drawContext.canvas.nativeCanvas.drawText(dayLabel, centerX, size.height - 4.dp.toPx(), textPaint)
                }

                if (selectedIndex == index && focusTime > 0) {
                    val labelText = millisecondsToHoursMinutes(focusTime)
                    val markerPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.argb((markerTextColor.alpha * 255).toInt(), (markerTextColor.red * 255).toInt(), (markerTextColor.green * 255).toInt(), (markerTextColor.blue * 255).toInt())
                        textSize = 10.sp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                    }
                    val mw = markerPaint.measureText(labelText) + 16.dp.toPx()
                    val mh = 22.dp.toPx()
                    val mt = top - mh - 4.dp.toPx()
                    val ml = (centerX - mw / 2).coerceIn(0f, size.width - mw)
                    drawRoundRect(color = markerBg, topLeft = Offset(ml, mt), size = Size(mw, mh), cornerRadius = CornerRadius(4.dp.toPx()))
                    drawContext.canvas.nativeCanvas.drawText(labelText, ml + mw / 2, mt + mh / 2 + markerPaint.textSize / 3, markerPaint)
                }
            }
        }
    }
}

// ----- Full Year Line Chart (matching TimeLineChart) -----
@Composable
fun YearLineChart(
    stats: List<Stat>,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val lineColor = colorScheme.secondary
    val axisTextColor = colorScheme.onSurface
    val markerBg = colorScheme.inverseSurface
    val markerTextColor = colorScheme.inverseOnSurface

    val today = remember { LocalDate.now() }
    val months = remember(stats, today) {
        (11 downTo 0).map { offset ->
            val monthDate = today.minusMonths(offset.toLong())
            val monthStats = stats.filter { it.date.year == monthDate.year && it.date.month == monthDate.month }
            val totalTime = monthStats.sumOf { it.totalFocusTime() }
            val label = monthDate.month.getDisplayName(TextStyle.NARROW, Locale.getDefault())
            Pair(label, totalTime)
        }
    }

    val maxTime = remember(months) {
        val max = months.maxOfOrNull { it.second } ?: 0L
        if (max > 0L) ((max / (60 * 60 * 1000L) + 1) * 60 * 60 * 1000L).coerceAtLeast(3 * 60 * 60 * 1000L)
        else 3 * 60 * 60 * 1000L
    }

    val yLabels = remember(maxTime) {
        val steps = 4
        (0 until steps).map { i ->
            val ms = maxTime * i / (steps - 1)
            val minutes = ms / 60000L
            if (minutes >= 60) "${minutes / 60}h" else "${minutes}m"
        }.reversed()
    }

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(months) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, spring(dampingRatio = 0.7f, stiffness = 200f))
    }

    var selectedIndex by remember { mutableIntStateOf(-1) }

    Row(modifier = modifier.fillMaxWidth().height(250.dp)) {
        Box(modifier = Modifier.width(40.dp).fillMaxHeight().padding(bottom = 20.dp)) {
            yLabels.forEachIndexed { index, label ->
                Text(
                    text = label, fontFamily = NunitoFontFamily, fontSize = 10.sp,
                    color = axisTextColor.copy(alpha = 0.7f),
                    modifier = Modifier.align(when (index) {
                        0 -> Alignment.TopEnd; yLabels.size - 1 -> Alignment.BottomEnd; else -> Alignment.CenterEnd
                    })
                )
            }
        }

        Canvas(
            modifier = Modifier.weight(1f).fillMaxHeight().pointerInput(months) {
                detectTapGestures { offset ->
                    val n = months.size
                    if (n < 2) return@detectTapGestures
                    val stepX = size.width / (n - 1).toFloat()
                    val idx = (offset.x / stepX).toInt().coerceIn(0, n - 1)
                    selectedIndex = if (selectedIndex == idx) -1 else idx
                }
            }
        ) {
            val chartBottom = size.height - 20.dp.toPx()
            val chartTop = 8.dp.toPx()
            val chartHeight = chartBottom - chartTop
            val n = months.size
            if (n < 2) return@Canvas
            val stepX = size.width / (n - 1).toFloat()
            val prog = animProgress.value

            val allPoints = months.mapIndexed { index, (_, time) ->
                val x = index * stepX
                val ratio = (time.toFloat() / maxTime.toFloat()).coerceIn(0f, 1f)
                val y = chartBottom - chartHeight * ratio
                Offset(x, y)
            }

            // Animate: interpolate each point toward final
            val animatedPoints = allPoints.mapIndexed { i, p ->
                val fraction = (prog * n - i).coerceIn(0f, 1f)
                Offset(p.x, chartBottom - (chartBottom - p.y) * fraction)
            }

            val textPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.argb(
                    (axisTextColor.alpha * 160).toInt(),
                    (axisTextColor.red * 255).toInt(),
                    (axisTextColor.green * 255).toInt(),
                    (axisTextColor.blue * 255).toInt()
                )
                textSize = 10.sp.toPx()
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
            }

            // Y-axis grid lines
            val yStep = chartHeight / 3f
            (0..3).forEach { i ->
                drawLine(color = axisTextColor.copy(alpha = 0.08f), start = Offset(0f, chartTop + yStep * i), end = Offset(size.width, chartTop + yStep * i), strokeWidth = 1.dp.toPx())
            }

            // Fill gradient under line
            val fillPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(animatedPoints.first().x, animatedPoints.first().y)
                for (i in 1 until animatedPoints.size) {
                    val p0 = animatedPoints[i - 1]; val p1 = animatedPoints[i]
                    val cx = (p0.x + p1.x) / 2
                    cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                }
                lineTo(animatedPoints.last().x, chartBottom)
                lineTo(animatedPoints.first().x, chartBottom)
                close()
            }
            drawPath(fillPath, brush = Brush.verticalGradient(
                colors = listOf(lineColor.copy(alpha = 0.3f), Color.Transparent),
                startY = chartTop, endY = chartBottom
            ))

            // Stroke path
            val strokePath = androidx.compose.ui.graphics.Path().apply {
                moveTo(animatedPoints.first().x, animatedPoints.first().y)
                for (i in 1 until animatedPoints.size) {
                    val p0 = animatedPoints[i - 1]; val p1 = animatedPoints[i]
                    val cx = (p0.x + p1.x) / 2
                    cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                }
            }
            drawPath(strokePath, color = lineColor, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))

            // X-axis month labels and dots
            months.forEachIndexed { index, (label, _) ->
                val x = index * stepX
                drawContext.canvas.nativeCanvas.drawText(label, x, size.height - 4.dp.toPx(), textPaint)

                if (index == selectedIndex) {
                    drawLine(
                        color = lineColor.copy(alpha = 0.4f),
                        start = Offset(x, chartTop), end = Offset(x, chartBottom),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                    )
                    drawCircle(color = lineColor, radius = 5.dp.toPx(), center = animatedPoints[index])
                    drawCircle(color = Color.White, radius = 3.dp.toPx(), center = animatedPoints[index])

                    val time = months[index].second
                    if (time > 0) {
                        val labelText = millisecondsToHoursMinutes(time)
                        val markerPaint = android.graphics.Paint().apply {
                            color = android.graphics.Color.argb((markerTextColor.alpha * 255).toInt(), (markerTextColor.red * 255).toInt(), (markerTextColor.green * 255).toInt(), (markerTextColor.blue * 255).toInt())
                            textSize = 10.sp.toPx()
                            textAlign = android.graphics.Paint.Align.CENTER
                            isAntiAlias = true
                        }
                        val mw = markerPaint.measureText(labelText) + 16.dp.toPx()
                        val mh = 22.dp.toPx()
                        val pt = animatedPoints[index]
                        val mt = pt.y - mh - 8.dp.toPx()
                        val ml = (pt.x - mw / 2).coerceIn(0f, size.width - mw)
                        drawRoundRect(color = markerBg, topLeft = Offset(ml, mt), size = Size(mw, mh), cornerRadius = CornerRadius(4.dp.toPx()))
                        drawContext.canvas.nativeCanvas.drawText(labelText, ml + mw / 2, mt + mh / 2 + markerPaint.textSize / 3, markerPaint)
                    }
                }
            }
        }
    }
}

// ----- Mini charts (kept for main stats screen summary cards) -----
@Composable
fun MiniWeekColumnChart(
    stats: List<Stat>,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val barColor = colorScheme.primary
    val barTrackColor = colorScheme.onSurface.copy(alpha = 0.05f)
    val textMutedColor = colorScheme.outline

    val today = remember { LocalDate.now() }
    val days = remember(stats, today) {
        (6 downTo 0).map { offset ->
            val date = today.minusDays(offset.toLong())
            val stat = stats.find { it.date == date }
            val focusTime = stat?.totalFocusTime() ?: 0L
            val dayLabel = date.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault())
            Pair(dayLabel, focusTime)
        }
    }
    val maxTime = remember(days) {
        val max = days.maxOfOrNull { it.second } ?: 0L
        if (max > 0L) max else 25 * 60 * 1000L
    }
    val yLabels = remember(maxTime) {
        val maxMinutes = (maxTime / 60000L).coerceAtLeast(18L)
        listOf("${maxMinutes}m", "${(maxMinutes * 2) / 3}m", "${maxMinutes / 3}m", "0m")
    }

    Row(modifier = modifier.fillMaxWidth().height(170.dp).padding(top = 12.dp, bottom = 4.dp)) {
        Box(modifier = Modifier.width(42.dp).fillMaxHeight().padding(bottom = 24.dp)) {
            Text(text = yLabels[0], fontFamily = NunitoFontFamily, fontSize = 11.sp, color = textMutedColor, modifier = Modifier.align(Alignment.TopStart))
            Text(text = yLabels[2], fontFamily = NunitoFontFamily, fontSize = 11.sp, color = textMutedColor, modifier = Modifier.align(Alignment.CenterStart))
            Text(text = yLabels[3], fontFamily = NunitoFontFamily, fontSize = 11.sp, color = textMutedColor, modifier = Modifier.align(Alignment.BottomStart))
        }
        Canvas(modifier = Modifier.weight(1f).fillMaxHeight()) {
            val canvasWidth = size.width; val canvasHeight = size.height
            val chartBottom = canvasHeight - 24.dp.toPx(); val chartHeight = chartBottom - 8.dp.toPx()
            val slotWidth = canvasWidth / 7; val barWidth = 32.dp.toPx().coerceAtMost(slotWidth * 0.7f)
            val cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
            val textPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.argb((textMutedColor.alpha * 255).toInt(), (textMutedColor.red * 255).toInt(), (textMutedColor.green * 255).toInt(), (textMutedColor.blue * 255).toInt())
                textSize = 12.sp.toPx(); textAlign = android.graphics.Paint.Align.CENTER; isAntiAlias = true
            }
            days.forEachIndexed { index, (dayLabel, focusTime) ->
                val centerX = index * slotWidth + slotWidth / 2; val left = centerX - barWidth / 2
                drawRoundRect(color = barTrackColor, topLeft = Offset(left, 8.dp.toPx()), size = Size(barWidth, chartHeight), cornerRadius = cornerRadius)
                val ratio = (focusTime.toFloat() / maxTime.toFloat()).coerceIn(0f, 1f)
                if (ratio > 0f) {
                    val barH = (chartHeight * ratio).coerceAtLeast(barWidth)
                    drawRoundRect(color = barColor, topLeft = Offset(left, chartBottom - barH), size = Size(barWidth, barH), cornerRadius = cornerRadius)
                }
                drawContext.canvas.nativeCanvas.drawText(dayLabel, centerX, canvasHeight - 4.dp.toPx(), textPaint)
            }
        }
    }
}

@Composable
fun MiniMonthColumnChart(
    stats: List<Stat>,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val barColor = colorScheme.primary
    val barTrackColor = colorScheme.onSurface.copy(alpha = 0.05f)
    val textMutedColor = colorScheme.outline

    val today = remember { LocalDate.now() }
    val days = remember(stats, today) {
        (29 downTo 0).map { offset ->
            val date = today.minusDays(offset.toLong())
            val stat = stats.find { it.date == date }
            Pair("${date.dayOfMonth}", stat?.totalFocusTime() ?: 0L)
        }
    }
    val maxTime = remember(days) { (days.maxOfOrNull { it.second } ?: 0L).coerceAtLeast(25 * 60 * 1000L) }
    val yLabels = remember(maxTime) {
        val mm = (maxTime / 60000L).coerceAtLeast(18L)
        listOf("${mm}m", "${mm / 2}m", "0m")
    }

    Row(modifier = modifier.fillMaxWidth().height(170.dp).padding(top = 12.dp, bottom = 4.dp)) {
        Box(modifier = Modifier.width(42.dp).fillMaxHeight().padding(bottom = 24.dp)) {
            Text(text = yLabels[0], fontFamily = NunitoFontFamily, fontSize = 11.sp, color = textMutedColor, modifier = Modifier.align(Alignment.TopStart))
            Text(text = yLabels[2], fontFamily = NunitoFontFamily, fontSize = 11.sp, color = textMutedColor, modifier = Modifier.align(Alignment.BottomStart))
        }
        Canvas(modifier = Modifier.weight(1f).fillMaxHeight()) {
            val canvasWidth = size.width; val canvasHeight = size.height
            val chartBottom = canvasHeight - 24.dp.toPx(); val chartHeight = chartBottom - 8.dp.toPx()
            val slotWidth = canvasWidth / 30; val barWidth = 6.dp.toPx().coerceAtMost(slotWidth * 0.75f)
            val cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
            val textPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.argb((textMutedColor.alpha * 255).toInt(), (textMutedColor.red * 255).toInt(), (textMutedColor.green * 255).toInt(), (textMutedColor.blue * 255).toInt())
                textSize = 9.sp.toPx(); textAlign = android.graphics.Paint.Align.CENTER; isAntiAlias = true
            }
            days.forEachIndexed { index, (dayLabel, focusTime) ->
                val centerX = index * slotWidth + slotWidth / 2; val left = centerX - barWidth / 2
                drawRoundRect(color = barTrackColor, topLeft = Offset(left, 8.dp.toPx()), size = Size(barWidth, chartHeight), cornerRadius = cornerRadius)
                val ratio = (focusTime.toFloat() / maxTime.toFloat()).coerceIn(0f, 1f)
                if (ratio > 0f) {
                    val barH = (chartHeight * ratio).coerceAtLeast(barWidth)
                    drawRoundRect(color = barColor, topLeft = Offset(left, chartBottom - barH), size = Size(barWidth, barH), cornerRadius = cornerRadius)
                    drawContext.canvas.nativeCanvas.drawText(dayLabel, centerX, canvasHeight - 4.dp.toPx(), textPaint)
                }
            }
        }
    }
}

@Composable
fun MiniYearLineChart(
    stats: List<Stat>,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val lineColor = colorScheme.primary
    val textMutedColor = colorScheme.outline

    val today = remember { LocalDate.now() }
    val months = remember(stats, today) {
        (11 downTo 0).map { offset ->
            val monthDate = today.minusMonths(offset.toLong())
            val monthStats = stats.filter { it.date.year == monthDate.year && it.date.month == monthDate.month }
            val totalTime = monthStats.sumOf { it.totalFocusTime() }
            val label = monthDate.month.getDisplayName(TextStyle.NARROW, Locale.getDefault())
            Pair(label, totalTime)
        }
    }
    val maxTime = remember(months) { (months.maxOfOrNull { it.second } ?: 0L).coerceAtLeast(60 * 60 * 1000L) }
    val yLabels = remember(maxTime) {
        val mh = (maxTime / 3600000L).coerceAtLeast(18L)
        listOf("${mh}h", "${mh / 2}h", "0h")
    }

    Row(modifier = modifier.fillMaxWidth().height(170.dp).padding(top = 12.dp, bottom = 4.dp)) {
        Box(modifier = Modifier.width(42.dp).fillMaxHeight().padding(bottom = 24.dp)) {
            Text(text = yLabels[0], fontFamily = NunitoFontFamily, fontSize = 11.sp, color = textMutedColor, modifier = Modifier.align(Alignment.TopStart))
            Text(text = yLabels[2], fontFamily = NunitoFontFamily, fontSize = 11.sp, color = textMutedColor, modifier = Modifier.align(Alignment.BottomStart))
        }
        Canvas(modifier = Modifier.weight(1f).fillMaxHeight()) {
            val canvasWidth = size.width; val canvasHeight = size.height
            val chartBottom = canvasHeight - 24.dp.toPx(); val chartHeight = chartBottom - 8.dp.toPx()
            val n = months.size; if (n < 2) return@Canvas
            val stepX = canvasWidth / (n - 1)
            val points = months.mapIndexed { i, (_, t) ->
                Offset(i * stepX, chartBottom - (chartHeight * (t.toFloat() / maxTime.toFloat()).coerceIn(0f, 1f)))
            }
            val strokePath = androidx.compose.ui.graphics.Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    val p0 = points[i - 1]; val p1 = points[i]; val cx = (p0.x + p1.x) / 2
                    cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                }
            }
            val fillPath = androidx.compose.ui.graphics.Path().apply {
                addPath(strokePath)
                lineTo(points.last().x, chartBottom); lineTo(points.first().x, chartBottom); close()
            }
            drawPath(fillPath, brush = Brush.verticalGradient(colors = listOf(lineColor.copy(alpha = 0.25f), Color.Transparent), startY = 8.dp.toPx(), endY = chartBottom))
            drawPath(strokePath, color = lineColor, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
            val textPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.argb((textMutedColor.alpha * 255).toInt(), (textMutedColor.red * 255).toInt(), (textMutedColor.green * 255).toInt(), (textMutedColor.blue * 255).toInt())
                textSize = 10.sp.toPx(); textAlign = android.graphics.Paint.Align.CENTER; isAntiAlias = true
            }
            months.forEachIndexed { i, (label, _) ->
                if (i % 2 == 0) drawContext.canvas.nativeCanvas.drawText(label, i * stepX, canvasHeight - 4.dp.toPx(), textPaint)
            }
        }
    }
}
