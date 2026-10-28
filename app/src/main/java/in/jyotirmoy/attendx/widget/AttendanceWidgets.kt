package `in`.jyotirmoy.attendx.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.Action
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.ColumnScope
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.unit.ColorProvider
import `in`.jyotirmoy.attendx.MainActivity
import `in`.jyotirmoy.attendx.R
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

private data class OverallStats(val present: Int, val absent: Int, val subjects: Int) {
    val total get() = present + absent
    val fraction get() = if (total == 0) 0f else present.toFloat() / total
}

class OverallAttendanceWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val initial = loadSnapshot(context)
        provideContent {
            val snapshot by rememberLiveSnapshot(context, initial)
            GlanceTheme { OverallBody(snapshot) }
        }
    }
}

@Composable
private fun OverallBody(snapshot: WidgetSnapshot) {
    val subjects = snapshot.subjects.values.toList()
    val stats = OverallStats(subjects.sumOf { it.present }, subjects.sumOf { it.absent }, subjects.size)
    // Lowest attendance first, so the subjects that need attention lead the list.
    val ranked = subjects.sortedWith(compareBy<SubjectStats>({ it.total == 0 }, { it.fraction }))
    OverallContent(stats, ranked, snapshot.marks.take(30))
}

class OverallAttendanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = OverallAttendanceWidget()
}

class SubjectAttendanceWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Exact
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val now = LocalDateTime.now()
        val initial = loadSnapshot(context)
        // The "Next class" tile goes stale when that class ends, which is a clock boundary.
        scheduleClockRefresh(context, initial.dayPlan(now), now)
        val configIntent = Intent(context, SubjectWidgetConfigActivity::class.java).putExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            GlanceAppWidgetManager(context).getAppWidgetId(id)
        )
        provideContent {
            // Read in composition: the configure screen saves while the new widget's session is still live.
            val subjectId = currentState(SubjectIdKey)
            val snapshot by rememberLiveSnapshot(context, initial)
            GlanceTheme { SubjectBody(snapshot, subjectId, configIntent) }
        }
    }
}

@Composable
private fun SubjectBody(snapshot: WidgetSnapshot, subjectId: Int?, configIntent: Intent) {
    val stats = subjectId?.let { snapshot.subjects[it] }
    val upcoming = stats?.let { snapshot.upcomingOf(it.id, LocalDateTime.now()) }.orEmpty()
    val history = stats?.let { snapshot.historyOf(it.id) }.orEmpty()
    SubjectContent(stats, configIntent, upcoming, history)
}

class SubjectAttendanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = SubjectAttendanceWidget()
}

class NextClassWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val now = LocalDateTime.now()
        val initial = loadSnapshot(context)
        scheduleClockRefresh(context, initial.dayPlan(now), now)
        provideContent {
            val snapshot by rememberLiveSnapshot(context, initial, rescheduleClock = true)
            GlanceTheme { NextClassBody(snapshot) }
        }
    }
}

@Composable
private fun NextClassBody(snapshot: WidgetSnapshot) {
    // Derived at composition time, so every re-render reflects the current minute.
    val at = LocalDateTime.now()
    val plan = snapshot.dayPlan(at)
    NextClassContent(plan, snapshot.comingUp(plan), at.toLocalDate())
}

class NextClassWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = NextClassWidget()
}

/** Hero figure size steps down on short widgets so the bento row still fits. */
private fun heroSize(height: Dp, base: Float): Float = when {
    height >= 200.dp -> base
    height >= 170.dp -> base - 10f
    else -> base - 20f
}

/** Two columns side by side, each gets half the card minus the gutter. */
private fun columnWidth(inner: Dp) = (inner - Gap * 2) / 2

/**
 * Lays a widget out for its size. Compact shows the summary alone. Taller adds a list
 * under it, wider puts a list beside it, and big gets lists in both columns, so stretching
 * a widget in any direction reveals more instead of leaving empty space.
 */
