package `in`.jyotirmoy.attendx.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import `in`.jyotirmoy.attendx.MainActivity
import `in`.jyotirmoy.attendx.R
import `in`.jyotirmoy.attendx.core.data.database.SubjectDatabase
import `in`.jyotirmoy.attendx.core.domain.model.AttendanceStatus
import `in`.jyotirmoy.attendx.core.domain.usecase.bunkAllowance
import `in`.jyotirmoy.attendx.core.domain.usecase.classesNeeded
import `in`.jyotirmoy.attendx.timetable.data.model.TimeTableScheduleWithSubject
import `in`.jyotirmoy.attendx.timetable.domain.classesOn
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

/** Subject picked in the configure screen, stored per widget instance. */
val SubjectIdKey = intPreferencesKey("subject_id")

private const val CLASS_CLOCK_REFRESH_WORK = "next_class_widget_refresh"

internal data class SubjectStats(
    val id: Int,
    val name: String,
    val present: Int,
    val absent: Int,
    val target: Float
) {
    val total get() = present + absent
    val fraction get() = if (total == 0) 0f else present.toFloat() / total
    val belowTarget get() = total > 0 && fraction * 100f < target
    val percentText get() = if (total == 0) "—" else "${(fraction * 100).roundToInt()}%"
}

/** One timetable class on one concrete date. [id] is the class_schedule row, for deep links. */
internal data class ClassSlot(
    val id: Int,
    val subject: SubjectStats,
    val date: LocalDate,
    val start: Long,
    val end: Long,
    val room: String?
)

/** One marked class: which subject, which day, and whether it was a present. */
internal data class Mark(val subject: SubjectStats, val date: LocalDate, val present: Boolean)

/** What the Next class widget shows: the class running now, the one after it, and what follows. */
internal data class DayPlan(
    val current: ClassSlot?,
    val next: ClassSlot?,
    val later: List<ClassSlot>,
    val upcomingDay: ClassSlot?
) {
    val moreToday get() = later.size
}

/**
 * One read of subjects, their attendance and the weekly timetable, shared by a render.
 * Dates are resolved through the timetable's own [classesOn], so odd/even-week classes
 * appear in the widgets exactly when they appear on the Timetable screen.
 */
internal class WidgetSnapshot(
    val subjects: Map<Int, SubjectStats>,
    private val weekly: Map<Int, List<TimeTableScheduleWithSubject>>,
    /** Marked classes across every subject, newest first. */
    val marks: List<Mark> = emptyList()
) {
    fun slotsOn(date: LocalDate): List<ClassSlot> =
        classesOn(weekly, date)
            .sortedBy { it.schedule.startTime }
            .mapNotNull { row ->
                subjects[row.schedule.subjectId]?.let {
                    ClassSlot(row.schedule.id, it, date, row.schedule.startTime, row.schedule.endTime, row.schedule.room)
                }
            }

    fun dayPlan(now: LocalDateTime): DayPlan {
        val today = now.toLocalDate()
        val upcomingDay = (1L..7L).firstNotNullOfOrNull { slotsOn(today.plusDays(it)).firstOrNull() }
        return planDay(slotsOn(today), minuteOf(now), upcomingDay)
    }

    /**
     * Classes after the plan's hero, over the hero's day and the [days] after it. While a class
     * is running, the card already shows the one after it, so the list starts past that.
     */
    fun comingUp(plan: DayPlan, days: Long = 3, limit: Int = 12): List<ClassSlot> {
        val hero = plan.current ?: plan.next ?: plan.upcomingDay ?: return emptyList()
        return (0L..days).asSequence()
            .flatMap { slotsOn(hero.date.plusDays(it)).asSequence() }
            .filter { it.date > hero.date || it.start > hero.start }
            .filterNot { plan.current != null && it == plan.next }
            .take(limit)
            .toList()
    }

    /** This subject's coming classes over the next two weeks, counting one that is running now. */
    fun upcomingOf(subjectId: Int, now: LocalDateTime, limit: Int = 8): List<ClassSlot> {
        val today = now.toLocalDate()
        return (0L..13L).asSequence()
            .flatMap { slotsOn(today.plusDays(it)).asSequence() }
            .filter { it.subject.id == subjectId && (it.date > today || it.end > minuteOf(now)) }
            .take(limit)
            .toList()
    }

    fun nextClassOf(subjectId: Int, now: LocalDateTime): ClassSlot? = upcomingOf(subjectId, now, limit = 1).firstOrNull()

    fun historyOf(subjectId: Int, limit: Int = 30): List<Mark> =
        marks.asSequence().filter { it.subject.id == subjectId }.take(limit).toList()
}

internal suspend fun loadSnapshot(context: Context): WidgetSnapshot {
    val db = SubjectDatabase.getDatabase(context)
    val attendance = db.attendanceDao()
    val subjects = db.subjectDao().getAllSubjectsOnce().associate { subject ->
        subject.id to SubjectStats(
            id = subject.id,
            name = subject.subject,
            present = attendance.getCountBySubjectAndStatus(subject.id, AttendanceStatus.PRESENT).first(),
            absent = attendance.getCountBySubjectAndStatus(subject.id, AttendanceStatus.ABSENT).first(),
            target = subject.targetPercentage
        )
    }
    val weekly = db.timeTableDao().getAllClassesWithSubject().first().groupBy { it.schedule.dayOfWeek }
    // Attendance dates are ISO strings, so one read serves every subject's history.
    val marks = attendance.getAllAttendancesOnce()
        .asSequence()
        .filter { it.status != AttendanceStatus.UNMARKED }
        .mapNotNull { mark ->
            val subject = subjects[mark.subjectId] ?: return@mapNotNull null
            runCatching { LocalDate.parse(mark.date) }.getOrNull()
                ?.let { Mark(subject, it, mark.status == AttendanceStatus.PRESENT) }
        }
        .sortedByDescending { it.date }
        .take(300)
        .toList()
    return WidgetSnapshot(subjects, weekly, marks)
}

