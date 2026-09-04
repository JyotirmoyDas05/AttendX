@file:OptIn(ExperimentalSharedTransitionApi::class)

package `in`.jyotirmoy.attendx

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import `in`.jyotirmoy.attendx.timetable.presentation.components.DEFAULT_HOUR_HEIGHT
import `in`.jyotirmoy.attendx.timetable.presentation.components.MAX_HOUR_HEIGHT
import `in`.jyotirmoy.attendx.timetable.presentation.components.MIN_HOUR_HEIGHT
import `in`.jyotirmoy.attendx.timetable.presentation.components.TimetableCalendarView
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

/**
 * Pinch-to-zoom on the calendar grid.
 *
 * This needs a real two-finger gesture, which `adb shell input` cannot produce,
 * so it lives here rather than as a unit test. It exists because the zoom broke
 * in a way that is invisible to the compiler: the gesture block is created once
 * and never re-keyed, so reading the hour height from a plain parameter captured
 * the value the screen opened with. Every pinch then scaled from that frozen
 * number instead of the current one, and the second pinch of a session undid the
 * first.
 */
class TimetableZoomTest {

    @get:Rule
    val rule = createComposeRule()

    private val grid = "calendar-grid"

    private fun setContent(onHeight: (Float) -> Unit, height: () -> Float) {
        rule.setContent {
            SharedTransitionLayout {
                AnimatedContent(targetState = Unit, label = "test") { _ ->
                    TimetableCalendarView(
                        weeklyClasses = emptyMap(),
                        anchorDate = LocalDate.of(2026, 9, 4),
                        numDays = 1,
                        animatedVisibilityScope = this@AnimatedContent,
                        verticalScrollState = rememberScrollState(),
                        hourHeight = height(),
                        onHourHeightChange = onHeight,
                        onAnchorDateChange = {},
                        onClassClick = { _, _ -> },
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag(grid)
                    )
                }
            }
        }
    }

    /** Spreads two fingers apart vertically by [spread] pixels. */
    private fun pinchOut(spread: Float) {
        rule.onNodeWithTag(grid).performTouchInput {
            val start = 60f
            down(0, center + Offset(0f, -start))
            down(1, center + Offset(0f, start))
            repeat(4) { step ->
                val d = start + spread * (step + 1) / 4f
                moveTo(0, center + Offset(0f, -d))
                moveTo(1, center + Offset(0f, d))
            }
            up(0)
            up(1)
        }
        rule.waitForIdle()
    }

    @Test
    fun pinchingOutMakesTheHoursTaller() {
        var height by mutableFloatStateOf(DEFAULT_HOUR_HEIGHT)
        setContent(onHeight = { height = it }, height = { height })

        pinchOut(spread = 200f)

        assertTrue(
            "expected a taller hour row than $DEFAULT_HOUR_HEIGHT, got $height",
            height > DEFAULT_HOUR_HEIGHT
        )
    }

    @Test
    fun asecondPinchBuildsOnTheFirst() {
        var height by mutableFloatStateOf(DEFAULT_HOUR_HEIGHT)
        setContent(onHeight = { height = it }, height = { height })

        pinchOut(spread = 150f)
        val afterFirst = height
        pinchOut(spread = 150f)

        // The regression this guards: with the height captured once, the second
        // pinch scaled from the starting value again and landed back where the
        // first one did.
        assertTrue(
            "second pinch did not build on the first ($afterFirst then $height)",
            height > afterFirst
        )
    }

    @Test
    fun zoomNeverLeavesItsBounds() {
        var height by mutableFloatStateOf(DEFAULT_HOUR_HEIGHT)
        setContent(onHeight = { height = it }, height = { height })

        repeat(6) { pinchOut(spread = 400f) }

        assertTrue("$height above $MAX_HOUR_HEIGHT", height <= MAX_HOUR_HEIGHT)
        assertTrue("$height below $MIN_HOUR_HEIGHT", height >= MIN_HOUR_HEIGHT)
    }

    @Test
    fun zoomClampsAtTheMaximum() {
        // Started just short of the ceiling, because a pinch is limited by the
        // screen and repeated gestures alone do not reliably reach it.
        var height by mutableFloatStateOf(MAX_HOUR_HEIGHT - 1f)
        setContent(onHeight = { height = it }, height = { height })

        pinchOut(spread = 300f)

        assertEquals(MAX_HOUR_HEIGHT, height, 0.01f)
    }
}