@Composable
private fun ColumnScope.Adaptive(
    summary: @Composable ColumnScope.(width: Dp, pushDown: Boolean) -> Unit,
    below: @Composable ColumnScope.(width: Dp) -> Unit,
    side: @Composable ColumnScope.(width: Dp) -> Unit,
    belowInBig: @Composable ColumnScope.(width: Dp) -> Unit = below,
    sideInBig: @Composable ColumnScope.(width: Dp) -> Unit = side
) {
    val size = LocalSize.current
    val inner = size.width - CardPadding * 2
    val column = columnWidth(inner)
    when {
        size.isWide && size.isTall -> Row(modifier = GlanceModifier.fillMaxSize()) {
            Column(modifier = GlanceModifier.defaultWeight().fillMaxHeight()) {
                summary(column, false)
                Spacer(GlanceModifier.height(12.dp))
                belowInBig(column)
            }
            Spacer(GlanceModifier.width(Gap * 2))
            Column(modifier = GlanceModifier.defaultWeight().fillMaxHeight()) { sideInBig(column) }
        }
        size.isWide -> Row(modifier = GlanceModifier.fillMaxSize()) {
            Column(modifier = GlanceModifier.defaultWeight().fillMaxHeight()) { summary(column, true) }
            Spacer(GlanceModifier.width(Gap * 2))
            Column(modifier = GlanceModifier.defaultWeight().fillMaxHeight()) { side(column) }
        }
        size.isTall -> {
            summary(inner, false)
            Spacer(GlanceModifier.height(12.dp))
            below(inner)
        }
        else -> summary(inner, true)
    }
}

// ---------------------------------------------------------------- Overall

@Composable
private fun OverallContent(stats: OverallStats, ranked: List<SubjectStats>, recent: List<Mark>) {
    val colors = GlanceTheme.colors
    val context = LocalContext.current
    val onCard = colors.onPrimaryContainer

    WidgetCard(colors.primaryContainer, actionStartActivity<MainActivity>()) {
        Adaptive(
            summary = { width, pushDown -> OverallSummary(stats, width, pushDown) },
            below = { width -> SubjectList(ranked, width) },
            side = { width -> SubjectList(ranked, width) },
            belowInBig = { width -> MarkList(context.getString(R.string.widget_recent), recent, width, onCard, showSubject = true) }
        )
    }
}

@Composable
private fun ColumnScope.OverallSummary(stats: OverallStats, width: Dp, pushTilesDown: Boolean) {
    val colors = GlanceTheme.colors
    val context = LocalContext.current
    val tile = (width - Gap) / 2

    Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        FlexText(context.getString(R.string.widget_overall_title), 14f, colors.onPrimaryContainer, FlexStyle.Label)
        Spacer(GlanceModifier.defaultWeight())
        if (stats.subjects > 0) {
            FlexText(
                context.resources.getQuantityString(R.plurals.widget_subjects, stats.subjects, stats.subjects),
                12f, colors.onPrimaryContainer, FlexStyle.Body
            )
        }
    }
    FlexText(
        if (stats.total == 0) "—" else "${(stats.fraction * 100).roundToInt()}%",
        heroSize(LocalSize.current.height, 56f), colors.onPrimaryContainer, FlexStyle.Display
    )
    WavyProgress(stats.fraction, width, fill = colors.primary, track = colors.surfaceVariant)
    Spacer(if (pushTilesDown) GlanceModifier.defaultWeight() else GlanceModifier.height(10.dp))
    Row(modifier = GlanceModifier.fillMaxWidth()) {
        StatTile(
            stats.present.toString(), context.getString(R.string.widget_tile_present),
            colors.primary, tile, GlanceModifier.defaultWeight()
        )
        Spacer(GlanceModifier.width(Gap))
        StatTile(
            stats.absent.toString(), context.getString(R.string.widget_tile_absent),
            colors.error, tile, GlanceModifier.defaultWeight()
        )
    }
}

/** Scrollable per-subject breakdown; each row opens that subject's calendar. */
@Composable
private fun ColumnScope.SubjectList(subjects: List<SubjectStats>, width: Dp) {
    val colors = GlanceTheme.colors
    val context = LocalContext.current
    SectionLabel(context.getString(R.string.widget_by_subject), colors.onPrimaryContainer)
    LazyColumn(modifier = GlanceModifier.defaultWeight().fillMaxWidth()) {
        items(subjects, itemId = { it.id.toLong() }) { subject -> SubjectRow(subject, width) }
    }
}

