package `in`.jyotirmoy.attendx.settings.presentation.page.changelog.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.jyotirmoy.attendx.BuildConfig
import `in`.jyotirmoy.attendx.core.utils.VersionComparator
import `in`.jyotirmoy.attendx.settings.domain.model.GitHubRelease
import `in`.jyotirmoy.attendx.settings.domain.usecase.GetAllChangelogsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// lazy dev: changelog vm
@HiltViewModel
class ChangelogViewModel @Inject constructor(
    private val getAllChangelogsUseCase: GetAllChangelogsUseCase
) : ViewModel() {

    private val _releases = MutableStateFlow<List<GitHubRelease>>(emptyList())
    val releases: StateFlow<List<GitHubRelease>> = _releases.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadChangelogs()
    }

    fun loadChangelogs(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            getAllChangelogsUseCase(forceRefresh)
                .onSuccess { allReleases ->
                    val currentVersion = BuildConfig.VERSION_NAME
                    _releases.value = allReleases.filter { release ->
                        VersionComparator.compare(currentVersion, release.tagName) >= 0
                    }
                    _isLoading.value = false
                }
                .onFailure {
                    _isLoading.value = false
                }
        }
    }
}