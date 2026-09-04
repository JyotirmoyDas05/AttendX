package `in`.jyotirmoy.attendx.timetable.domain

import `in`.jyotirmoy.attendx.timetable.data.model.TimeTableScheduleWithSubject
import java.time.LocalDate
import java.time.temporal.ChronoField

/**
 * Odd/even week resolution for classes that only run on alternating weeks.
 *
 * This used to live as a private copy inside three use cases, each of which read
 * `LocalDate.now()` directly. That was fine while the timetable could only ever
 * show the current week, but the calendar views now page through weeks, so the
 * answer depends on *which* week is on screen rather than on today. Taking the
 * date as a parameter is what makes that possible; it also makes the rule
 * testable without waiting a week.
 */
object WeekPattern {

    const val ALL = "all"
    const val ODD = "odd"
    const val EVEN = "even"

    /** "odd" or "even" for the week containing [date]. */
    fun weekTypeOf(date: LocalDate): String =
        if (date.get(ChronoField.ALIGNED_WEEK_OF_YEAR) % 2 == 1) ODD else EVEN

    /**
     * Whether a class with [pattern] runs during the week containing [date].
     * An unrecognised pattern is treated as "every week" — losing a class from
     * the timetable is worse than showing one that should have been hidden.
     */
    fun matches(pattern: String, date: LocalDate): Boolean = when (pattern) {
        ODD -> weekTypeOf(date) == ODD
        EVEN -> weekTypeOf(date) == EVEN
        else -> true
    }
}

/**
 * Classes that actually run on [date], taken from the recurring week map and
 * narrowed by each class's odd/even week pattern.
 *
 * One place decides "what is on this date", so the grid, the agenda list and the
 * view model cannot drift apart on the answer.
 */
fun classesOn(
    weekly: Map<Int, List<TimeTableScheduleWithSubject>>,
    date: LocalDate
): List<TimeTableScheduleWithSubject> =
    weekly[date.dayOfWeek.value]
        .orEmpty()
        .filter { WeekPattern.matches(it.schedule.weekPattern, date) }
