package `in`.jyotirmoy.attendx.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.unit.ColorProvider
import `in`.jyotirmoy.attendx.R
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.TextStyle
import java.util.Locale

private const val SCHEDULE_DAYS = 14L

// Glance caps a Column at 10 children, and a day's classes are one child each.
private const val MAX_CLASSES_PER_DAY = 10

private val DayColumn = 44.dp
private val PillRadius = 16.dp

// Below this the badge column would squeeze the class names; days get header rows instead.
private val BadgeLayoutWidth = 250.dp

/** One row of the agenda: either a month heading or a day with its classes. */
private sealed interface AgendaEntry {
    val key: Long

    data class Month(val date: LocalDate) : AgendaEntry {
        override val key get() = -date.toEpochDay()
    }

    data class Day(val date: LocalDate, val classes: List<ClassSlot>) : AgendaEntry {
        override val key get() = date.toEpochDay()
    }
}

/**
 * The Timetable's Schedule view on the home screen, modelled on Google Calendar's schedule
 * widget: the next two weeks as day groups of class pills, scrollable at any size. Tapping
 * a class opens it on the Timetable for that exact date; the add button opens the add sheet.
 */
class ScheduleWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val now = LocalDateTime.now()
        val initial = loadSnapshot(context)
        // "Now" and past-class styling shift at class boundaries and midnight.
        scheduleClockRefresh(context, initial.dayPlan(now), now)
        provideContent {
            val snapshot by rememberLiveSnapshot(context, initial, rescheduleClock = true)
            GlanceTheme { ScheduleBody(snapshot) }
        }
    }
}

@Composable
private fun ScheduleBody(snapshot: WidgetSnapshot) {
    val at = LocalDateTime.now()
    ScheduleContent(buildAgenda(snapshot, at.toLocalDate()), at.toLocalDate(), minuteOf(at))
}

private fun buildAgenda(snapshot: WidgetSnapshot, today: LocalDate): List<AgendaEntry> = buildList {
    var month = today.month
    for (offset in 0 until SCHEDULE_DAYS) {
        val date = today.plusDays(offset)
        val classes = snapshot.slotsOn(date)
        // Today always shows, like Google Calendar's "Nothing scheduled"; other empty days don't.
        if (date != today && classes.isEmpty()) continue
        if (date.month != month) {
            add(AgendaEntry.Month(date))
            month = date.month
        }
        add(AgendaEntry.Day(date, classes.take(MAX_CLASSES_PER_DAY)))
    }
}

class ScheduleWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = ScheduleWidget()
}

@Composable
private fun ScheduleContent(agenda: List<AgendaEntry>, today: LocalDate, minute: Long) {
    val colors = GlanceTheme.colors
    val context = LocalContext.current
    val size = LocalSize.current
    val inner = size.width - CardPadding * 2
    // Narrow widgets drop the day-badge column: each day gets a header row instead, so the
    // date stays visible and the class pills keep the full width.
    val badges = size.width >= BadgeLayoutWidth
    val pillWidth = if (badges) inner - DayColumn - Gap else inner

    WidgetCard(colors.widgetBackground, actionStartActivity(timetableIntent(context))) {
        Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            // The month heads the card; each day's own date is in its badge or header row.
            FlexText(
                today.month.getDisplayName(TextStyle.FULL, Locale.getDefault()), 20f, colors.onSurface,
                FlexStyle.Title, maxWidth = inner - IconButtonSize - Gap
            )
            Spacer(GlanceModifier.defaultWeight())
            IconButton(
                R.drawable.widget_ic_add,
                context.getString(R.string.widget_add_class),
                actionStartActivity(timetableIntent(context, addClass = true)),
                container = colors.primaryContainer,
                content = colors.onPrimaryContainer
            )
        }
        Spacer(GlanceModifier.height(10.dp))
        LazyColumn(modifier = GlanceModifier.defaultWeight().fillMaxWidth()) {
            items(agenda, itemId = { it.key }) { entry ->
                when (entry) {
                    is AgendaEntry.Month -> FlexText(
                        entry.date.month.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                        16f, colors.onSurface, FlexStyle.Title,
                        modifier = GlanceModifier.padding(top = 4.dp, bottom = 8.dp)
                    )
                    is AgendaEntry.Day -> DayGroup(entry, today, minute, pillWidth, badges)
                }
            }
        }
    }
}

@Composable
private fun DayGroup(day: AgendaEntry.Day, today: LocalDate, minute: Long, pillWidth: Dp, badges: Boolean) {
    if (!badges) {
        Column(modifier = GlanceModifier.fillMaxWidth().padding(bottom = 4.dp)) {
            DayHeader(day.date, day.date == today)
            DayClasses(day, today, minute, pillWidth)
        }
        return
    }
    Row(modifier = GlanceModifier.fillMaxWidth().padding(bottom = 4.dp)) {
        DayBadge(day.date, day.date == today)
        Spacer(GlanceModifier.width(Gap))
        DayClasses(day, today, minute, pillWidth, GlanceModifier.defaultWeight())
    }
}

