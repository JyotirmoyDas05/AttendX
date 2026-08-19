package `in`.jyotirmoy.attendx.main.presentation

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.HourglassBottom
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.os.Build
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.jyotirmoy.attendx.core.common.LocalFloatingNavBarPadding
import `in`.jyotirmoy.attendx.core.common.LocalWeakHaptic
import `in`.jyotirmoy.attendx.home.presentation.components.drawer.SmartAttendanceDrawer
import `in`.jyotirmoy.attendx.home.presentation.screens.HomeScreen
import `in`.jyotirmoy.attendx.navigation.LocalNavController
import `in`.jyotirmoy.attendx.navigation.MarketplaceScreen
import `in`.jyotirmoy.attendx.navigation.SettingsScreen
import `in`.jyotirmoy.attendx.navigation.UploadTemplateScreen
import `in`.jyotirmoy.attendx.settings.data.local.SettingsKeys
import `in`.jyotirmoy.attendx.settings.presentation.viewmodel.SettingsViewModel
import `in`.jyotirmoy.attendx.timetable.presentation.screens.TimeTableScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

// -- Data models --

data class BottomNavItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private data class ItemMetrics(val left: Float, val width: Float)

// -- Fluid dynamics constants (XCalendar) --

private const val MAX_STRETCH = 0.4f
private const val CLICK_STRETCH = 0.12f
private const val COMPRESS_RATIO = 0.25f
private const val DRAG_SENSITIVITY = 30f
private const val SETTLE_DAMPING = 0.35f
private const val SETTLE_STIFFNESS = 400f

// -- Animation specs (XCalendar) --

private object NavBarAnimationSpecs {
    val indicatorSpring = spring<Float>(dampingRatio = 0.6f, stiffness = 800f)
    val fluidSettleSpring = spring<Float>(dampingRatio = SETTLE_DAMPING, stiffness = SETTLE_STIFFNESS)
    val fluidOvershootSpring = spring<Float>(dampingRatio = 0.3f, stiffness = 280f)
    val fluidClickSpring = spring<Float>(dampingRatio = 0.4f, stiffness = 600f)
    val clickTransition = tween<Float>(400, easing = FastOutSlowInEasing)
    val fluidStartTween = tween<Float>(60)
}

// -- Animation phases (XCalendar state machine) --

private sealed interface AnimationPhase {
    data object Idle : AnimationPhase
    data object Dragging : AnimationPhase
    data object ClickAnimating : AnimationPhase
    data object MorphingOut : AnimationPhase

    val showGlassEffect: Boolean get() = this != Idle
}

// -- Stable state holder (mirrors XCalendar NavBarState) --

@Stable
private class NavBarState(
    val indicatorOffset: Animatable<Float, AnimationVector1D>,
    val indicatorScale: Animatable<Float, AnimationVector1D>,
    val boxScale: Animatable<Float, AnimationVector1D>,
    val phase: MutableState<AnimationPhase>,
    val itemMetrics: SnapshotStateMap<Int, ItemMetrics>,
    val dragOffset: MutableState<Float>,
    val dragStartIndex: MutableState<Int>,
    val indicatorWidthPx: MutableState<Float>,
    val indicatorInitialized: MutableState<Boolean>,
    val fluidDeformation: Animatable<Float, AnimationVector1D>,
) {
    val glassStretchX: Float get() = 1f + fluidDeformation.value
    val glassStretchY: Float get() = 1f - fluidDeformation.value * COMPRESS_RATIO

    val dragBounds: Pair<Float, Float>
        get() = if (itemMetrics.isNotEmpty()) {
            val firstLeft = itemMetrics[0]?.left ?: 0f
            val lastLeft = itemMetrics[itemMetrics.size - 1]?.left ?: 0f
            firstLeft to lastLeft
        } else 0f to 0f

    val itemCenters: Map<Int, Float>
        get() = itemMetrics.mapValues { (_, m) -> m.left + m.width / 2f }

    fun findNearestIndex(indicatorCenter: Float): Int =
        itemCenters.minByOrNull { (_, center) -> abs(center - indicatorCenter) }?.key
            ?: dragStartIndex.value
}

