package `in`.jyotirmoy.attendx.main.presentation

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.HourglassBottom
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.jyotirmoy.attendx.core.common.LocalFloatingNavBarPadding
import `in`.jyotirmoy.attendx.core.common.blocksDrawerGesture
import `in`.jyotirmoy.attendx.core.common.LocalWeakHaptic
import `in`.jyotirmoy.attendx.core.ui.glass.AttendXFloatingNavBar
import `in`.jyotirmoy.attendx.core.ui.glass.AttendXNavTab
import `in`.jyotirmoy.attendx.core.ui.glass.LocalAppBackdrop
import `in`.jyotirmoy.attendx.core.ui.glass.backdrop.backdrops.LayerBackdrop
import `in`.jyotirmoy.attendx.core.ui.glass.backdrop.backdrops.layerBackdrop
import `in`.jyotirmoy.attendx.core.ui.glass.backdrop.backdrops.rememberBackdropFreeze
import `in`.jyotirmoy.attendx.core.ui.glass.floatingtabbar.rememberFloatingTabBarScrollConnection
import `in`.jyotirmoy.attendx.core.ui.glass.isGlassAllowed
import `in`.jyotirmoy.attendx.home.presentation.components.drawer.SmartAttendanceDrawer
import `in`.jyotirmoy.attendx.home.presentation.screens.HomeScreen
import `in`.jyotirmoy.attendx.navigation.LocalNavController
import `in`.jyotirmoy.attendx.navigation.MarketplaceScreen
import `in`.jyotirmoy.attendx.navigation.SettingsScreen
import `in`.jyotirmoy.attendx.navigation.UploadTemplateScreen
import `in`.jyotirmoy.attendx.settings.presentation.viewmodel.SettingsViewModel
import `in`.jyotirmoy.attendx.timetable.presentation.screens.TimeTableScreen
import kotlinx.coroutines.launch

/** Index of the Timetable tab in [MainScreen]'s nav bar. */
private const val TIMETABLE_TAB = 2

@Composable
fun MainScreen(
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val navController = LocalNavController.current
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    // The Schedule widget opens a class on the Timetable tab; switch to it, and that screen opens the sheet.
    val timetableLaunch by `in`.jyotirmoy.attendx.navigation.TimetableLaunch.pending.collectAsState()
    LaunchedEffect(timetableLaunch) {
        if (timetableLaunch != null) selectedTab = TIMETABLE_TAB
    }
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val scope = rememberCoroutineScope()
    val weakHaptic = LocalWeakHaptic.current

    val navTabs = remember {
        listOf(
            AttendXNavTab("Home", Icons.Rounded.Home, Icons.Outlined.Home),
            AttendXNavTab("Tools", Icons.Rounded.HourglassBottom, Icons.Outlined.HourglassEmpty),
            AttendXNavTab("Timetable", Icons.Rounded.DateRange, Icons.Outlined.DateRange),
        )
    }

    // Drives the bar's inline↔expanded collapse on scroll
    val scrollConnection = rememberFloatingTabBarScrollConnection(
        initialIsInline = false,
    )

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    // Screen-level backdrop freeze during scroll/fling
    val backdropFreeze = rememberBackdropFreeze()
    val rawBackdrop = LocalAppBackdrop.current
    val appLayerBackdrop = if (isGlassAllowed()) rawBackdrop as? LayerBackdrop else null

    // The nav bar's real footprint, measured from its own layout: system nav
    // inset + bottom offset + bar height, latched at the tallest observed state
    // so the collapse-to-pill animation never churns content padding. Screens
    // and FABs derive their bottom spacing from this, which guarantees nothing
    // can slide under the bar at any screen size, font scale or orientation.
    val density = LocalDensity.current
    var navBarOccupiedPx by remember { mutableFloatStateOf(0f) }
    val measuredNavBarPadding = with(density) { navBarOccupiedPx.toDp() }
    val navBarBottomPadding = if (measuredNavBarPadding > 0.dp) {
        measuredNavBarPadding + 16.dp
    } else {
        if (isLandscape) 72.dp else 104.dp
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen || selectedTab == 0,
        drawerContent = {
            SmartAttendanceDrawer(
                onCommunityClick = {
                    scope.launch { drawerState.close() }
                    navController.navigate(MarketplaceScreen())
                    weakHaptic()
                },
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
            // Screen content — isolated and recorded into layerBackdrop for glass sampling
            CompositionLocalProvider(LocalFloatingNavBarPadding provides navBarBottomPadding) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer()
                        .then(
                            if (appLayerBackdrop != null) {
                                Modifier.layerBackdrop(
                                    appLayerBackdrop,
                                    frozen = backdropFreeze.frozen
                                )
                            } else Modifier
                        )
                        .graphicsLayer()
                        .nestedScroll(backdropFreeze.connection)
                        .nestedScroll(scrollConnection),
                ) {
                    when (selectedTab) {
                        0 -> HomeScreen(
                            onOpenDrawer = {
                                scope.launch { drawerState.open() }
                            }
                        )
                        1 -> `in`.jyotirmoy.attendx.tools.presentation.ToolsHubScreen(
                            onNavigateToPomodoro = {
                                navController.navigate(`in`.jyotirmoy.attendx.navigation.PomodoroScreenRoute)
                            },
                            onNavigateToMarketplace = {
                                navController.navigate(MarketplaceScreen())
                            },
                            onNavigateToUpload = {
                                navController.navigate(UploadTemplateScreen)
                            }
                        )
                        TIMETABLE_TAB -> TimeTableScreen(
                            onNavigateToUpload = { navController.navigate(UploadTemplateScreen) },
                            onNavigateToMarketplace = { navController.navigate(MarketplaceScreen()) },
                            onNavigateToSettings = { navController.navigate(SettingsScreen) }
                        )
                    }
                }
            }

            // Liquid Glass Floating NavBar
            AttendXFloatingNavBar(
                tabs = navTabs,
                selectedIndex = selectedTab,
                onTabSelected = { selectedTab = it },
                scrollConnection = scrollConnection,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    // Placed BEFORE the inset/padding modifiers so the reported
                    // size covers everything the bar occupies from the screen
                    // bottom: nav inset + bottom offset + bar itself.
                    .onSizeChanged { size ->
                        if (size.height > navBarOccupiedPx) {
                            navBarOccupiedPx = size.height.toFloat()
                        }
                    }
                    // Dragging on the bar (e.g. from Home rightwards) must never
                    // pull the drawer's full-screen drag gesture open with it.
                    .blocksDrawerGesture()
                    .navigationBarsPadding()
                    .padding(
                        bottom = if (isLandscape) 4.dp else 10.dp,
                        start = 24.dp,
                        end = 24.dp,
                    ),
            )
        }
    }
}
