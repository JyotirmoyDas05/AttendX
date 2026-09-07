package `in`.jyotirmoy.attendx.core.presentation.components.pager

import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier

/**
 * Number of pages either side of the starting point. Large enough that a user
 * cannot reach the end by swiping, small enough to stay well inside Int range
 * when multiplied by a page's day span.
 */
private const val HALF_RANGE = 5_000

/**
 * Horizontal pager over an unbounded sequence, driven by an external value
 * rather than by a page index.
 *
 * The caller owns the current [reference] — a date, a week, whatever the screen
 * is anchored to — and this maps it on and off a page index. That keeps paging
 * bidirectional: swiping updates the caller through [onReferenceChange], and the
 * caller moving the reference some other way (a "today" button, tapping a day)
 * animates the pager to match.
 *
 * Adapted from the equivalent component in XCalendar, which solves the same
 * problem for its calendar screens.
 *
 * @param reference the value currently displayed
 * @param offsetOf whole pages between two references; must be the inverse of
 *   [referenceAt] or the pager and the caller will fight each other
 * @param referenceAt the reference shown [pageOffset] pages away from [base]
 * @param onReferenceChange invoked once a swipe settles, never mid-gesture
 */
@Composable
fun <T> InfiniteSwipePager(
    reference: T,
    offsetOf: (current: T, base: T) -> Int,
    referenceAt: (base: T, pageOffset: Int) -> T,
    onReferenceChange: (T) -> Unit,
    modifier: Modifier = Modifier,
    userScrollEnabled: Boolean = true,
    key: ((page: Int) -> Any)? = null,
    content: @Composable (reference: T) -> Unit
) {
    // Frozen on first composition so page indices stay stable for the lifetime
    // of the pager. Recomputing it as the reference moves would renumber every
    // page underneath the user mid-swipe.
    val base = remember { reference }

    val pagerState = rememberPagerState(
        initialPage = HALF_RANGE + offsetOf(reference, base),
        pageCount = { HALF_RANGE * 2 }
    )

    fun referenceForPage(page: Int): T = referenceAt(base, page - HALF_RANGE)

    // Whether the page the pager is on is one the user put it on.
    //
    // settledPage falls back to currentPage the instant a scroll stops, and a
    // programmatic scroll cut short — by a second tap arriving while the first
    // is still animating — stops on whatever page it had reached. Reporting
    // that as a swipe sent a page the user never chose back to the caller,
    // which moved the reference, which started another scroll: rapid taps on
    // the day strip flickered and settled on a date nobody picked.
    //
    // A drag is the one unambiguous signal that the user, not us, is driving.
    var draggedSinceReport by remember { mutableStateOf(false) }

    LaunchedEffect(pagerState) {
        pagerState.interactionSource.interactions.collect { interaction ->
            if (interaction is DragInteraction.Start) draggedSinceReport = true
        }
    }

    // settledPage, not currentPage: currentPage changes the moment a drag crosses
    // the halfway point, which would emit references for pages the user is still
    // dragging through and may yet flick back from.
    LaunchedEffect(pagerState, base) {
        snapshotFlow { pagerState.settledPage }.collect { page ->
            if (!draggedSinceReport) return@collect
            draggedSinceReport = false
            val settled = referenceForPage(page)
            if (settled != reference) onReferenceChange(settled)
        }
    }

    // The caller changed the reference from outside — follow it, unless we are
    // already there because this change came from our own swipe.
    LaunchedEffect(reference) {
        val target = HALF_RANGE + offsetOf(reference, base)
        if (pagerState.settledPage != target) {
            // We are driving from here on, so a drag that ended back on the page
            // it started from no longer counts as the user choosing this settle.
            draggedSinceReport = false
            pagerState.animateScrollToPage(target)
        }
    }

    HorizontalPager(
        state = pagerState,
        modifier = modifier,
        userScrollEnabled = userScrollEnabled,
        key = key,
        beyondViewportPageCount = 1
    ) { page ->
        content(referenceForPage(page))
    }
}