@Composable
private fun SubjectRow(subject: SubjectStats, width: Dp) {
    val colors = GlanceTheme.colors
    val context = LocalContext.current
    val accent = if (subject.belowTarget) colors.error else colors.primary
    Column(modifier = GlanceModifier.fillMaxWidth().padding(bottom = 6.dp)) {
        Column(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(colors.surface)
                .cornerRadius(InnerRadius)
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .clickable(actionStartActivity(calendarIntent(context, subject)))
        ) {
            Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                FlexText(subject.name, 13f, colors.onSurface, FlexStyle.Title, maxWidth = width - 24.dp - 52.dp)
                Spacer(GlanceModifier.defaultWeight())
                FlexText(subject.percentText, 14f, accent, FlexStyle.Figure)
            }
            Spacer(GlanceModifier.height(6.dp))
            LinearProgressIndicator(
                progress = subject.fraction,
                modifier = GlanceModifier.fillMaxWidth().height(4.dp),
                color = accent,
                backgroundColor = colors.surfaceVariant
            )
        }
    }
}

// ---------------------------------------------------------------- Subject

@Composable
private fun SubjectContent(
    stats: SubjectStats?,
    configIntent: Intent,
    upcoming: List<ClassSlot>,
    history: List<Mark>
) {
    val colors = GlanceTheme.colors
    val context = LocalContext.current
    val inner = LocalSize.current.width - CardPadding * 2

    if (stats == null) {
        // Not configured yet, or the subject was deleted: the whole tile opens the picker.
        WidgetCard(colors.surfaceVariant, actionStartActivity(configIntent)) {
            Spacer(GlanceModifier.defaultWeight())
            FlexText(
                context.getString(R.string.widget_subject_pick), 18f, colors.onSurfaceVariant,
                FlexStyle.Title, maxWidth = inner, maxLines = 3
            )
            Spacer(GlanceModifier.defaultWeight())
        }
        return
    }

    // Expressive status roles: primaryContainer when on track, errorContainer when below target.
    val container = if (stats.belowTarget) colors.errorContainer else colors.primaryContainer
    val onContainer = if (stats.belowTarget) colors.onErrorContainer else colors.onPrimaryContainer
    val recentLabel = context.getString(R.string.widget_recent)
    WidgetCard(container, actionStartActivity(calendarIntent(context, stats))) {
        Adaptive(
            summary = { width, pushDown -> SubjectSummary(stats, configIntent, width, pushDown) },
            below = { width ->
                NextClassTile(upcoming.firstOrNull(), width)
                Spacer(GlanceModifier.height(12.dp))
                MarkList(recentLabel, history, width, onContainer, showSubject = false)
            },
            side = { width ->
                NextClassTile(upcoming.firstOrNull(), width)
                Spacer(GlanceModifier.height(12.dp))
                MarkList(recentLabel, history, width, onContainer, showSubject = false)
            },
            // Big: the summary's column lists every coming class; the other column is history.
            belowInBig = { width -> UpcomingList(upcoming, width, onContainer) },
            sideInBig = { width -> MarkList(recentLabel, history, width, onContainer, showSubject = false) }
        )
    }
}

@Composable
private fun ColumnScope.SubjectSummary(stats: SubjectStats, configIntent: Intent, width: Dp, pushTilesDown: Boolean) {
    val colors = GlanceTheme.colors
    val context = LocalContext.current
    val onContainer = if (stats.belowTarget) colors.onErrorContainer else colors.onPrimaryContainer
    val accent = if (stats.belowTarget) colors.error else colors.primary
    val tile = (width - Gap) / 2
    val (statusValue, statusLabel) = stats.status(context)

    Row(modifier = GlanceModifier.fillMaxWidth()) {
        FlexText(
            stats.name, 15f, onContainer, FlexStyle.Title,
            maxWidth = width - IconButtonSize - 4.dp, maxLines = 2
        )
        Spacer(GlanceModifier.defaultWeight())
        IconButton(R.drawable.settings, context.getString(R.string.widget_change), actionStartActivity(configIntent))
    }
    FlexText(stats.percentText, heroSize(LocalSize.current.height, 52f), onContainer, FlexStyle.Display)
    WavyProgress(stats.fraction, width, fill = accent, track = colors.surfaceVariant)
    Spacer(if (pushTilesDown) GlanceModifier.defaultWeight() else GlanceModifier.height(10.dp))
    Row(modifier = GlanceModifier.fillMaxWidth()) {
        StatTile(statusValue, statusLabel, accent, tile, GlanceModifier.defaultWeight())
        Spacer(GlanceModifier.width(Gap))
        StatTile(
            "${stats.present}/${stats.total}", context.getString(R.string.widget_tile_attended),
            colors.onSurface, tile, GlanceModifier.defaultWeight()
        )
    }
}

