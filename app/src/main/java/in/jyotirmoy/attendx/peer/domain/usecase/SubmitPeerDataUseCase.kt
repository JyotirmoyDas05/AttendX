package `in`.jyotirmoy.attendx.peer.domain.usecase

import `in`.jyotirmoy.attendx.peer.data.repository.PeerComparisonRepository
import javax.inject.Inject

class SubmitPeerDataUseCase @Inject constructor(
    private val repository: PeerComparisonRepository
) {
    suspend operator fun invoke(
        groupId: String,
        attendancePercentage: Float
    ): Result<Unit> = repository.submitAttendanceData(groupId, attendancePercentage)
}
