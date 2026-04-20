package `in`.jyotirmoy.attendx.peer.domain.model

data class PeerComparisonData(
    val averageAttendance: Float,
    val stdDev: Float,
    val sampleSize: Int,
    val userPercentile: Float,
    val isStale: Boolean = false,
    val fetchedAt: Long = 0L
)

sealed class PeerComparisonResult {
    data class Success(val data: PeerComparisonData) : PeerComparisonResult()
    object InsufficientPeers : PeerComparisonResult()
    object OptedOut : PeerComparisonResult()
    data class Error(val message: String) : PeerComparisonResult()
    object Loading : PeerComparisonResult()
}