@Composable
private fun rememberNavBarState(): NavBarState = remember {
    NavBarState(
        indicatorOffset = Animatable(0f),
        indicatorScale = Animatable(1f),
        boxScale = Animatable(1f),
        phase = mutableStateOf(AnimationPhase.Idle),
        itemMetrics = mutableStateMapOf(),
        dragOffset = mutableStateOf(0f),
        dragStartIndex = mutableStateOf(-1),
        indicatorWidthPx = mutableStateOf(0f),
        indicatorInitialized = mutableStateOf(false),
        fluidDeformation = Animatable(0f),
    )
}

// -- Drag gesture modifier (XCalendar) --

private fun Modifier.indicatorDragGesture(
    state: NavBarState,
    selectedIndex: Int,
    coroutineScope: CoroutineScope,
    onTabSelect: (Int) -> Unit,
): Modifier = pointerInput(selectedIndex) {
    detectDragGesturesAfterLongPress(
        onDragStart = {
            state.phase.value = AnimationPhase.Dragging
            state.dragStartIndex.value = selectedIndex
            state.dragOffset.value = state.indicatorOffset.value
        },
        onDrag = { _, dragAmount ->
            val (minBound, maxBound) = state.dragBounds
            state.dragOffset.value =
                (state.dragOffset.value + dragAmount.x).coerceIn(minBound, maxBound)

            // Smoothed deformation: lerp toward target instead of snapping
            val targetDeformation = (dragAmount.x / DRAG_SENSITIVITY).coerceIn(-1f, 1f) * MAX_STRETCH
            val smoothed = state.fluidDeformation.value + (targetDeformation - state.fluidDeformation.value) * 0.4f
            coroutineScope.launch { state.fluidDeformation.snapTo(smoothed) }
        },
        onDragEnd = {
            coroutineScope.launch {
                val indicatorCenter =
                    state.dragOffset.value + (state.indicatorWidthPx.value / 2f)
                val nearestIndex = state.findNearestIndex(indicatorCenter)
                val targetPos =
                    state.itemMetrics[nearestIndex]?.left ?: state.dragOffset.value

                // overshoot: briefly reverse deformation
                val currentDeformation = state.fluidDeformation.value
                launch {
                    state.fluidDeformation.animateTo(
                        -currentDeformation * 0.5f,
                        NavBarAnimationSpecs.fluidOvershootSpring
                    )
                    state.fluidDeformation.animateTo(0f, NavBarAnimationSpecs.fluidSettleSpring)
                }

                // animate indicator to snapped position
                state.indicatorOffset.animateTo(targetPos, NavBarAnimationSpecs.indicatorSpring)
                state.phase.value = AnimationPhase.MorphingOut

                if (nearestIndex != state.dragStartIndex.value) {
                    onTabSelect(nearestIndex)
                }
            }
        },
    )
}

// -- Click transition (XCalendar) --

private suspend fun NavBarState.animateClickTransition(targetIndex: Int) {
    val targetPos = itemMetrics[targetIndex]?.left ?: indicatorOffset.value

    phase.value = AnimationPhase.ClickAnimating

    kotlinx.coroutines.coroutineScope {
        // position glide
        launch {
            indicatorOffset.animateTo(targetPos, NavBarAnimationSpecs.clickTransition)
        }
        // fluid deformation: stretch → overshoot → settle
        launch {
            fluidDeformation.animateTo(CLICK_STRETCH, NavBarAnimationSpecs.fluidStartTween)
            fluidDeformation.animateTo(
                -CLICK_STRETCH * 0.3f,
                NavBarAnimationSpecs.fluidClickSpring
            )
            fluidDeformation.animateTo(0f, NavBarAnimationSpecs.fluidSettleSpring)
        }
    }

    phase.value = AnimationPhase.MorphingOut
}

// -- Main composable --

