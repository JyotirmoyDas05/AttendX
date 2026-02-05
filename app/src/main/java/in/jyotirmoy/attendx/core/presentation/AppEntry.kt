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
    var apkUrl by rememberSaveable { mutableStateOf("") }
    val savedVersionCode = LocalSettings.current.savedVersionCode

    LaunchedEffect(Unit) {
        autoUpdateViewModel.updateEvents.collectLatest { result ->
            if (result is UpdateResult.Success && result.isUpdateAvailable) {
                tagName = result.release.tagName
                val bestApk = DeviceArchitecture.selectBestApk(result.release.assets)
                apkUrl = bestApk?.downloadUrl ?: ""
                Log.d("AppEntry", "Selected APK: ${bestApk?.name} (${bestApk?.architecture})")
                showUpdateSheet = bestApk != null
            }
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
