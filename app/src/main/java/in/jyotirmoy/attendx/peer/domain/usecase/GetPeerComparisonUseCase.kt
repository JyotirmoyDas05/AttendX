package `in`.jyotirmoy.attendx.peer.domain.usecase

import `in`.jyotirmoy.attendx.peer.data.local.PeerComparisonCacheEntity
import `in`.jyotirmoy.attendx.peer.data.repository.PeerComparisonRepository
import `in`.jyotirmoy.attendx.peer.domain.model.PeerComparisonData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPeerComparisonUseCase @Inject constructor(
    private val repository: PeerComparisonRepository
) {
    suspend operator fun invoke(
        groupId: String,
        userAttendancePercentage: Float
    ): PeerComparisonData? = repository.getPeerStats(groupId, userAttendancePercentage)

    fun observeCache(groupId: String): Flow<PeerComparisonCacheEntity?> =
        repository.observeCache(groupId)
}
