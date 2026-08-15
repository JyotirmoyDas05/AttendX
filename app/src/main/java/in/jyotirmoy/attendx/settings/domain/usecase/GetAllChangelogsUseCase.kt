package `in`.jyotirmoy.attendx.settings.domain.usecase

import `in`.jyotirmoy.attendx.settings.domain.model.GitHubRelease
import `in`.jyotirmoy.attendx.settings.domain.repository.UpdateRepository
import javax.inject.Inject

class GetAllChangelogsUseCase @Inject constructor(
    private val updateRepository: UpdateRepository
) {
    suspend operator fun invoke(forceRefresh: Boolean = false): Result<List<GitHubRelease>> {
        return updateRepository.fetchAllReleases(forceRefresh)
    }
}