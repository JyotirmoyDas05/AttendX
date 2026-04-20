package `in`.jyotirmoy.attendx.peer.data.model

import com.google.firebase.Timestamp

data class PeerGroup(
    val id: String = "",
    val displayName: String = "",
    val college: String = "",
    val department: String = "",
    val semester: Int = 0,
    val memberCount: Int = 0,
    val createdAt: Timestamp = Timestamp.now()
)
