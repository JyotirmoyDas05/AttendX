package `in`.jyotirmoy.attendx.peer.domain.usecase

import `in`.jyotirmoy.attendx.peer.data.model.PeerGroup
import `in`.jyotirmoy.attendx.peer.domain.repository.PeerGroupRepository
import javax.inject.Inject

class JoinOrCreatePeerGroupUseCase @Inject constructor(
    private val repository: PeerGroupRepository
) {
    /** Join an existing group — just returns it; memberCount incremented on first attendance submit. */
    suspend fun joinExisting(group: PeerGroup): Result<PeerGroup> = Result.success(group)

    /** Create a brand-new canonical group. */
    suspend fun createNew(
        college: String,
        department: String,
        semester: Int
    ): Result<PeerGroup> = repository.createGroup(college, department, semester)
}
