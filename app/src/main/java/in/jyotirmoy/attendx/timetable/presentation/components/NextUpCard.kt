@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.jyotirmoy.attendx.timetable.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import `in`.jyotirmoy.attendx.timetable.data.model.TimeTableScheduleWithSubject

/**
 * The status band above the calendar: what is on now, or what is next.
 *
 * It used to stack the subject, the time and the room as three full-width rows
 * inside 24dp of padding, which cost about a quarter of the screen to say one
 * short thing. The time and room are secondary to the subject, so they sit
 * beside it as trailing metadata instead of below it — the hierarchy is the
 * same, the height is roughly half.
 */
@Composable
fun NextUpCard(
    nextClass: TimeTableScheduleWithSubject?,
    currentClass: TimeTableScheduleWithSubject?,
    modifier: Modifier = Modifier
) {
    val activeClass = currentClass ?: nextClass

    if (activeClass == null) {
        AllCaughtUpCard(modifier)
        return
    }

    val isNow = currentClass != null
    val containerColor = if (isNow) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }
    val contentColor = if (isNow) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSecondaryContainer
    }

    StatusCard(modifier = modifier, containerColor = containerColor) {
        // Stacks when the row would squeeze either side. Scaled by font scale
        // rather than a fixed dp, because the width available shrinks with the
        // screen while the text grows with the user's text size, and the two
        // meet well before either is extreme on its own.
        val fontScale = LocalDensity.current.fontScale
        val cardWidth = maxWidth
        val stacked = cardWidth < MIN_WIDTH_FOR_SIDE_BY_SIDE * fontScale

        if (stacked) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Headline(
                    label = if (isNow) "CLASS IN PROGRESS" else "NEXT UP",
                    subject = activeClass.subject.subject,
                    contentColor = contentColor
                )
                Metadata(
                    schedule = activeClass,
                    contentColor = contentColor,
                    alignment = Alignment.Start,
                    // Nothing is competing for the width down here, so a long
                    // room name may use a second line instead of ellipsising.
                    roomMaxLines = 2
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Headline(
                    label = if (isNow) "CLASS IN PROGRESS" else "NEXT UP",
                    subject = activeClass.subject.subject,
                    contentColor = contentColor,
                    modifier = Modifier.weight(1f)
                )
                Metadata(
                    schedule = activeClass,
                    contentColor = contentColor,
                    alignment = Alignment.End,
                    roomMaxLines = 1,
                    // Capped so a long room can never starve the subject, which
                    // is the thing the user actually came here to read.
                    modifier = Modifier.widthIn(max = cardWidth * TRAILING_WIDTH_FRACTION)
                )
            }
        }
    }
}

@Composable
private fun Headline(
    label: String,
    subject: String,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor.copy(alpha = 0.7f),
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = subject,
            style = MaterialTheme.typography.titleLargeEmphasized,
            fontWeight = FontWeight.Bold,
            color = contentColor,
            // Two lines covers the long course titles this app is full of; past
            // that the full name is a tap away on the class itself.
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun Metadata(
    schedule: TimeTableScheduleWithSubject,
    contentColor: Color,
    alignment: Alignment.Horizontal,
    roomMaxLines: Int,
    modifier: Modifier = Modifier
) {
    val room = schedule.schedule.room?.takeIf { it.isNotBlank() }

    Column(
        modifier = modifier,
        horizontalAlignment = alignment
    ) {
        Text(
            text = "${formatTime(schedule.schedule.startTime)} - " +
                formatTime(schedule.schedule.endTime),
            style = MaterialTheme.typography.titleSmall,
            color = contentColor,
            // Never broken across lines: a split time reads as two times.
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
            textAlign = if (alignment == Alignment.End) TextAlign.End else TextAlign.Start
        )

        if (room != null) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // An icon rather than the pin emoji, which sat off the baseline
                // and changed width between fonts.
                Icon(
                    imageVector = Icons.Rounded.LocationOn,
                    contentDescription = null,
                    tint = contentColor.copy(alpha = 0.8f),
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = room,
                    style = MaterialTheme.typography.labelMedium,
                    color = contentColor.copy(alpha = 0.9f),
                    maxLines = roomMaxLines,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = if (alignment == Alignment.End) TextAlign.End else TextAlign.Start
                )
            }
        }
    }
}

@Composable
private fun AllCaughtUpCard(modifier: Modifier = Modifier) {
    StatusCard(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "🎉", style = MaterialTheme.typography.titleLarge)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "All caught up!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "No more classes scheduled for today.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun StatusCard(
    modifier: Modifier,
    containerColor: Color,
    content: @Composable BoxWithConstraintsScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            content()
        }
    }
}

/** Below this (times the font scale) the band stacks rather than sits in a row. */
private val MIN_WIDTH_FOR_SIDE_BY_SIDE = 300.dp

/** Ceiling on the trailing metadata so the subject keeps the lion's share. */
private const val TRAILING_WIDTH_FRACTION = 0.4f

fun formatTime(minutes: Long): String {
    val hours = minutes / 60
    val m = minutes % 60
    val isPm = hours >= 12
    val displayHour = when {
        hours == 0L -> 12L
        hours > 12L -> hours - 12L
        else -> hours
    }
    return String.format("%d:%02d %s", displayHour, m, if (isPm) "pm" else "am")
}
