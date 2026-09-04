package `in`.jyotirmoy.attendx.core.presentation.components.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Two controls side by side, stacking when the row would be too cramped.
 *
 * A hard-coded `Row` of two weighted text fields is the app's most reliable way
 * to clip a label: the available width shrinks with the screen but the text
 * grows with the user's font scale, so the two meet well before either is
 * extreme on its own.
 *
 * The threshold is scaled by the font scale rather than being a fixed dp,
 * because the content needs room in proportion to how large the user has asked
 * text to be. A fixed 340dp looks fine at 1x and still clips "10:00 AM" to
 * "10:00 AI" at 2x on the very same screen.
 *
 * @param minWidthForRow width below which the children stack, at a font scale
 *   of 1. The default suits a labelled text field; raise it for wider content.
 */
@Composable
fun AdaptiveTwoColumn(
    modifier: Modifier = Modifier,
    spacing: Dp = 12.dp,
    minWidthForRow: Dp = 340.dp,
    first: @Composable () -> Unit,
    second: @Composable () -> Unit
) {
    val fontScale = LocalDensity.current.fontScale

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        if (maxWidth < minWidthForRow * fontScale) {
            Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
                first()
                second()
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                Box(modifier = Modifier.weight(1f)) { first() }
                Box(modifier = Modifier.weight(1f)) { second() }
            }
        }
    }
}
