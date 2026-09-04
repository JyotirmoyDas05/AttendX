package `in`.jyotirmoy.attendx.core

import `in`.jyotirmoy.attendx.core.utils.TimeUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Covers the rules the Add Class form is gated on. The old form expressed these
 * inline as `end > start` over wheel indices, which could not represent an
 * unparseable time and treated a midnight rollover as merely invalid.
 */
class TimeUtilsRangeTest {

    @Test
    fun `accepts a normal class`() {
        assertNull(TimeUtils.validateRange("09:00", "10:30"))
    }

    @Test
    fun `rejects a zero length class`() {
        assertNotNull(TimeUtils.validateRange("09:00", "09:00"))
    }

    @Test
    fun `rejects an end before the start`() {
        assertEquals(
            "End time must be after start time",
            TimeUtils.validateRange("14:00", "09:00")
        )
    }

    @Test
    fun `rejects a class that would wrap past midnight`() {
        // 11:30 PM to 12:30 AM. The auto-fill used to produce exactly this and
        // then leave the form unsubmittable with no way to recover.
        assertNotNull(TimeUtils.validateRange("23:30", "00:30"))
    }

    @Test
    fun `rejects unparseable input rather than passing it through`() {
        assertNotNull(TimeUtils.validateRange("", "10:00"))
        assertNotNull(TimeUtils.validateRange("09:00", "not a time"))
        assertNotNull(TimeUtils.validateRange("25:00", "26:00"))
    }

    @Test
    fun `a minute long class is allowed`() {
        assertNull(TimeUtils.validateRange("09:00", "09:01"))
    }

    @Test
    fun `overlapping classes are detected`() {
        assertTrue(TimeUtils.overlaps("09:00", "10:00", "09:30", "10:30"))
        assertTrue(TimeUtils.overlaps("09:30", "10:30", "09:00", "10:00"))
    }

    @Test
    fun `a class fully inside another overlaps`() {
        assertTrue(TimeUtils.overlaps("09:00", "12:00", "10:00", "11:00"))
        assertTrue(TimeUtils.overlaps("10:00", "11:00", "09:00", "12:00"))
    }

    @Test
    fun `back to back classes do not overlap`() {
        // The common case: a 9-10 lecture followed by a 10-11 lab is legal.
        assertFalse(TimeUtils.overlaps("09:00", "10:00", "10:00", "11:00"))
        assertFalse(TimeUtils.overlaps("10:00", "11:00", "09:00", "10:00"))
    }

    @Test
    fun `separated classes do not overlap`() {
        assertFalse(TimeUtils.overlaps("09:00", "10:00", "14:00", "15:00"))
    }

    @Test
    fun `unparseable times are not reported as a clash`() {
        // Better to let the range validation produce the error than to block the
        // form with a confusing "overlaps an existing class" message.
        assertFalse(TimeUtils.overlaps("garbage", "10:00", "09:00", "11:00"))
    }
}
