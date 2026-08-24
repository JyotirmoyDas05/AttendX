@file:OptIn(ExperimentalSharedTransitionApi::class)

package `in`.jyotirmoy.attendx.core.ui.glass

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.jyotirmoy.attendx.core.ui.glass.floatingtabbar.FloatingTabBar
import `in`.jyotirmoy.attendx.core.ui.glass.floatingtabbar.FloatingTabBarDefaults
import `in`.jyotirmoy.attendx.core.ui.glass.floatingtabbar.FloatingTabBarScrollConnection
import `in`.jyotirmoy.attendx.core.ui.glass.floatingtabbar.LocalTabBarBackdropFrozen
import `in`.jyotirmoy.attendx.core.ui.glass.shapes.ContinuousRoundedRectangle

private val NavBarShape = ContinuousRoundedRectangle(percent = 50)
private val NavBarMinTabWidth = 56.dp

data class AttendXNavTab(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

/**
 * Floating Liquid Glass nav bar for AttendX.
 *
 * Collapses to an inline pill while scrolling down (driven by [scrollConnection])
 * and expands back on scroll up. When the Kyant0/backdrop glass effect is enabled,
 * the bar samples the real UI behind it through [Modifier.liquidGlass].
 */
@Composable
fun AttendXFloatingNavBar(
    tabs: List<AttendXNavTab>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    scrollConnection: FloatingTabBarScrollConnection,
    modifier: Modifier = Modifier,
) {
    val glassConfig = LocalGlassEffectConfig.current
    val useGlass = glassConfig.isEnabledFor(GlassComponent.NAV_BAR) && isGlassAllowed()

    val backgroundColor = when {
        useGlass -> Color.Transparent
        else -> MaterialTheme.colorScheme.surfaceContainerHigh
    }

    // A user-picked glass text color wins; otherwise follow the Material scheme:
    // on glass the selected tab sits on the glass puck (onSurface), in the
    // fallback it sits on the opaque secondaryContainer puck (onSecondaryContainer).
    // Unselected tabs are onSurfaceVariant either way, so everything adapts to
    // Dynamic Colors like the rest of the app instead of hardcoding white.
    val usesCustomTextColor = useGlass && glassConfig.textColor.isSpecified
    val rawTabTextColor = when {
        usesCustomTextColor -> glassConfig.textColor
        useGlass -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSecondaryContainer
    }

    val tabTextColor by animateColorAsState(
        targetValue = rawTabTextColor,
        label = "navBarTextColor",
    )
    val selectedContentColor = tabTextColor
    val unselectedContentColor = if (usesCustomTextColor) {
        tabTextColor.copy(alpha = 0.55f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val tabBarContentModifier = if (useGlass) {
        Modifier.liquidGlass(
            config = glassConfig,
            shape = NavBarShape,
            highlightAlpha = glassConfig.highlightOpacity,
            frozen = LocalTabBarBackdropFrozen.current,
        )
    } else Modifier

    BoxWithConstraints(modifier) {
        val tabWidth = if (tabs.isEmpty()) {
            FloatingTabBarDefaults.TabWidth
        } else {
            (maxWidth / tabs.size).coerceIn(NavBarMinTabWidth, FloatingTabBarDefaults.TabWidth)
        }

        FloatingTabBar(
            selectedTabKey = selectedIndex,
            scrollConnection = scrollConnection,
            modifier = Modifier.fillMaxWidth(),
            tabBarContentModifier = tabBarContentModifier,
            colors = FloatingTabBarDefaults.colors(
                backgroundColor = backgroundColor,
                accessoryBackgroundColor = backgroundColor,
            ),
            sizes = FloatingTabBarDefaults.sizes(
                tabBarContentPadding = PaddingValues(vertical = 4.dp, horizontal = 4.dp),
                tabExpandedContentPadding = PaddingValues(vertical = 4.dp, horizontal = 6.dp),
                tabInlineContentPadding = PaddingValues(8.dp),
                tabWidth = tabWidth,
            ),
            backdrop = if (useGlass) LocalAppBackdrop.current else null,
            accentColor = tabTextColor,
        ) {
            tabs.forEachIndexed { index, tabDef ->
                val isSelected = selectedIndex == index
                tab(
                    key = index,
                    title = {
                        Text(
                            text = tabDef.label,
                            color = if (isSelected) selectedContentColor else unselectedContentColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 10.sp,
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = if (isSelected) tabDef.selectedIcon else tabDef.unselectedIcon,
                            contentDescription = tabDef.label,
                            tint = if (isSelected) selectedContentColor else unselectedContentColor,
                            modifier = Modifier.size(24.dp),
                        )
                    },
                    onClick = { onTabSelected(index) },
                )
            }
        }
    }
}
