package `in`.jyotirmoy.attendx.peer.data.model

import com.google.firebase.Timestamp

data class PeerSubjectPayload(
    val subjectCode: String? = null,
    val normalizedName: String = "",   // pre-normalized at write time (lowercase, noise stripped)
    val originalName: String = "",     // shown in UI
    val attendancePercentage: Float = 0f
)

data class PeerSessionDoc(
    val sessionId: String = "",
    val status: String = STATUS_WAITING,
    val hostAnonUid: String = "",
    val guestAnonUid: String? = null,
    val hostData: List<PeerSubjectPayload> = emptyList(),
    val guestData: List<PeerSubjectPayload> = emptyList(),
    val createdAt: Timestamp? = null
) {
    companion object {
        const val STATUS_WAITING = "WAITING"
        const val STATUS_PAIRED  = "PAIRED"
        const val STATUS_EXPIRED = "EXPIRED"
    }
}
