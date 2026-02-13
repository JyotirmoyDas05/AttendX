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
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.ui.graphics.TransformOrigin
import androidx.navigation.toRoute
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
                `in`.jyotirmoy.attendx.timetable.presentation.screens.TimeTableScreen(
                    onNavigateToUpload = { navController.navigate(UploadTemplateScreen) },
                    onNavigateToMarketplace = { navController.navigate(MarketplaceScreen()) }
                )
            }

            composable<UploadTemplateScreen>(
                enterTransition = { slideFadeInFromRight() },
                popExitTransition = { slideFadeOutToRight() }
            ) {
                `in`.jyotirmoy.attendx.timetable.presentation.screens.template.UploadTemplateScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable<MarketplaceScreen>(
                enterTransition = { 
                    val args = targetState.toRoute<MarketplaceScreen>()
                    scaleIn(
                        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
                        transformOrigin = TransformOrigin(args.pivotX, args.pivotY)
                    ) + fadeIn(animationSpec = tween(durationMillis = 400))
                },
                popExitTransition = { 
                    val args = initialState.toRoute<MarketplaceScreen>()
                    scaleOut(
                        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
                        transformOrigin = TransformOrigin(args.pivotX, args.pivotY)
                    ) + fadeOut(animationSpec = tween(durationMillis = 400))
                }
            ) {
                `in`.jyotirmoy.attendx.timetable.presentation.screens.template.MarketplaceScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onTemplateClick = { template -> 
                        navController.navigate(TemplatePreviewScreen(template.id))
                    }
                )
            }

            composable<TemplatePreviewScreen> {
                `in`.jyotirmoy.attendx.timetable.presentation.screens.template.preview.TemplatePreviewScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
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

@Serializable
object UploadTemplateScreen

@Serializable
data class MarketplaceScreen(
    val pivotX: Float = 0.5f,
    val pivotY: Float = 0.5f
)

@Serializable
data class TemplatePreviewScreen(val templateId: String)