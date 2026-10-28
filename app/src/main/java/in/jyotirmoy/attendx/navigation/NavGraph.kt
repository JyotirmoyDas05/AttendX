package `in`.jyotirmoy.attendx.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import `in`.jyotirmoy.attendx.calender.presentation.screens.CalendarScreen
import `in`.jyotirmoy.attendx.home.presentation.screens.HomeScreen
import `in`.jyotirmoy.attendx.peer.presentation.PeerInsightsScreen
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

import android.content.Intent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import `in`.jyotirmoy.attendx.timetable.presentation.screens.template.update.TemplateUpdateDialog
import `in`.jyotirmoy.attendx.timetable.presentation.screens.template.update.TemplateUpdateViewModel

@Composable
fun Navigation(
    notificationIntent: Intent? = null,
    onNotificationIntentHandled: () -> Unit = {},
    /** Set while AppEntry is already showing its own startup sheet. */
    suppressDialogs: Boolean = false
) {
    val navController = rememberNavController()
    val templateUpdateViewModel: TemplateUpdateViewModel = hiltViewModel()
    val pendingTemplateUpdate by templateUpdateViewModel.pending.collectAsStateWithLifecycle()
    var showTemplateUpdateDialog by rememberSaveable { mutableStateOf(false) }

    // Throttled to one remote read per 24h inside the use case, so this is not a
    // Firestore hit on every cold start.
    LaunchedEffect(Unit) { templateUpdateViewModel.check() }

    LaunchedEffect(pendingTemplateUpdate) {
        if (pendingTemplateUpdate != null) showTemplateUpdateDialog = true
    }

    // Badge taps in the timetable header re-open the dialog.
    LaunchedEffect(Unit) {
        templateUpdateViewModel.showRequests.collect { showTemplateUpdateDialog = true }
    }

    LaunchedEffect(notificationIntent) {
        // From the Schedule widget: a class on a specific date, or the add-class sheet. Back to the
        // main screen first, so the Timetable tab (and its nav bar) is what opens.
        if (notificationIntent != null && notificationIntent.getStringExtra("EXTRA_NAVIGATE_TO") == "TIMETABLE") {
            navController.popBackStack(MainScreenRoute, inclusive = false)
            TimetableLaunch.pending.value = TimetableLaunchRequest(
                scheduleId = notificationIntent.getIntExtra("EXTRA_SCHEDULE_ID", -1),
                date = notificationIntent.getStringExtra("EXTRA_DATE")?.let { runCatching { java.time.LocalDate.parse(it) }.getOrNull() },
                addClass = notificationIntent.getBooleanExtra("EXTRA_ADD_CLASS", false)
            )
            onNotificationIntentHandled()
        }
        if (notificationIntent != null && notificationIntent.getStringExtra("EXTRA_NAVIGATE_TO") == "CALENDAR") {
            val subjectId = notificationIntent.getIntExtra("EXTRA_SUBJECT_ID", -1)
            val subjectName = notificationIntent.getStringExtra("EXTRA_SUBJECT_NAME") ?: ""
            if (subjectId != -1) {
                navController.navigate(CalendarScreen(subjectId = subjectId, subject = subjectName))
                onNotificationIntentHandled()
            }
        }
    }

    CompositionLocalProvider(LocalNavController provides navController) {
        pendingTemplateUpdate?.let { info ->
            if (showTemplateUpdateDialog && !suppressDialogs) {
                TemplateUpdateDialog(
                    info = info,
                    onReview = {
                        showTemplateUpdateDialog = false
                        templateUpdateViewModel.onReviewing()
                        // The preview screen's existing per-item checkboxes are the
                        // review UI; importing there rewrites the stored version.
                        navController.navigate(TemplatePreviewScreen(info.templateId))
                    },
                    onNotNow = {
                        showTemplateUpdateDialog = false
                        templateUpdateViewModel.dismissVersion()
                    },
                    onStopChecking = {
                        showTemplateUpdateDialog = false
                        templateUpdateViewModel.stopChecking()
                    }
                )
            }
        }

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

            composable<LiquidGlassScreenRoute>(
                enterTransition = { slideFadeInFromRight() },
                popExitTransition = { slideFadeOutToRight() }
            ) {
                `in`.jyotirmoy.attendx.settings.presentation.page.lookandfeel.screens.LiquidGlassSettingsScreen()
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
                    },
                    onNavigateToUpload = { navController.navigate(UploadTemplateScreen) }
                )
            }

            composable<TemplatePreviewScreen> {
                `in`.jyotirmoy.attendx.timetable.presentation.screens.template.preview.TemplatePreviewScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable<ToolsScreen>(
                enterTransition = { slideFadeInFromRight() },
                exitTransition = { slideFadeOutToLeft() },
                popEnterTransition = { slideFadeInFromLeft() },
                popExitTransition = { slideFadeOutToRight() }
            ) {
                `in`.jyotirmoy.attendx.tools.presentation.ToolsHubScreen(
                    onNavigateToPomodoro = { navController.navigate(PomodoroScreenRoute) }
                )
            }

            composable<PomodoroScreenRoute>(
                enterTransition = { slideFadeInFromRight() },
                exitTransition = { slideFadeOutToLeft() },
                popEnterTransition = { slideFadeInFromLeft() },
                popExitTransition = { slideFadeOutToRight() }
            ) {
                `in`.jyotirmoy.attendx.tools.presentation.pomodoro.PomodoroScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable<PeerInsightsRoute>(
                enterTransition = { slideFadeInFromRight() },
                popExitTransition = { slideFadeOutToRight() }
            ) {
                PeerInsightsScreen(
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
object LiquidGlassScreenRoute

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

@Serializable
object ToolsScreen

@Serializable
object PomodoroScreenRoute

@Serializable
object PeerInsightsRoute