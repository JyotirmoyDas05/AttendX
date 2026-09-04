package `in`.jyotirmoy.attendx.core

import `in`.jyotirmoy.attendx.core.utils.UsernameFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UsernameFilterTest {

    private fun valid(name: String) = UsernameFilter.validate(name) is UsernameFilter.Result.Valid

    @Test
    fun `ordinary names are accepted`() {
        listOf("jyotirmoy", "Jyotirmoy.Das", "cs_2027", "a-b-c", "Das", "student123")
            .forEach { assertTrue("$it should be valid", valid(it)) }
    }

    @Test
    fun `charset and length are enforced`() {
        listOf("ab", "a".repeat(25), "has space", "emoji😀", "semi;colon", "")
            .forEach { assertEquals("$it should be rejected", false, valid(it)) }
    }

    @Test
    fun `profanity is rejected as a whole name or a token`() {
        listOf("fuck", "FUCK", "shit.head", "big_ass", "xX-cunt-Xx")
            .forEach { assertEquals("$it should be rejected", false, valid(it)) }
    }

    @Test
    fun `leetspeak and stretched spellings are caught`() {
        listOf("sh1t", "f4ggot", "fuuuuck", "n1gger")
            .forEach { assertEquals("$it should be rejected", false, valid(it)) }
    }

    @Test
    fun `impersonation of the app or its operators is rejected`() {
        listOf("admin", "AttendX", "support", "official", "moderator")
            .forEach { assertEquals("$it should be rejected", false, valid(it)) }
    }

    @Test
    fun `short blocked words do not reject legitimate names containing them`() {
        // The reason short words are token-matched rather than substring-matched:
        // "ass" must not reject "class" or the developer's own surname.
        listOf("classmate", "Jyotirmoy.Das", "bassist", "Cassandra", "passenger", "sexton")
            .forEach { assertTrue("$it should be valid", valid(it)) }
    }

    @Test
    fun `normalize collapses runs of three or more but leaves doubles alone`() {
        assertEquals("fuck", UsernameFilter.normalize("fuuuuck"))
        assertEquals("ass", UsernameFilter.normalize("ass"))
        assertEquals("hello", UsernameFilter.normalize("he.l-l_o"))
    }
}
