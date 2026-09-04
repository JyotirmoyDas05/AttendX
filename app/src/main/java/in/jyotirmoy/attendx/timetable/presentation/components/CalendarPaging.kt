package `in`.jyotirmoy.attendx.timetable.presentation.components

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

/**
 * Mapping between the date a calendar view is anchored to and the page it sits
 * on. Kept as free functions so the arithmetic can be tested without a device —
 * an off-by-one here shows up as a view that scrolls back a page every time you
 * swipe, which is miserable to debug through the UI.
 *
 * Each view snaps its window to a *fixed* grid rather than to wherever the pager
 * happened to start. That is what keeps [pageOffsetOf] and [windowStartAt] exact
 * inverses: if a window could begin on any date, jumping to an unaligned date
 * (tapping a day, pressing "today") would land between pages, and the pager
 * would drag the selection to the nearest boundary behind the user's back.
 */
object CalendarPaging {

    /**
     * First date of the window containing [anchor].
     *
     * - 1 day: every date is its own window.
     * - Week: the ISO Monday, so weeks line up with how the header reads.
     * - Anything else: a fixed cycle counted from the epoch, so the grid is the
     *   same no matter which date the screen opened on.
     */
    fun windowStart(anchor: LocalDate, numDays: Int): LocalDate = when {
        numDays <= 1 -> anchor
        numDays >= 7 -> anchor.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        else -> anchor.minusDays(Math.floorMod(anchor.toEpochDay(), numDays.toLong()))
    }

    /**
     * Whole pages from [base] to [current]. Both must be window starts on the
     * same grid, which [windowStart] guarantees.
     *
     * Floor division, not `/`: Kotlin truncates towards zero, so a plain divide
     * maps the page before the base and the base itself both to 0, and swiping
     * backwards sticks.
     */
    fun pageOffsetOf(current: LocalDate, base: LocalDate, numDays: Int): Int {
        val days = ChronoUnit.DAYS.between(base, current)
        return Math.floorDiv(days, numDays.coerceAtLeast(1).toLong()).toInt()
    }

    /** Inverse of [pageOffsetOf]. */
    fun windowStartAt(base: LocalDate, pageOffset: Int, numDays: Int): LocalDate =
        base.plusDays(pageOffset.toLong() * numDays.coerceAtLeast(1))

    /** The dates a window covers, left to right. */
    fun windowDates(start: LocalDate, numDays: Int): List<LocalDate> =
        (0 until numDays.coerceAtLeast(1)).map { start.plusDays(it.toLong()) }
}
