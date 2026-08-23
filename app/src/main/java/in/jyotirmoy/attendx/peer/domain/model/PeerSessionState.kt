package `in`.jyotirmoy.attendx.peer.domain.model

import android.graphics.Bitmap

sealed class PeerSessionState {
    /** Entry screen — no session active */
    object Idle : PeerSessionState()

    /** Host: session created, QR shown, waiting for guest to scan */
    data class Hosting(
        val sessionId: String,
        val qrBitmap: Bitmap,
        val secondsRemaining: Int = 900  // 15-min TTL countdown
    ) : PeerSessionState()

    /** Guest: scanning QR code via camera */
    object Scanning : PeerSessionState()

    /** Guest: session found, writing guestData — brief transition */
    object Joining : PeerSessionState()

    /** Host: guestData arrived — fuzzy proposals computed, awaiting host confirmation */
    data class PendingConfirmation(
        val proposals: List<SubjectMatchProposal>
    ) : PeerSessionState()

    /** Both devices: final per-subject comparison results */
    data class Results(
        val comparisons: List<SubjectComparisonResult>
    ) : PeerSessionState()

    /** Session TTL expired before guest joined */
    object Expired : PeerSessionState()

    data class Error(val message: String) : PeerSessionState()
}
