package `in`.jyotirmoy.attendx.peer.domain.model

import `in`.jyotirmoy.attendx.peer.data.model.PeerSubjectPayload

enum class MatchConfidence {
    AUTO_CODE,   // subjectCode matched exactly — highest trust
    AUTO_NAME,   // normalized name matched exactly
    FUZZY,       // token overlap / edit distance suggestion — user must confirm
    MANUAL       // user manually paired
}

data class SubjectMatchProposal(
    val hostSubject: PeerSubjectPayload,
    val guestSubject: PeerSubjectPayload?,  // null = unmatched
    val confidence: MatchConfidence,
    val score: Float                         // 0..1 match score
)

data class SubjectComparisonResult(
    val label: String,
    val hostPercentage: Float,
    val guestPercentage: Float,
    val delta: Float,                        // hostPercentage - guestPercentage
    val confidence: MatchConfidence
)