/** When this subject meets next; opens that class on the Timetable. */
@Composable
private fun NextClassTile(next: ClassSlot?, width: Dp) {
    val colors = GlanceTheme.colors
    val context = LocalContext.current
    val today = LocalDate.now()
    val tileModifier = GlanceModifier
        .fillMaxWidth()
        .background(colors.surface)
        .cornerRadius(InnerRadius)
        .padding(horizontal = 12.dp, vertical = 10.dp)
    Column(modifier = next?.let { tileModifier.clickable(actionStartActivity(timetableIntent(context, it))) } ?: tileModifier) {
        Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            FlexText(context.getString(R.string.widget_next_class), 11f, colors.onSurfaceVariant, FlexStyle.Label)
            Spacer(GlanceModifier.defaultWeight())
            next?.let {
                FlexText(dayLabel(context, it.date, today), 11f, colors.primary, FlexStyle.Label, maxWidth = width / 2, autoShrink = true)
            }
        }
        FlexText(
            next?.let { clockRange(it) + (it.room?.let { room -> " · $room" } ?: "") }
                ?: context.getString(R.string.widget_not_scheduled),
            15f, colors.onSurface, FlexStyle.Title, maxWidth = width - 24.dp
        )
    }
}

/** Every coming class of one subject over the next two weeks. */
@Composable
private fun ColumnScope.UpcomingList(slots: List<ClassSlot>, width: Dp, labelColor: ColorProvider) {
    val colors = GlanceTheme.colors
    val context = LocalContext.current
    val today = LocalDate.now()
    SectionLabel(context.getString(R.string.widget_upcoming), labelColor)
    if (slots.isEmpty()) {
        FlexText(context.getString(R.string.widget_not_scheduled), 13f, labelColor, FlexStyle.Body, maxWidth = width)
        return
    }
    LazyColumn(modifier = GlanceModifier.defaultWeight().fillMaxWidth()) {
        items(slots, itemId = { it.date.toEpochDay() * 100_000 + it.id }) { slot ->
            Column(modifier = GlanceModifier.fillMaxWidth().padding(bottom = 6.dp)) {
                // Two lines, so the time range never has to share a narrow column with the day.
                Column(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .background(colors.surface)
                        .cornerRadius(InnerRadius)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .clickable(actionStartActivity(timetableIntent(context, slot)))
                ) {
                    FlexText(dayLabel(context, slot.date, today), 11f, colors.primary, FlexStyle.Label, maxWidth = width - 24.dp)
                    FlexText(
                        clockRange(slot) + (slot.room?.let { " · $it" } ?: ""), 14f, colors.onSurface,
                        FlexStyle.Title, maxWidth = width - 24.dp
                    )
                }
            }
        }
    }
}

/** Marked classes, newest first. [showSubject] for the cross-subject feed on the Overall widget. */
@Composable
private fun ColumnScope.MarkList(label: String, marks: List<Mark>, width: Dp, labelColor: ColorProvider, showSubject: Boolean) {
    val context = LocalContext.current
    SectionLabel(label, labelColor)
    if (marks.isEmpty()) {
        FlexText(context.getString(R.string.widget_no_history), 13f, labelColor, FlexStyle.Body, maxWidth = width)
        return
    }
    LazyColumn(modifier = GlanceModifier.defaultWeight().fillMaxWidth()) {
        items(marks, itemId = { it.date.toEpochDay() * 100_000 + it.subject.id }) { mark -> MarkRow(mark, width, showSubject) }
    }
}

private val MarkDate: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, d MMM", Locale.getDefault())

