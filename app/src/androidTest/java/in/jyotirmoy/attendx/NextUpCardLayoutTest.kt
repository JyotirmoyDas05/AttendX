package `in`.jyotirmoy.attendx

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import `in`.jyotirmoy.attendx.core.data.model.SubjectEntity
import `in`.jyotirmoy.attendx.timetable.data.model.TimeTableScheduleEntity
import `in`.jyotirmoy.attendx.timetable.data.model.TimeTableScheduleWithSubject
import `in`.jyotirmoy.attendx.timetable.presentation.components.NextUpCard
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Layout guards for the status band above the calendar.
 *
 * The band packs a long course title and a free-text room onto one row, so the
 * two can fight for width. These pin the rules that keep that readable: the
 * trailing metadata is capped, and the row gives way to a stack before either
 * side is squeezed. Both depend on measured widths, which is why they run on a
 * device rather than as unit tests.
 */
class NextUpCardLayoutTest {

    @get:Rule
    val rule = createComposeRule()

    private val longSubject = "Essence of Indian Knowlege Tradition and Heritage Studies"
    private val longRoom = "Block C, Second Floor, Laboratory 214-B"

    private fun classWith(room: String?) = TimeTableScheduleWithSubject(
        schedule = TimeTableScheduleEntity(
            id = 1,
            subjectId = 1,
            dayOfWeek = 5,
            startTime = 15 * 60,
            endTime = 16 * 60,
            room = room
        ),
        subject = SubjectEntity(id = 1, subject = longSubject)
    )

    private fun show(width: Int, fontScale: Float = 1f, room: String? = longRoom) {
        rule.setContent {
            val base = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(base.density, fontScale)
            ) {
                Box(modifier = Modifier.width(width.dp)) {
                    NextUpCard(nextClass = classWith(room), currentClass = null)
                }
            }
        }
    }

    @Test
    fun aLongRoomDoesNotStarveTheSubject() {
        show(width = 400)

        val subject = rule.onNodeWithText(longSubject, substring = true)
            .getUnclippedBoundsInRoot()
        val room = rule.onNodeWithText(longRoom, substring = true)
            .getUnclippedBoundsInRoot()

        // The subject is what the user came to read; the room is supporting
        // detail and must never end up the wider of the two.
        val subjectWidth = subject.right - subject.left
        val roomWidth = room.right - room.left
        assertTrue(
            "subject $subjectWidth should be wider than room $roomWidth",
            subjectWidth > roomWidth
        )
    }

    @Test
    fun theRowGivesWayToAStackWhenTextIsScaledUp() {
        show(width = 400, fontScale = 2f)

        val subject = rule.onNodeWithText(longSubject, substring = true)
            .getUnclippedBoundsInRoot()
        val room = rule.onNodeWithText(longRoom, substring = true)
            .getUnclippedBoundsInRoot()

        // Stacked means the metadata sits below the title rather than beside it.
        assertTrue(
            "expected the room (top ${room.top}) below the subject " +
                "(bottom ${subject.bottom}) once stacked",
            room.top >= subject.bottom
        )
    }

    @Test
    fun theBandStaysSideBySideAtANormalWidthAndTextSize() {
        show(width = 400)

        val subject = rule.onNodeWithText(longSubject, substring = true)
            .getUnclippedBoundsInRoot()
        val time = rule.onNodeWithText("3:00 pm", substring = true)
            .getUnclippedBoundsInRoot()

        // Beside, not below — this is the whole point of the reshaped card.
        assertTrue(
            "expected the time (left ${time.left}) to the right of the subject " +
                "(right ${subject.right})",
            time.left >= subject.right
        )
    }

    @Test
    fun aClassWithNoRoomStillRenders() {
        show(width = 400, room = null)

        rule.onNodeWithText(longSubject, substring = true).assertIsDisplayed()
        rule.onNodeWithText("3:00 pm", substring = true).assertIsDisplayed()
    }

    @Test
    fun theBandSurvivesAVeryNarrowScreen() {
        show(width = 240)

        rule.onNodeWithText(longSubject, substring = true).assertIsDisplayed()
        rule.onNodeWithText(longRoom, substring = true).assertIsDisplayed()
    }
}
