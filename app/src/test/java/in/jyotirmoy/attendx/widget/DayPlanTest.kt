package `in`.jyotirmoy.attendx.widget

import `in`.jyotirmoy.attendx.core.data.model.SubjectEntity
import `in`.jyotirmoy.attendx.timetable.data.model.TimeTableScheduleEntity
import `in`.jyotirmoy.attendx.timetable.data.model.TimeTableScheduleWithSubject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Covers how the class widgets pick what to show. Boundaries matter: a class stops
 * being "now" the minute it ends, and the next class day only appears once today is done.
 */
class DayPlanTest {

    private val wednesday = LocalDate.of(2026, 10, 28)

    private fun slot(name: String, start: Long, end: Long, date: LocalDate = wednesday) =
        ClassSlot(
            id = name.hashCode(),
            subject = SubjectStats(id = name.hashCode(), name = name, present = 0, absent = 0, target = 75f),
            date = date,
            start = start,
            end = end,
            room = null
        )

    private val os = slot("OS", 9 * 60L, 10 * 60L)
    private val dbms = slot("DBMS", 10 * 60L + 15, 11 * 60L + 15)
    private val cn = slot("CN", 13 * 60L, 14 * 60L)
    private val today = listOf(os, dbms, cn)
    private val thursday = slot("DAA", 9 * 60L, 10 * 60L, wednesday.plusDays(1))

    @Test
    fun `during a class shows it as now and the following one as next`() {
        val plan = planDay(today, 9 * 60L + 30, thursday)
        assertEquals(os, plan.current)
        assertEquals(dbms, plan.next)
        assertEquals(listOf(cn), plan.later)
        assertNull(plan.upcomingDay)
    }

    @Test
    fun `a class is no longer now at its end minute`() {
        val plan = planDay(today, 10 * 60L, thursday)
        assertNull(plan.current)
        assertEquals(dbms, plan.next)
    }

    @Test
    fun `between classes there is only a next`() {
        val plan = planDay(today, 12 * 60L, thursday)
        assertNull(plan.current)
        assertEquals(cn, plan.next)
        assertEquals(0, plan.moreToday)
    }

    @Test
    fun `after the last class it falls back to the next class day`() {
        val plan = planDay(today, 21 * 60L, thursday)
        assertNull(plan.current)
        assertNull(plan.next)
        assertEquals(thursday, plan.upcomingDay)
    }

    @Test
    fun `refresh is due when the current class ends`() {
        val now = LocalDateTime.of(2026, 10, 28, 9, 30)
        val plan = planDay(today, 9 * 60L + 30, thursday)
        assertEquals(LocalDateTime.of(2026, 10, 28, 10, 0), nextPlanChange(plan, now))
    }

    @Test
    fun `with nothing left today refresh is due at midnight`() {
        val now = LocalDateTime.of(2026, 10, 28, 21, 0)
        val plan = planDay(today, 21 * 60L, thursday)
        assertEquals(LocalDateTime.of(2026, 10, 29, 0, 0), nextPlanChange(plan, now))
    }

    @Test
    fun `coming up skips the class already shown under now and continues into later days`() {
        fun row(id: Int, subjectId: Int, day: Int, start: Long) = TimeTableScheduleWithSubject(
            schedule = TimeTableScheduleEntity(id = id, subjectId = subjectId, dayOfWeek = day, startTime = start, endTime = start + 60),
            subject = SubjectEntity(id = subjectId, subject = "S$subjectId")
        )
        val subjects = (1..4).associateWith { SubjectStats(it, "S$it", 0, 0, 75f) }
        // Wednesday is day 3, Thursday day 4.
        val snapshot = WidgetSnapshot(
            subjects,
            listOf(row(1, 1, 3, 540), row(2, 2, 3, 600), row(3, 3, 3, 660), row(4, 4, 4, 540))
                .groupBy { it.schedule.dayOfWeek }
        )
        val now = LocalDateTime.of(2026, 10, 28, 9, 30)
        val plan = snapshot.dayPlan(now)

        assertEquals(1, plan.current?.id)
        assertEquals(2, plan.next?.id)
        assertEquals(listOf(3, 4), snapshot.comingUp(plan).map { it.id })
    }
}
