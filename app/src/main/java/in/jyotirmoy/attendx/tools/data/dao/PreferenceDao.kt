package `in`.jyotirmoy.attendx.tools.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import `in`.jyotirmoy.attendx.tools.data.model.BooleanPreference
import `in`.jyotirmoy.attendx.tools.data.model.IntPreference
import `in`.jyotirmoy.attendx.tools.data.model.StringPreference

@Dao
interface PreferenceDao {
    @Insert(onConflict = REPLACE)
    suspend fun insertBooleanPreference(preference: BooleanPreference)

    @Insert(onConflict = REPLACE)
    suspend fun insertIntPreference(preference: IntPreference)

    @Insert(onConflict = REPLACE)
    suspend fun insertStringPreference(preference: StringPreference)

    @Query("SELECT value FROM pomodoro_boolean_preference WHERE `key` = :key")
    suspend fun getBooleanPreference(key: String): Boolean?

    @Query("SELECT value FROM pomodoro_int_preference WHERE `key` = :key")
    suspend fun getIntPreference(key: String): Int?

    @Query("SELECT value FROM pomodoro_string_preference WHERE `key` = :key")
    suspend fun getStringPreference(key: String): String?
}