/** Narrow layout's day label: "Today · Thu 29", or just "Fri 30". */
@Composable
private fun DayHeader(date: LocalDate, isToday: Boolean) {
    val colors = GlanceTheme.colors
    val context = LocalContext.current
    val label = "${date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())} ${date.dayOfMonth}"
    Row(
        modifier = GlanceModifier.fillMaxWidth().padding(bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isToday) {
            Pill(context.getString(R.string.widget_next_today), colors.primary, colors.onPrimary)
            Spacer(GlanceModifier.width(Gap))
        }
        FlexText(label, 13f, if (isToday) colors.primary else colors.onSurfaceVariant, FlexStyle.Label)
    }
}

/** A day's class pills, its own Column so a full day never exceeds Glance's 10-children limit. */
@Composable
private fun DayClasses(
    day: AgendaEntry.Day,
    today: LocalDate,
    minute: Long,
    pillWidth: Dp,
    modifier: GlanceModifier = GlanceModifier
) {
    val colors = GlanceTheme.colors
    val context = LocalContext.current
    Column(modifier = modifier) {
            if (day.classes.isEmpty()) {
                Column(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                ) {
                    Column(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .background(colors.surfaceVariant)
                            .cornerRadius(PillRadius)
                            .padding(horizontal = 12.dp, vertical = 12.dp)
                    ) {
                        FlexText(
                            context.getString(R.string.widget_nothing_scheduled), 14f,
                            colors.onSurfaceVariant, FlexStyle.Label, maxWidth = pillWidth - 24.dp
                        )
                    }
                }
            }
            day.classes.forEach { slot -> ClassPill(slot, today, minute, pillWidth) }
    }
}

@Composable
private fun DayBadge(date: LocalDate, isToday: Boolean) {
    val colors = GlanceTheme.colors
    val weekday = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    if (isToday) {
        // Today gets the filled circle, as in Google Calendar.
        Column(
            modifier = GlanceModifier
                .size(DayColumn)
                .background(colors.primary)
                .cornerRadius(DayColumn / 2),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FlexText(weekday, 10f, colors.onPrimary, FlexStyle.Label, maxWidth = DayColumn - 8.dp, autoShrink = true)
            FlexText(date.dayOfMonth.toString(), 16f, colors.onPrimary, FlexStyle.Figure)
        }
    } else {
        Column(
            modifier = GlanceModifier.width(DayColumn).padding(top = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FlexText(weekday, 11f, colors.onSurfaceVariant, FlexStyle.Label, maxWidth = DayColumn, autoShrink = true)
            FlexText(date.dayOfMonth.toString(), 18f, colors.onSurface, FlexStyle.Figure)
        }
    }
}

@Composable
private fun ClassPill(slot: ClassSlot, today: LocalDate, minute: Long, width: Dp) {
    val colors = GlanceTheme.colors
    val context = LocalContext.current
    val isToday = slot.date == today
    val running = isToday && minute >= slot.start && minute < slot.end
    val (container, content) = pillColors(slot, running, finished = isToday && minute >= slot.end)
    val details = buildString {
        if (running) append(context.getString(R.string.widget_next_now)).append(" · ")
        append(clockRange(slot))
        slot.room?.let { append(" · ").append(it) }
    }

    Column(modifier = GlanceModifier.fillMaxWidth().padding(bottom = 6.dp)) {
        Column(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(container)
                .cornerRadius(PillRadius)
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .clickable(actionStartActivity(timetableIntent(context, slot)))
        ) {
            FlexText(slot.subject.name, 14f, content, FlexStyle.Title, maxWidth = width - 24.dp)
            FlexText(details, 12f, content, FlexStyle.Body, maxWidth = width - 24.dp)
        }
    }
}

/**
 * Tonal roles instead of fixed colours, so pills follow dynamic colour and dark mode. Each
 * subject keeps the same role everywhere; the running class is filled, finished ones recede.
 */
@Composable
private fun pillColors(slot: ClassSlot, running: Boolean, finished: Boolean): Pair<ColorProvider, ColorProvider> {
    val colors = GlanceTheme.colors
    return when {
        running -> colors.primary to colors.onPrimary
        finished -> colors.surfaceVariant to colors.onSurfaceVariant
        else -> when (slot.subject.id.mod(3)) {
            0 -> colors.primaryContainer to colors.onPrimaryContainer
            1 -> colors.secondaryContainer to colors.onSecondaryContainer
            else -> colors.tertiaryContainer to colors.onTertiaryContainer
        }
    }
}