@Composable
private fun MarkRow(mark: Mark, width: Dp, showSubject: Boolean) {
    val colors = GlanceTheme.colors
    val context = LocalContext.current
    // The status pill always gets its room; the text gives way first.
    val textWidth = width - 24.dp - StatusPillWidth
    Column(modifier = GlanceModifier.fillMaxWidth().padding(bottom = 6.dp)) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(colors.surface)
                .cornerRadius(InnerRadius)
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .clickable(actionStartActivity(calendarIntent(context, mark.subject))),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showSubject) {
                Column {
                    FlexText(mark.subject.name, 13f, colors.onSurface, FlexStyle.Title, maxWidth = textWidth)
                    FlexText(mark.date.format(MarkDate), 11f, colors.onSurfaceVariant, FlexStyle.Body, maxWidth = textWidth)
                }
            } else {
                FlexText(mark.date.format(MarkDate), 13f, colors.onSurface, FlexStyle.Body, maxWidth = textWidth, autoShrink = true)
            }
            Spacer(GlanceModifier.defaultWeight())
            if (mark.present) {
                Pill(context.getString(R.string.widget_status_present), colors.primary, colors.onPrimary)
            } else {
                Pill(context.getString(R.string.widget_status_absent), colors.error, colors.onError)
            }
        }
    }
}

// ---------------------------------------------------------------- Next class

@Composable
private fun NextClassContent(plan: DayPlan, comingUp: List<ClassSlot>, today: LocalDate) {
    val colors = GlanceTheme.colors
    val context = LocalContext.current
    val inner = LocalSize.current.width - CardPadding * 2
    val hero = plan.current ?: plan.next ?: plan.upcomingDay
    val onClick: Action = hero
        ?.let { actionStartActivity(calendarIntent(context, it.subject)) }
        ?: actionStartActivity<MainActivity>()
    val onCard = colors.onSecondaryContainer

    WidgetCard(colors.secondaryContainer, onClick) {
        if (hero == null) {
            Pill(context.getString(R.string.widget_next_today), onCard, colors.secondaryContainer)
            Spacer(GlanceModifier.defaultWeight())
            FlexText(
                context.getString(R.string.widget_no_more_classes), 22f, onCard,
                FlexStyle.Title, maxWidth = inner, maxLines = 3
            )
            Spacer(GlanceModifier.defaultWeight())
            return@WidgetCard
        }
        // Big splits the list: the hero's own day under it, the days after beside it.
        val sameDay = comingUp.filter { it.date == hero.date }
        val laterDays = comingUp.filter { it.date != hero.date }
        Adaptive(
            summary = { width, pushDown -> NextClassHero(plan, hero, width, today, pushDown) },
            below = { width -> ComingUpList(context.getString(R.string.widget_coming_up), comingUp, width, today) },
            side = { width ->
                ComingUpList(
                    context.getString(R.string.widget_coming_up),
                    if (LocalSize.current.isTall) laterDays else comingUp, width, today
                )
            },
            belowInBig = { width -> ComingUpList(laterLabel(context, hero.date, today), sameDay, width, today) }
        )
    }
}

/** Heading for the rest of the hero's day, which is not always today. */
private fun laterLabel(context: Context, day: LocalDate, today: LocalDate): String = when (day) {
    today -> context.getString(R.string.widget_later_today)
    today.plusDays(1) -> context.getString(R.string.widget_later_tomorrow)
    else -> context.getString(R.string.widget_later_on, day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()))
}

