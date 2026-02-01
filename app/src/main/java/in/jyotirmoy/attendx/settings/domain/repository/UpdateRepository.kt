package `in`.jyotirmoy.attendx.settings.domain.repository

import `in`.jyotirmoy.attendx.settings.domain.model.UpdateResult

interface UpdateRepository {
    suspend fun fetchLatestRelease(includePrerelease:Boolean): UpdateResult
}