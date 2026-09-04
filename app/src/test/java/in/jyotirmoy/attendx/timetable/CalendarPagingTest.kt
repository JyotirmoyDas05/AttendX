package `in`.jyotirmoy.attendx.timetable

import `in`.jyotirmoy.attendx.timetable.domain.WeekPattern
import `in`.jyotirmoy.attendx.timetable.presentation.components.CalendarPaging
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class CalendarPagingTest {

    // 2026-09-02 is a Wednesday.
    private val wed = LocalDate.of(2026, 9, 2)

    @Test
    fun `single day windows start on their own date`() {
        assertEquals(wed, CalendarPaging.windowStart(wed, 1))
    }

    @Test
    fun `week windows start on monday`() {
        val start = CalendarPaging.windowStart(wed, 7)
        assertEquals(DayOfWeek.MONDAY, start.dayOfWeek)
        assertEquals(LocalDate.of(2026, 8, 31), start)
    }

    @Test
    fun `every day of a week maps to the same week window`() {
        val expected = CalendarPaging.windowStart(wed, 7)
        for (offset in -3L..3L) {
            val date = wed.plusDays(offset)
            if (date.with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)) == expected) {
                assertEquals(expected, CalendarPaging.windowStart(date, 7))
            }
        }
    }

    @Test
    fun `three day windows land on a fixed grid regardless of anchor`() {
        // Any date inside the same 3-day block must resolve to one start, or the
        // pager would renumber its pages depending on where the screen opened.
        val start = CalendarPaging.windowStart(wed, 3)
        assertEquals(start, CalendarPaging.windowStart(start, 3))
        assertEquals(start, CalendarPaging.windowStart(start.plusDays(1), 3))
        assertEquals(start, CalendarPaging.windowStart(start.plusDays(2), 3))
        assertEquals(start.plusDays(3), CalendarPaging.windowStart(start.plusDays(3), 3))
    }

    @Test
    fun `offset and start are exact inverses`() {
        for (numDays in intArrayOf(1, 3, 7)) {
            val base = CalendarPaging.windowStart(wed, numDays)
            for (page in -400..400) {
                val start = CalendarPaging.windowStartAt(base, page, numDays)
                assertEquals(
                    "numDays=$numDays page=$page",
                    page,
                    CalendarPaging.pageOffsetOf(start, base, numDays)
                )
            }
        }
    }

    @Test
    fun `paging backwards does not stick on the base page`() {
        // Truncating division maps both the base and the page before it to 0,
        // which makes a backwards swipe snap straight back.
        val base = CalendarPaging.windowStart(wed, 3)
        assertEquals(-1, CalendarPaging.pageOffsetOf(base.minusDays(3), base, 3))
        assertEquals(-2, CalendarPaging.pageOffsetOf(base.minusDays(6), base, 3))
        assertEquals(-1, CalendarPaging.pageOffsetOf(base.minusDays(7), base, 7))
    }

    @Test
    fun `swiping forward always moves the date forward`() {
        // The old day-of-week model wrapped Sunday back to Monday, so swiping
        // forward moved the displayed date a week backwards.
        var start = CalendarPaging.windowStart(wed, 1)
        repeat(10) {
            val next = CalendarPaging.windowStartAt(start, 1, 1)
            assertTrue("$next should be after $start", next.isAfter(start))
            start = next
        }
    }

    @Test
    fun `a week window covers monday through sunday`() {
        val dates = CalendarPaging.windowDates(CalendarPaging.windowStart(wed, 7), 7)
        assertEquals(7, dates.size)
        assertEquals(DayOfWeek.MONDAY, dates.first().dayOfWeek)
        assertEquals(DayOfWeek.SUNDAY, dates.last().dayOfWeek)
    }

    @Test
    fun `week type alternates between adjacent weeks`() {
        val thisWeek = WeekPattern.weekTypeOf(wed)
        val nextWeek = WeekPattern.weekTypeOf(wed.plusWeeks(1))
        assertTrue(thisWeek != nextWeek)
    }

    @Test
    fun `all pattern runs every week`() {
        assertTrue(WeekPattern.matches(WeekPattern.ALL, wed))
        assertTrue(WeekPattern.matches(WeekPattern.ALL, wed.plusWeeks(1)))
    }

    @Test
    fun `odd and even patterns are mutually exclusive on a given week`() {
        val odd = WeekPattern.matches(WeekPattern.ODD, wed)
        val even = WeekPattern.matches(WeekPattern.EVEN, wed)
        assertTrue(odd != even)
    }

    @Test
    fun `an unknown pattern still shows the class`() {
        // Dropping a class from the timetable is worse than showing an extra.
        assertTrue(WeekPattern.matches("fortnightly", wed))
    }
}
