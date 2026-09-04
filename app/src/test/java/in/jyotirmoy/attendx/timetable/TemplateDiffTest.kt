package `in`.jyotirmoy.attendx.timetable

import `in`.jyotirmoy.attendx.timetable.data.model.community.TemplateClassEntry
import `in`.jyotirmoy.attendx.timetable.data.model.community.TemplateSubjectEntry
import `in`.jyotirmoy.attendx.timetable.domain.usecase.diffTemplate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TemplateDiffTest {

    private fun cls(day: Int, start: Long, subject: String, room: String? = null) =
        TemplateClassEntry(
            dayOfWeek = day, startTime = start, endTime = start + 60,
            subject = subject, room = room
        )

    private val local = listOf(cls(1, 600, "Maths"), cls(2, 540, "Physics"))

    @Test
    fun `identical template reports no changes`() {
        val diff = diffTemplate(local, emptyList(), local, emptyList())

        assertTrue(diff.incoming.isEmpty())
        assertTrue(diff.localOnly.isEmpty())
        assertTrue(diff.newSubjects.isEmpty())
    }

    @Test
    fun `an added class shows as incoming only`() {
        val remote = local + cls(3, 660, "Chemistry")

        val diff = diffTemplate(remote, emptyList(), local, emptyList())

        assertEquals(listOf(cls(3, 660, "Chemistry")), diff.incoming)
        assertTrue(diff.localOnly.isEmpty())
    }

    @Test
    fun `a class only the user has shows as localOnly and is never incoming`() {
        val localWithExtra = local + cls(5, 780, "Elective")

        val diff = diffTemplate(local, emptyList(), localWithExtra, emptyList())

        assertTrue(diff.incoming.isEmpty())
        assertEquals(listOf(cls(5, 780, "Elective")), diff.localOnly)
    }

    @Test
    fun `a changed time appears as one incoming plus one localOnly`() {
        // Structural equality can't express an edit, so a moved class shows up on
        // both sides. The dialog copy accounts for this.
        val remote = listOf(cls(1, 630, "Maths"), cls(2, 540, "Physics"))

        val diff = diffTemplate(remote, emptyList(), local, emptyList())

        assertEquals(listOf(cls(1, 630, "Maths")), diff.incoming)
        assertEquals(listOf(cls(1, 600, "Maths")), diff.localOnly)
    }

    @Test
    fun `new subjects are matched case-insensitively`() {
        val localSubjects = listOf(TemplateSubjectEntry(name = "maths"))
        val remoteSubjects = listOf(
            TemplateSubjectEntry(name = "Maths"),
            TemplateSubjectEntry(name = "Chemistry")
        )

        val diff = diffTemplate(emptyList(), remoteSubjects, emptyList(), localSubjects)

        assertEquals(listOf(TemplateSubjectEntry(name = "Chemistry")), diff.newSubjects)
    }

    @Test
    fun `results are sorted by day then start time`() {
        val remote = listOf(cls(5, 540, "E"), cls(1, 700, "A"), cls(1, 600, "B"))

        val diff = diffTemplate(remote, emptyList(), emptyList(), emptyList())

        assertEquals(listOf(1, 1, 5), diff.incoming.map { it.dayOfWeek })
        assertEquals(listOf(600L, 700L, 540L), diff.incoming.map { it.startTime })
    }
}
