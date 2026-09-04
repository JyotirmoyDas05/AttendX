package `in`.jyotirmoy.attendx.core.presentation.components.pager

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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

    // settledPage, not currentPage: currentPage changes the moment a drag crosses
    // the halfway point, which would emit references for pages the user is still
    // dragging through and may yet flick back from.
    LaunchedEffect(pagerState, base) {
        snapshotFlow { pagerState.settledPage }.collect { page ->
            val settled = referenceForPage(page)
            if (settled != reference) onReferenceChange(settled)
        }
    }

    // The caller changed the reference from outside — follow it, unless we are
    // already there because this change came from our own swipe.
    LaunchedEffect(reference) {
        val target = HALF_RANGE + offsetOf(reference, base)
        if (pagerState.settledPage != target) {
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
