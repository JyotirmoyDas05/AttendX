package `in`.jyotirmoy.attendx.tools.presentation.pomodoro.theme

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

object PomodoroShapeDefaults {
    val topListItemShape: RoundedCornerShape
        @Composable get() =
            RoundedCornerShape(
                topStart = 28.dp,
                topEnd = 28.dp,
                bottomStart = 4.dp,
                bottomEnd = 4.dp
            )

    val middleListItemShape: RoundedCornerShape
        @Composable get() = RoundedCornerShape(4.dp)

    val bottomListItemShape: RoundedCornerShape
        @Composable get() =
            RoundedCornerShape(
                topStart = 4.dp,
                topEnd = 4.dp,
                bottomStart = 28.dp,
                bottomEnd = 28.dp
            )

    val cardShape: CornerBasedShape
        @Composable get() = shapes.large

    @Composable
    fun getListItemShape(index: Int, count: Int): Shape {
        return when {
            count <= 1 -> cardShape
            index == 0 -> topListItemShape
            index == count - 1 -> bottomListItemShape
            else -> middleListItemShape
        }
    }

    val PANE_MAX_WIDTH = 600.dp
}

object PomodoroColors {
    var black = false

    @OptIn(ExperimentalMaterial3Api::class)
    val topBarColors: TopAppBarColors
        @Composable get() =
            TopAppBarDefaults.topAppBarColors(
                containerColor = if (!black) colorScheme.surfaceContainer else colorScheme.surface,
                scrolledContainerColor = if (!black) colorScheme.surfaceContainer else colorScheme.surface
            )

    @OptIn(ExperimentalMaterial3Api::class)
    val detailPaneTopBarColors: TopAppBarColors
        @Composable get() =
            TopAppBarDefaults.topAppBarColors(
                containerColor = if (!black) colorScheme.surfaceContainerLow else colorScheme.surface,
                scrolledContainerColor = if (!black) colorScheme.surfaceContainerLow else colorScheme.surface
            )

    val listItemColors: ListItemColors
        @Composable get() =
            ListItemDefaults.colors(
                containerColor = if (!black) colorScheme.surfaceBright else colorScheme.surfaceContainerHigh
            )

    val switchColors: SwitchColors
        @Composable get() = SwitchDefaults.colors(
            checkedIconColor = colorScheme.primary,
        )
}

@Composable
fun PomodoroSegmentedListItem(
    onClick: () -> Unit,
    shape: Shape,
    modifier: Modifier = Modifier,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    supportingContent: (@Composable () -> Unit)? = null,
    selected: Boolean = false,
    content: @Composable () -> Unit
) {
    val containerColor = if (selected) {
        colorScheme.primaryContainer
    } else if (!PomodoroColors.black) {
        colorScheme.surfaceBright
    } else {
        colorScheme.surfaceContainerHigh
    }

    ListItem(
        headlineContent = content,
        supportingContent = supportingContent,
        leadingContent = leadingContent,
        trailingContent = trailingContent,
        colors = ListItemDefaults.colors(
            containerColor = containerColor
        ),
        modifier = modifier
            .clip(shape)
            .clickable(onClick = onClick)
    )
}
