package `in`.jyotirmoy.attendx.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import `in`.jyotirmoy.attendx.calender.presentation.screens.CalendarScreen
import `in`.jyotirmoy.attendx.home.presentation.screens.HomeScreen
import `in`.jyotirmoy.attendx.settings.presentation.page.about.screens.AboutScreen
import `in`.jyotirmoy.attendx.settings.presentation.page.backup.screens.BackupAndRestoreScreen
import `in`.jyotirmoy.attendx.settings.presentation.page.behavior.screens.BehaviorScreen
import `in`.jyotirmoy.attendx.settings.presentation.page.lookandfeel.screens.DarkThemeScreen
import `in`.jyotirmoy.attendx.settings.presentation.page.lookandfeel.screens.LookAndFeelScreen
import `in`.jyotirmoy.attendx.settings.presentation.page.mainscreen.screen.SettingsScreen
import `in`.jyotirmoy.attendx.settings.presentation.page.notification.screens.NotificationScreen
import `in`.jyotirmoy.attendx.main.presentation.MainScreen
import kotlinx.serialization.Serializable

@Composable
fun Navigation() {
    val navController = rememberNavController()
    CompositionLocalProvider(LocalNavController provides navController) {
        NavHost(
            navController = navController, startDestination = MainScreenRoute
        ) {
            composable<MainScreenRoute>(
                exitTransition = { sharedAxisXExit() },
                popEnterTransition = { sharedAxisXPopEnter() }
            ) {
                MainScreen()
            }

            composable<CalendarScreen>(
                enterTransition = { sharedAxisXEnter() },
                popExitTransition = { sharedAxisXPopExit() }
            ) {
                CalendarScreen()
            }

            composable<SettingsScreen>(
                enterTransition = { slideFadeInFromRight() },
                exitTransition = { slideFadeOutToLeft() },
                popEnterTransition = { slideFadeInFromLeft() },
                popExitTransition = { slideFadeOutToRight() }
            ) {
                SettingsScreen()
            }

            composable<LookAndFeelScreen>(
                enterTransition = { slideFadeInFromRight() },
                exitTransition = { slideFadeOutToLeft() },
                popEnterTransition = { slideFadeInFromLeft() },
                popExitTransition = { slideFadeOutToRight() }
            ) {
                LookAndFeelScreen()
            }

            composable<DarkThemeScreen>(
                enterTransition = { slideFadeInFromRight() },
                popExitTransition = { slideFadeOutToRight() }
            ) {
                DarkThemeScreen()
            }

            composable<BehaviorScreen>(
                enterTransition = { slideFadeInFromRight() },
                exitTransition = { slideFadeOutToLeft() },
                popEnterTransition = { slideFadeInFromLeft() },
                popExitTransition = { slideFadeOutToRight() }
            ) {
                BehaviorScreen()
            }

            composable<AboutScreen>(
                enterTransition = { slideFadeInFromRight() },
                exitTransition = { slideFadeOutToLeft() },
                popEnterTransition = { slideFadeInFromLeft() },
                popExitTransition = { slideFadeOutToRight() }
            ) {
                AboutScreen()
            }







            composable<BackupAndRestoreScreen>(
                enterTransition = { slideFadeInFromRight() },
                popExitTransition = { slideFadeOutToRight() }
            ) {
                BackupAndRestoreScreen()
            }

            composable<NotificationScreen>(
                enterTransition = { slideFadeInFromRight() },
                popExitTransition = { slideFadeOutToRight() }
            ) {
                NotificationScreen()
            }

            composable<TimeTableScreen>(
                enterTransition = { slideFadeInFromRight() },
                exitTransition = { slideFadeOutToLeft() },
                popEnterTransition = { slideFadeInFromLeft() },
                popExitTransition = { slideFadeOutToRight() }
            ) {
                `in`.jyotirmoy.attendx.timetable.presentation.screens.TimeTableScreen()
            }
        }
    }
}

@Serializable
object MainScreenRoute

@Serializable
object HomeScreen

@Serializable
data class CalendarScreen(
    val subjectId: Int, val subject: String
)

@Serializable
object SettingsScreen

@Serializable
object LookAndFeelScreen

@Serializable
object DarkThemeScreen

@Serializable
object AboutScreen



@Serializable
object BehaviorScreen

@Serializable
object BackupAndRestoreScreen

@Serializable
object NotificationScreen

@Serializable
object TimeTableScreen