@Composable
fun MainScreen(
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val navController = LocalNavController.current
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val state = rememberNavBarState()

    val liquidGlassEnabled by settingsViewModel.isItemChecked(SettingsKeys.LIQUID_GLASS_EFFECT)
        .collectAsState(initial = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)

    val navItems = remember {
        listOf(
            BottomNavItem("Home", Icons.Rounded.Home, Icons.Outlined.Home),
            BottomNavItem("Tools", Icons.Rounded.HourglassBottom, Icons.Outlined.HourglassEmpty),
            BottomNavItem("Timetable", Icons.Rounded.DateRange, Icons.Outlined.DateRange)
        )
    }

    // Effect 1: Sync indicator position with selection (XCalendar)
    LaunchedEffect(selectedTab, state.itemMetrics.size) {
        if (state.phase.value is AnimationPhase.Dragging ||
            state.phase.value is AnimationPhase.ClickAnimating ||
            selectedTab < 0
        ) return@LaunchedEffect

        val metrics = state.itemMetrics[selectedTab] ?: return@LaunchedEffect
        state.indicatorWidthPx.value = metrics.width
        val targetPos = metrics.left

        if (state.indicatorInitialized.value && abs(state.indicatorOffset.value - targetPos) >= 0.5f) {
            state.indicatorOffset.animateTo(targetPos, NavBarAnimationSpecs.indicatorSpring)
        } else {
            state.indicatorOffset.snapTo(targetPos)
            state.indicatorInitialized.value = true
        }
    }

    // Effect 2: Scale animations from phase changes (XCalendar)
    LaunchedEffect(state.phase.value) {
        when (val phase = state.phase.value) {
            AnimationPhase.Dragging, AnimationPhase.ClickAnimating -> {
                launch { state.indicatorScale.animateTo(1.25f, NavBarAnimationSpecs.indicatorSpring) }
                launch { state.boxScale.animateTo(1.05f, NavBarAnimationSpecs.indicatorSpring) }
            }
            AnimationPhase.MorphingOut -> {
                // Immediately show filled pill, let scale settle visually in background
                state.phase.value = AnimationPhase.Idle
                launch { state.indicatorScale.animateTo(1f, NavBarAnimationSpecs.indicatorSpring) }
                launch { state.boxScale.animateTo(1f, NavBarAnimationSpecs.indicatorSpring) }
            }
            AnimationPhase.Idle -> {
                launch { state.indicatorScale.animateTo(1f, NavBarAnimationSpecs.indicatorSpring) }
                launch { state.boxScale.animateTo(1f, NavBarAnimationSpecs.indicatorSpring) }
            }
        }
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val weakHaptic = LocalWeakHaptic.current

    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen || selectedTab == 0,
        drawerContent = {
            SmartAttendanceDrawer(
                onSettingsClick = {
                    scope.launch { drawerState.close() }
                    navController.navigate(SettingsScreen)
                    weakHaptic()
                }
            )
        }
    ) {
        val navBarBottomPadding = if (isLandscape) 72.dp else 104.dp
        Box(modifier = Modifier.fillMaxSize()) {
            // Screen content (true edge-to-edge behind floating nav bar)
            CompositionLocalProvider(LocalFloatingNavBarPadding provides navBarBottomPadding) {
                Box(modifier = Modifier.fillMaxSize()) {
                    when (selectedTab) {
                        0 -> HomeScreen(
                            onOpenDrawer = {
                                scope.launch { drawerState.open() }
                            }
                        )
                        1 -> `in`.jyotirmoy.attendx.tools.presentation.ToolsHubScreen(
                            onNavigateToPomodoro = {
                                navController.navigate(`in`.jyotirmoy.attendx.navigation.PomodoroScreenRoute)
                            }
                        )
                        2 -> TimeTableScreen(
                            onNavigateToUpload = { navController.navigate(UploadTemplateScreen) },
                            onNavigateToMarketplace = { navController.navigate(MarketplaceScreen()) },
                            onNavigateToSettings = { navController.navigate(SettingsScreen) }
                        )
                    }
                }
            }

        // Floating Bottom Navigation Bar with AGSL Glass / Fluid Solid Pill (XCalendar architecture)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = if (isLandscape) 4.dp else 10.dp)
        ) {
            // Track nav bar container size for glass shader
            var navBarSize by remember { mutableStateOf(IntSize.Zero) }

            // Determine effective offset
            val effectiveOffset = when (state.phase.value) {
                AnimationPhase.Dragging -> state.dragOffset.value
                else -> state.indicatorOffset.value
            }

            // Real AGSL glass shader effect (API 33+, null on older or when disabled)
            val glassEffect = NavBarGlassEffect.rememberGlassRenderEffect(
                navBarWidth = navBarSize.width.toFloat(),
                navBarHeight = navBarSize.height.toFloat(),
                indicatorOffsetPx = effectiveOffset,
                indicatorWidthPx = state.indicatorWidthPx.value,
                indicatorScale = state.indicatorScale.value,
                glassStretchX = state.glassStretchX,
                glassStretchY = state.glassStretchY,
                onSurfaceColor = MaterialTheme.colorScheme.onSurface,
                primaryColor = MaterialTheme.colorScheme.primary,
                enabled = liquidGlassEnabled && state.phase.value.showGlassEffect,
            )

            Surface(
                modifier = Modifier
                    .shadow(
                        elevation = if (isLandscape) 6.dp else 12.dp,
                        shape = RoundedCornerShape(28.dp),
                        spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                        ambientColor = MaterialTheme.colorScheme.surfaceTint.copy(alpha = 0.15f)
                    ),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.88f),
                border = BorderStroke(
                    0.8.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                ),
                tonalElevation = 6.dp
            ) {
                Box(
                    modifier = Modifier
                        .onSizeChanged { navBarSize = it }
                        .graphicsLayer {
                            renderEffect = glassEffect
                            scaleX = state.boxScale.value
                            scaleY = state.boxScale.value
                        }
                        .padding(
                            horizontal = if (isLandscape) 4.dp else 5.dp,
                            vertical = if (isLandscape) 3.dp else 4.dp
                        )
                        .indicatorDragGesture(state, selectedTab, scope) { newIndex ->
                            selectedTab = newIndex
                        }
                ) {
                    // Fluid Solid Pill Indicator (when Liquid Glass shader is off / trimmed-down mode)
                    if (!liquidGlassEnabled && state.indicatorWidthPx.value > 0f) {
                        val indicatorWidthDp = with(density) { state.indicatorWidthPx.value.toDp() }
                        val pillHeight = if (isLandscape) 36.dp else 46.dp

                        Box(
                            modifier = Modifier
                                .offset { IntOffset(x = effectiveOffset.roundToInt(), y = 0) }
                                .width(indicatorWidthDp)
                                .height(pillHeight)
                                .graphicsLayer {
                                    scaleX = state.indicatorScale.value * state.glassStretchX
                                    scaleY = state.indicatorScale.value * state.glassStretchY
                                }
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                        )
                    }

                    // Tab Items Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(if (isLandscape) 4.dp else 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        navItems.forEachIndexed { index, item ->
                            val isSelected = selectedTab == index
                            val showStaticBg =
                                liquidGlassEnabled && isSelected && state.phase.value == AnimationPhase.Idle
                            val contentColor by animateColorAsState(
                                targetValue = if (isSelected) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                label = "navContent"
                            )

                            Row(
                                modifier = Modifier
                                    .onGloballyPositioned { coords ->
                                        state.itemMetrics[index] = ItemMetrics(
                                            left = coords.positionInParent().x,
                                            width = coords.size.width.toFloat()
                                        )
                                        if (state.indicatorWidthPx.value == 0f || index == selectedTab) {
                                            state.indicatorWidthPx.value =
                                                coords.size.width.toFloat()
                                        }
                                    }
                                    .clip(RoundedCornerShape(20.dp))
                                    .then(
                                        if (showStaticBg) {
                                            Modifier.background(
                                                MaterialTheme.colorScheme.primaryContainer,
                                                RoundedCornerShape(20.dp)
                                            )
                                        } else Modifier
                                    )
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        if (index != selectedTab && state.phase.value == AnimationPhase.Idle) {
                                            selectedTab = index
                                            scope.launch {
                                                state.animateClickTransition(index)
                                            }
                                        }
                                    }
                                    .padding(
                                        horizontal = if (isLandscape) 14.dp else 18.dp,
                                        vertical = if (isLandscape) 6.dp else 10.dp
                                    ),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label,
                                    tint = contentColor,
                                    modifier = Modifier.size(if (isLandscape) 19.dp else 22.dp)
                                )
                                if (isSelected || !isLandscape) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = item.label,
                                        color = contentColor,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontSize = if (isLandscape) 11.5.sp else 12.5.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
}
