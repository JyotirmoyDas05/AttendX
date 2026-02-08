package `in`.jyotirmoy.attendx.settings.presentation.page.autoupdate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.jyotirmoy.attendx.BuildConfig
import `in`.jyotirmoy.attendx.core.domain.model.DownloadState
import `in`.jyotirmoy.attendx.core.domain.usecase.DownloadApkUseCase
import `in`.jyotirmoy.attendx.core.utils.DeviceArchitecture
import `in`.jyotirmoy.attendx.settings.data.local.SettingsKeys
import `in`.jyotirmoy.attendx.settings.domain.model.UpdateResult
import `in`.jyotirmoy.attendx.settings.domain.repository.SettingsRepository
import `in`.jyotirmoy.attendx.settings.domain.usecase.CheckUpdateUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AutoUpdateViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val checkUpdateUseCase: CheckUpdateUseCase,
    private val downloadApkUseCase: DownloadApkUseCase
) : ViewModel() {
    private val _updateEvents = MutableSharedFlow<UpdateResult>()
    val updateEvents = _updateEvents.asSharedFlow()

    // persistent update state for UI
    private val _isUpdateAvailable = MutableStateFlow(false)
    val isUpdateAvailable = _isUpdateAvailable.asStateFlow()

    private val _latestVersion = MutableStateFlow("")
    val latestVersion = _latestVersion.asStateFlow()

    private val _apkUrl = MutableStateFlow("")
    val apkUrl = _apkUrl.asStateFlow()

    fun checkForUpdates(includePrerelease: Boolean) {
        viewModelScope.launch {
            val result = checkUpdateUseCase(BuildConfig.VERSION_NAME, includePrerelease)
            
            // persist update info
            if (result is UpdateResult.Success && result.isUpdateAvailable) {
                _isUpdateAvailable.value = true
                _latestVersion.value = result.release.tagName
                val bestApk = DeviceArchitecture.selectBestApk(result.release.assets)
                _apkUrl.value = bestApk?.downloadUrl ?: ""
            }
            
            _updateEvents.emit(result)
        }
    }

    fun select(option: Int) {
        viewModelScope.launch {
            settingsRepository.setInt(SettingsKeys.GITHUB_RELEASE_TYPE, option)
        }
    }

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState = _downloadState.asStateFlow()

    fun downloadApk(url: String, fileName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            downloadApkUseCase(url, fileName) {
                _downloadState.value = it
            }
        }
    }

    fun cancelDownload() {
        downloadApkUseCase.cancel()
    }

    fun showUpdateSheet(): Boolean {
        return _isUpdateAvailable.value && _apkUrl.value.isNotEmpty()
    }
}