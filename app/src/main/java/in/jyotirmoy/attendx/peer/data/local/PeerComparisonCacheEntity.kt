package `in`.jyotirmoy.attendx.peer.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "peer_comparison_cache")
data class PeerComparisonCacheEntity(
    @PrimaryKey val groupKey: String,
    val averageAttendance: Float,
    val stdDev: Float,
    val sampleSize: Int,
    val userPercentile: Float,
    val fetchedAt: Long
)
