package `in`.jyotirmoy.attendx.peer.domain.usecase

import `in`.jyotirmoy.attendx.peer.data.model.PeerGroup
import `in`.jyotirmoy.attendx.peer.domain.repository.PeerGroupRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchPeerGroupsUseCase @Inject constructor(
    private val repository: PeerGroupRepository
) {
    operator fun invoke(query: String): Flow<List<PeerGroup>> =
        repository.searchGroups(query)
}
