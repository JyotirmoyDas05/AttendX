package `in`.jyotirmoy.attendx

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import `in`.jyotirmoy.attendx.core.presentation.components.pager.InfiniteSwipePager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * The pager reports a page back to its caller only when the user put it there.
 *
 * Tapping a day three columns away animates the pager across the pages in
 * between. Tapping again before that lands cuts the animation short, and the
 * pager stops on whichever page it had reached. Reporting that as a swipe moved
 * the caller's date to one nobody picked, which started another animation — the
 * flicker-and-land-on-the-wrong-day this pins down.
 */
class InfiniteSwipePagerTest {

    @get:Rule
    val rule = createComposeRule()

    private val reported = mutableListOf<Int>()
    private var reference by mutableStateOf(0)

    private fun show() {
        rule.setContent {
            InfiniteSwipePager(
                reference = reference,
                offsetOf = { current, base -> current - base },
                referenceAt = { base, offset -> base + offset },
                onReferenceChange = {
                    reported += it
                    reference = it
                },
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("pager")
            ) {
                Box(modifier = Modifier.fillMaxSize())
            }
        }
    }

    @Test
    fun anInterruptedJumpIsNotReportedBackAsASwipe() {
        show()
        rule.mainClock.autoAdvance = false

        // Two taps in quick succession, the second landing mid-animation.
        rule.runOnUiThread { reference = 3 }
        rule.mainClock.advanceTimeBy(120)
        rule.runOnUiThread { reference = 1 }

        rule.mainClock.autoAdvance = true
        rule.waitForIdle()

        assertEquals("no page should have been reported back", emptyList<Int>(), reported)
        assertEquals("should rest on the page last asked for", 1, reference)
    }

    @Test
    fun aRealSwipeIsStillReported() {
        show()

        rule.onNodeWithTag("pager").performTouchInput { swipeLeft() }
        rule.waitForIdle()

        assertTrue("a drag should move the reference forward, got $reported", reported == listOf(1))
    }
}
