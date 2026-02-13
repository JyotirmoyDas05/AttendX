package `in`.jyotirmoy.attendx.timetable.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

/**
 * Google Calendar-style "Today" icon — calendar with today's date and a creased corner
 */
@Composable
fun TodayCalendarIcon(
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    val today = remember { LocalDate.now().dayOfMonth.toString() }
    val textMeasurer = rememberTextMeasurer()
    val textStyle = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = tint
    )
    val measuredText = remember(today, textStyle) {
        textMeasurer.measure(today, textStyle)
    }

    Canvas(modifier = modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        val stroke = 1.5.dp.toPx()
        val r = 3.dp.toPx() // corner radius
        val headerH = h * 0.28f // top bar height
        val foldSize = w * 0.22f // crease fold size

        // Calendar body with fold cut at bottom-right
        val bodyPath = Path().apply {
            // Start at top-left + radius
            moveTo(r, 0f)
            // Top edge
            lineTo(w - r, 0f)
            // Top-right corner
            arcTo(Rect(w - 2 * r, 0f, w, 2 * r), -90f, 90f, false)
            // Right edge down to fold
            lineTo(w, h - foldSize - r)
            // Fold diagonal
            lineTo(w - foldSize, h)
            // Bottom edge
            lineTo(r, h)
            // Bottom-left corner
            arcTo(Rect(0f, h - 2 * r, 2 * r, h), 90f, 90f, false)
            // Left edge
            lineTo(0f, r)
            // Top-left corner
            arcTo(Rect(0f, 0f, 2 * r, 2 * r), 180f, 90f, false)
            close()
        }

        drawPath(bodyPath, color = tint, style = Stroke(width = stroke))

        // Header bar
        drawLine(
            color = tint,
            start = Offset(0f, headerH),
            end = Offset(w, headerH),
            strokeWidth = stroke
        )

        // Fold crease line (diagonal from corner inward)
        val foldPath = Path().apply {
            moveTo(w - foldSize, h)
            lineTo(w - foldSize, h - foldSize)
            lineTo(w, h - foldSize)
        }
        drawPath(foldPath, color = tint, style = Stroke(width = stroke * 0.8f))

        // Date number centered in body area below header
        val bodyTop = headerH
        val bodyH = h - bodyTop
        val textX = (w - measuredText.size.width) / 2f
        val textY = bodyTop + (bodyH - measuredText.size.height) / 2f
        drawText(measuredText, topLeft = Offset(textX, textY))
    }
}
