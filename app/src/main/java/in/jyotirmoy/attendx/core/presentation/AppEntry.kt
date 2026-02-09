package `in`.jyotirmoy.attendx.core.presentation

import android.util.Log
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.jyotirmoy.attendx.BuildConfig
import `in`.jyotirmoy.attendx.core.common.LocalSettings
import `in`.jyotirmoy.attendx.core.presentation.components.bottomsheet.ChangelogBottomSheet
import `in`.jyotirmoy.attendx.core.presentation.components.bottomsheet.UpdateBottomSheet
import `in`.jyotirmoy.attendx.navigation.Navigation
import `in`.jyotirmoy.attendx.settings.data.local.SettingsKeys
import `in`.jyotirmoy.attendx.settings.domain.model.UpdateResult
import `in`.jyotirmoy.attendx.core.utils.DeviceArchitecture
import `in`.jyotirmoy.attendx.settings.presentation.page.autoupdate.viewmodel.AutoUpdateViewModel
import `in`.jyotirmoy.attendx.settings.presentation.viewmodel.SettingsViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AppEntry(
    autoUpdateViewModel: AutoUpdateViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    var showUpdateSheet by rememberSaveable { mutableStateOf(false) }
    var showChangelogSheet by rememberSaveable { mutableStateOf(false) }
    var tagName by rememberSaveable { mutableStateOf(BuildConfig.VERSION_NAME) }
    var releaseNotes by rememberSaveable { mutableStateOf("") }
    var apkUrl by rememberSaveable { mutableStateOf("") }
    val savedVersionCode = LocalSettings.current.savedVersionCode

    LaunchedEffect(Unit) {
        autoUpdateViewModel.updateEvents.collectLatest { result ->
            if (result is UpdateResult.Success && result.isUpdateAvailable) {
                tagName = result.release.tagName
                releaseNotes = result.release.releaseNotes
                val bestApk = DeviceArchitecture.selectBestApk(result.release.assets)
                apkUrl = bestApk?.downloadUrl ?: ""
                Log.d("AppEntry", "Update found: $tagName")
                Log.d("AppEntry", "Selected APK: ${bestApk?.name} (${bestApk?.architecture})")
                Log.d("AppEntry", "APK URL: $apkUrl")
                showUpdateSheet = bestApk != null
            }
        }
    }

    // Check for updates on app start
    LaunchedEffect(Unit) {
        settingsViewModel.getInt(SettingsKeys.GITHUB_RELEASE_TYPE).collectLatest { type ->
            autoUpdateViewModel.checkForUpdates(type == `in`.jyotirmoy.attendx.core.domain.model.GithubReleaseType.PRE_RELEASE)
        }
    }

    LaunchedEffect(savedVersionCode) {
        showChangelogSheet = savedVersionCode < BuildConfig.VERSION_CODE
    }

    Surface {
        Navigation()

        if (showUpdateSheet) {
            UpdateBottomSheet(
                onDismiss = { showUpdateSheet = false },
                latestVersion = tagName,
                releaseNotes = releaseNotes,
                apkUrl = apkUrl,
            )
        }

        if (showChangelogSheet) {
            ChangelogBottomSheet(
                onDismiss = {
                    showChangelogSheet = false
                    settingsViewModel.setInt(
                        SettingsKeys.SAVED_VERSION_CODE,
                        BuildConfig.VERSION_CODE
                    )
                }
            )
        }
    }
}