@Composable
private fun ColumnScope.NextClassHero(plan: DayPlan, hero: ClassSlot, width: Dp, today: LocalDate, pushDown: Boolean) {
    val colors = GlanceTheme.colors
    val context = LocalContext.current
    val onCard = colors.onSecondaryContainer
    val tile = (width - Gap) / 2

    // Header: what the hero class is, plus one useful fact on the right.
    Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        when (hero) {
            plan.current -> Pill(context.getString(R.string.widget_next_now), colors.primary, colors.onPrimary)
            plan.next -> Pill(context.getString(R.string.widget_next_upcoming), onCard, colors.secondaryContainer)
            else -> Pill(dayLabel(context, hero.date, today), onCard, colors.secondaryContainer)
        }
        Spacer(GlanceModifier.defaultWeight())
        val aside = when {
            hero == plan.current -> context.getString(R.string.widget_until, clock(hero.end))
            hero == plan.next && plan.moreToday > 0 -> context.getString(R.string.widget_more_today, plan.moreToday)
            hero == plan.next -> context.getString(R.string.widget_last_class)
            else -> null
        }
        aside?.let { FlexText(it, 12f, onCard, FlexStyle.Label, maxWidth = width - PillAllowance, autoShrink = true) }
    }
    Spacer(GlanceModifier.height(6.dp))
    FlexText(hero.subject.name, 22f, onCard, FlexStyle.Title, maxWidth = width, maxLines = 2)
    FlexText(
        clockRange(hero) + (hero.room?.let { " · $it" } ?: ""),
        13f, onCard, FlexStyle.Body, maxWidth = width
    )
    Spacer(if (pushDown) GlanceModifier.defaultWeight() else GlanceModifier.height(10.dp))

    if (hero == plan.current) {
        // The class after this one, so "now" never hides what's coming.
        Column(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(colors.surface)
                .cornerRadius(InnerRadius)
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            val next = plan.next
            if (next == null) {
                FlexText(context.getString(R.string.widget_last_class), 13f, colors.onSurface, FlexStyle.Label)
            } else {
                Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    FlexText(context.getString(R.string.widget_next_upcoming), 11f, colors.onSurfaceVariant, FlexStyle.Label)
                    Spacer(GlanceModifier.defaultWeight())
                    FlexText(clock(next.start), 11f, colors.onSurfaceVariant, FlexStyle.Label)
                }
                FlexText(next.subject.name, 15f, colors.onSurface, FlexStyle.Title, maxWidth = width - 24.dp)
            }
        }
    } else {
        Row(modifier = GlanceModifier.fillMaxWidth()) {
            StatTile(
                hero.subject.percentText, context.getString(R.string.widget_tile_attended),
                if (hero.subject.belowTarget) colors.error else colors.primary,
                tile, GlanceModifier.defaultWeight()
            )
            Spacer(GlanceModifier.width(Gap))
            val (statusValue, statusLabel) = hero.subject.status(context)
            StatTile(
                statusValue, statusLabel,
                if (hero.subject.belowTarget) colors.error else colors.onSurface,
                tile, GlanceModifier.defaultWeight()
            )
        }
    }
}

/** Classes after the hero; each row opens that class on the Timetable. */
@Composable
private fun ColumnScope.ComingUpList(label: String, slots: List<ClassSlot>, width: Dp, today: LocalDate) {
    val colors = GlanceTheme.colors
    val context = LocalContext.current
    SectionLabel(label, colors.onSecondaryContainer)
    if (slots.isEmpty()) {
        FlexText(
            context.getString(R.string.widget_nothing_coming_up), 13f, colors.onSecondaryContainer,
            FlexStyle.Body, maxWidth = width
        )
        return
    }
    LazyColumn(modifier = GlanceModifier.defaultWeight().fillMaxWidth()) {
        items(slots, itemId = { it.date.toEpochDay() * 100_000 + it.id }) { slot -> ComingUpRow(slot, width, today) }
    }
}

@Composable
private fun ComingUpRow(slot: ClassSlot, width: Dp, today: LocalDate) {
    val colors = GlanceTheme.colors
    val context = LocalContext.current
    Column(modifier = GlanceModifier.fillMaxWidth().padding(bottom = 6.dp)) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(colors.surface)
                .cornerRadius(InnerRadius)
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .clickable(actionStartActivity(timetableIntent(context, slot))),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = GlanceModifier.width(TimeColumn)) {
                FlexText(clock(slot.start), 14f, colors.onSurface, FlexStyle.Figure, maxWidth = TimeColumn, autoShrink = true)
                if (slot.date != today) {
                    FlexText(
                        slot.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                        11f, colors.primary, FlexStyle.Label
                    )
                }
            }
            Spacer(GlanceModifier.width(Gap))
            FlexText(
                slot.subject.name, 13f, colors.onSurface, FlexStyle.Title,
                maxWidth = width - 24.dp - TimeColumn - Gap, maxLines = 2
            )
        }
    }
}

private val TimeColumn = 48.dp

private val StatusPillWidth = 76.dp

// Room left for the status pill when the header text sits beside it.
private val PillAllowance = 80.dp
