package `in`.jyotirmoy.attendx.timetable.presentation.screens.template.update

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import `in`.jyotirmoy.attendx.core.common.LocalWeakHaptic

/**
 * Persistent affordance for a pending template update, sat beside the
 * import/export menu so it is where the user would go to act on it.
 *
 * Deliberately a badge rather than a card: the timetable header already leads
 * with the next-class block and a second large card would compete with it.
 */
@Composable
fun TemplateUpdateBadge(
    visible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    iconSize: Dp = 20.dp
) {
    val weakHaptic = LocalWeakHaptic.current

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
        modifier = modifier
    ) {
        Surface(
            onClick = {
                weakHaptic()
                onClick()
            },
            shape = CircleShape,
            color = MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.onTertiary,
            modifier = Modifier.size(size)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.Sync,
                    contentDescription = "Timetable update available",
                    modifier = Modifier.size(iconSize)
                )
            }
        }
    }
}
