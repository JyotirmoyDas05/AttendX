package `in`.jyotirmoy.attendx.peer.domain.repository

import `in`.jyotirmoy.attendx.peer.data.model.PeerGroup
import kotlinx.coroutines.flow.Flow

interface PeerGroupRepository {
    fun searchGroups(query: String): Flow<List<PeerGroup>>
    suspend fun createGroup(college: String, department: String, semester: Int): Result<PeerGroup>
    suspend fun getGroup(groupId: String): Result<PeerGroup>
}