/** Fires when any table the widgets read changes. */
private fun dataChanges(context: Context) =
    SubjectDatabase.getDatabase(context).invalidationTracker
        .createFlow("attendance", "subjects", "class_schedule", emitInitialState = false)

/**
 * Glance keeps a widget's session alive for a while after it renders, and update() on a live
 * session only recomposes: it doesn't run provideGlance again. Data read once up front would
 * then stay stale, so it is reloaded here, inside the composition, whenever its tables change.
 */
@Composable
internal fun rememberLiveSnapshot(
    context: Context,
    initial: WidgetSnapshot,
    rescheduleClock: Boolean = false
): State<WidgetSnapshot> = produceState(initial) {
    dataChanges(context).collect {
        val fresh = loadSnapshot(context)
        if (rescheduleClock) LocalDateTime.now().let { now -> scheduleClockRefresh(context, fresh.dayPlan(now), now) }
        value = fresh
    }
}

internal fun minuteOf(now: LocalDateTime): Long = now.hour * 60L + now.minute

/** Picks "now" and "next" from today's classes (sorted by start). The next class day only shows once today is done. */
internal fun planDay(today: List<ClassSlot>, minute: Long, upcomingDay: ClassSlot?): DayPlan {
    val current = today.firstOrNull { minute >= it.start && minute < it.end }
    val upcoming = today.filter { it.start > minute }
    return DayPlan(
        current = current,
        next = upcoming.firstOrNull(),
        later = upcoming.drop(1),
        upcomingDay = if (current == null && upcoming.isEmpty()) upcomingDay else null
    )
}

/** The plan changes when the current class ends, the next one starts, or the day rolls over. */
internal fun nextPlanChange(plan: DayPlan, now: LocalDateTime): LocalDateTime {
    val startOfDay = now.toLocalDate().atStartOfDay()
    val minutes = listOfNotNull(plan.current?.end, plan.next?.start).minOrNull()
    return minutes?.let { startOfDay.plusMinutes(it) } ?: startOfDay.plusDays(1)
}

/**
 * Re-renders the time-driven widgets when "now" and "next" shift. Every widget that shows
 * class times calls this, so any one of them alone still keeps itself current.
 */
internal fun scheduleClockRefresh(context: Context, plan: DayPlan, now: LocalDateTime) {
    WorkManager.getInstance(context).enqueueUniqueWork(
        CLASS_CLOCK_REFRESH_WORK,
        ExistingWorkPolicy.REPLACE,
        OneTimeWorkRequestBuilder<NextClassRefreshWorker>()
            // A few seconds late beats rendering a minute early.
            .setInitialDelay(Duration.between(now, nextPlanChange(plan, now)).plusSeconds(5))
            .build()
    )
}

class NextClassRefreshWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        NextClassWidget().updateAll(applicationContext)
        ScheduleWidget().updateAll(applicationContext)
        SubjectAttendanceWidget().updateAll(applicationContext)
        return Result.success()
    }
}

/** Called from App whenever attendance, subjects, or the timetable change, so every widget stays current. */
suspend fun refreshAttendanceWidgets(context: Context) {
    OverallAttendanceWidget().updateAll(context)
    SubjectAttendanceWidget().updateAll(context)
    NextClassWidget().updateAll(context)
    ScheduleWidget().updateAll(context)
}

/** Bunk-aware status figure: classes you can skip, or classes needed to get back to target. */
internal fun SubjectStats.status(context: Context): Pair<String, String> = when {
    total == 0 -> "—" to context.getString(R.string.widget_tile_no_classes)
    belowTarget ->
        classesNeeded(present, total, target).toString() to
            context.getString(R.string.widget_tile_to_reach, target.roundToInt())
    else -> bunkAllowance(present, total, target).toString() to context.getString(R.string.widget_tile_can_skip)
}

/** Same deep link the class notifications use: opens the subject's calendar page. */
internal fun calendarIntent(context: Context, subject: SubjectStats): Intent =
    Intent(context, MainActivity::class.java)
        .putExtra("EXTRA_NAVIGATE_TO", "CALENDAR")
        .putExtra("EXTRA_SUBJECT_ID", subject.id)
        .putExtra("EXTRA_SUBJECT_NAME", subject.name)

/** Opens the Timetable: on [slot]'s date with its class sheet up, on the add-class sheet, or plainly. */
internal fun timetableIntent(context: Context, slot: ClassSlot? = null, addClass: Boolean = false): Intent =
    Intent(context, MainActivity::class.java)
        .putExtra("EXTRA_NAVIGATE_TO", "TIMETABLE")
        .putExtra("EXTRA_SCHEDULE_ID", slot?.id ?: -1)
        .putExtra("EXTRA_DATE", slot?.date?.toString())
        .putExtra("EXTRA_ADD_CLASS", addClass)

internal fun clock(minutes: Long): String = "%02d:%02d".format(minutes / 60, minutes % 60)

internal fun clockRange(slot: ClassSlot): String = "${clock(slot.start)}–${clock(slot.end)}"

/** "Today", "Tomorrow", or the short weekday for anything later. */
internal fun dayLabel(context: Context, date: LocalDate, today: LocalDate): String = when (date) {
    today -> context.getString(R.string.widget_next_today)
    today.plusDays(1) -> context.getString(R.string.widget_next_tomorrow)
    else -> date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
}
