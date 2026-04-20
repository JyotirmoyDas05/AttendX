package `in`.jyotirmoy.attendx.peer.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface PeerComparisonCacheDao {
    @Query("SELECT * FROM peer_comparison_cache WHERE groupKey = :key")
    fun observeCache(key: String): Flow<PeerComparisonCacheEntity?>

    @Upsert
    suspend fun upsert(entity: PeerComparisonCacheEntity)

    @Query("DELETE FROM peer_comparison_cache WHERE groupKey = :key")
    suspend fun delete(key: String)
}
