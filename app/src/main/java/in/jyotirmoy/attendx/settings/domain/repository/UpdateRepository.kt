package `in`.jyotirmoy.attendx.settings.domain.repository

import `in`.jyotirmoy.attendx.settings.domain.model.GitHubRelease
import `in`.jyotirmoy.attendx.settings.domain.model.UpdateResult

interface UpdateRepository {
    suspend fun fetchLatestRelease(includePrerelease: Boolean): UpdateResult
    suspend fun fetchAllReleases(forceRefresh: Boolean = false): Result<List<GitHubRelease>>
}