package `in`.jyotirmoy.attendx.main.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import `in`.jyotirmoy.attendx.home.presentation.screens.HomeScreen
import `in`.jyotirmoy.attendx.timetable.presentation.screens.TimeTableScreen
import `in`.jyotirmoy.attendx.navigation.LocalNavController
import `in`.jyotirmoy.attendx.navigation.UploadTemplateScreen
import `in`.jyotirmoy.attendx.navigation.MarketplaceScreen

import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home

data class BottomNavItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val screen: @Composable () -> Unit
)

@Composable
fun MainScreen() {
    val navController = LocalNavController.current
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    val navItems = listOf(
        BottomNavItem(
            label = "Home",
            selectedIcon = Icons.Rounded.Home,
            unselectedIcon = Icons.Outlined.Home,
            screen = { HomeScreen() }
        ),
        BottomNavItem(
            label = "Timetable",
            selectedIcon = Icons.Rounded.DateRange,
            unselectedIcon = Icons.Outlined.DateRange,
            screen = { TimeTableScreen(
                onNavigateToUpload = { navController.navigate(UploadTemplateScreen) },
                onNavigateToMarketplace = { navController.navigate(MarketplaceScreen()) }
            ) }
        )
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ) {
                navItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == index) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index }
                    )
                }
            }
        }
    ) { innerPadding ->
        // Display selected screen with bottom padding for nav bar
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            when (selectedTab) {
                0 -> HomeScreen()
                1 -> TimeTableScreen(
                    onNavigateToUpload = { navController.navigate(UploadTemplateScreen) },
                    onNavigateToMarketplace = { navController.navigate(MarketplaceScreen()) }
                )
            }
        }
    }
}
