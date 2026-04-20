package `in`.jyotirmoy.attendx.peer.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import `in`.jyotirmoy.attendx.core.data.service.AnonymousAuthService
import `in`.jyotirmoy.attendx.peer.data.local.PeerComparisonCacheDao
import `in`.jyotirmoy.attendx.peer.data.local.PeerComparisonCacheEntity
import `in`.jyotirmoy.attendx.peer.domain.model.PeerComparisonData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.sqrt

private const val K_ANONYMITY_FLOOR = 5
private const val ATTENDANCE_COLLECTION = "peer_attendance"
private const val GROUPS_COLLECTION = "peer_groups"

@Singleton
class PeerComparisonRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authService: AnonymousAuthService,
    private val cacheDao: PeerComparisonCacheDao
) {
    /**
     * Submits attendance as aggregate-only increment, keyed by canonical Firestore groupId.
     * Increments memberCount in peer_groups on first submission for search ranking.
     */
    suspend fun submitAttendanceData(
        groupId: String,
        attendancePercentage: Float
    ): Result<Unit> {
        return try {
            val uid = authService.getOrCreateUid()
            val docRef = firestore.collection(ATTENDANCE_COLLECTION).document(groupId)

            val snapshot = docRef.get().await()
            @Suppress("UNCHECKED_CAST")
            val contributors = snapshot.get("contributorUids") as? List<String> ?: emptyList()
            if (uid in contributors) return Result.success(Unit)

            docRef.set(
                mapOf(
                    "sum" to FieldValue.increment(attendancePercentage.toDouble()),
                    "count" to FieldValue.increment(1L),
                    "sumOfSquares" to FieldValue.increment((attendancePercentage * attendancePercentage).toDouble()),
                    "lastUpdated" to FieldValue.serverTimestamp(),
                    "contributorUids" to FieldValue.arrayUnion(uid)
                ),
                SetOptions.merge()
            ).await()

            // Increment memberCount in peer_groups for search ranking
            firestore.collection(GROUPS_COLLECTION).document(groupId)
                .update("memberCount", FieldValue.increment(1L))
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetches aggregate peer stats by canonical groupId.
     * Returns null if count < K_ANONYMITY_FLOOR (privacy floor).
     */
    suspend fun getPeerStats(
        groupId: String,
        userAttendancePercentage: Float
    ): PeerComparisonData? {
        return try {
            val doc = firestore.collection(ATTENDANCE_COLLECTION).document(groupId).get().await()
            val count = (doc.getLong("count") ?: 0L).toInt()
            if (count < K_ANONYMITY_FLOOR) return null

            val sum = doc.getDouble("sum") ?: 0.0
            val sumOfSquares = doc.getDouble("sumOfSquares") ?: 0.0
            val avg = (sum / count).toFloat()
            val variance = (sumOfSquares / count) - (avg * avg)
            val stdDev = sqrt(variance.coerceAtLeast(0.0)).toFloat()

            val z = if (stdDev > 0) (userAttendancePercentage - avg) / stdDev else 0f
            val percentile = normalCdfApprox(z) * 100f

            val result = PeerComparisonData(
                averageAttendance = avg,
                stdDev = stdDev,
                sampleSize = count,
                userPercentile = percentile,
                fetchedAt = System.currentTimeMillis()
            )

            cacheDao.upsert(
                PeerComparisonCacheEntity(
                    groupKey = groupId,
                    averageAttendance = avg,
                    stdDev = stdDev,
                    sampleSize = count,
                    userPercentile = percentile,
                    fetchedAt = System.currentTimeMillis()
                )
            )

            result
        } catch (e: Exception) {
            null
        }
    }

    fun observeCache(groupId: String): Flow<PeerComparisonCacheEntity?> =
        cacheDao.observeCache(groupId)

    // Abramowitz & Stegun approximation for normal CDF
    private fun normalCdfApprox(z: Float): Float {
        val t = 1f / (1f + 0.2316419f * abs(z))
        val poly = t * (0.319381530f + t * (-0.356563782f + t * (1.781477937f + t * (-1.821255978f + t * 1.330274429f))))
        val pdf = exp(-0.5f * z * z).toFloat() / sqrt(2f * Math.PI.toFloat())
        val cdf = 1f - pdf * poly
        return if (z >= 0) cdf else 1f - cdf
    }
}
