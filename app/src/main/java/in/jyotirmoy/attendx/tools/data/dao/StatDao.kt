package `in`.jyotirmoy.attendx.tools.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import `in`.jyotirmoy.attendx.tools.data.model.Stat
import `in`.jyotirmoy.attendx.tools.data.model.StatTime
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface StatDao {
    @Insert(onConflict = REPLACE)
    suspend fun insertStat(stat: Stat)

    @Query("UPDATE pomodoro_stat SET focusTimeQ1 = focusTimeQ1 + :focusTime WHERE date = :date")
    suspend fun addFocusTimeQ1(date: LocalDate, focusTime: Long)

    @Query("UPDATE pomodoro_stat SET focusTimeQ2 = focusTimeQ2 + :focusTime WHERE date = :date")
    suspend fun addFocusTimeQ2(date: LocalDate, focusTime: Long)

    @Query("UPDATE pomodoro_stat SET focusTimeQ3 = focusTimeQ3 + :focusTime WHERE date = :date")
    suspend fun addFocusTimeQ3(date: LocalDate, focusTime: Long)

    @Query("UPDATE pomodoro_stat SET focusTimeQ4 = focusTimeQ4 + :focusTime WHERE date = :date")
    suspend fun addFocusTimeQ4(date: LocalDate, focusTime: Long)

    @Query("UPDATE pomodoro_stat SET breakTime = breakTime + :breakTime WHERE date = :date")
    suspend fun addBreakTime(date: LocalDate, breakTime: Long)

    @Query("SELECT * FROM pomodoro_stat WHERE date = :date")
    fun getStat(date: LocalDate): Flow<Stat?>

    @Query("SELECT * FROM pomodoro_stat ORDER BY date DESC LIMIT :n")
    fun getLastNDaysStats(n: Int): Flow<List<Stat>>

    @Query(
        "SELECT " +
                "    CAST(AVG(focusTimeQ1) AS INTEGER) AS focusTimeQ1," +
                "    CAST(AVG(focusTimeQ2) AS INTEGER) AS focusTimeQ2," +
                "    CAST(AVG(focusTimeQ3) AS INTEGER) AS focusTimeQ3," +
                "    CAST(AVG(focusTimeQ4) AS INTEGER) AS focusTimeQ4," +
                "    CAST(AVG(breakTime) AS INTEGER) AS breakTime " +
                "FROM (" +
                "    SELECT * FROM pomodoro_stat" +
                "    ORDER BY date DESC" +
                "    LIMIT :n" +
                ")" +
                "WHERE focusTimeQ1 > 0 OR focusTimeQ2 > 0 OR focusTimeQ3 > 0 OR focusTimeQ4 > 0"
    )
    fun getLastNDaysAvgStats(n: Int): Flow<StatTime?>

    @Query("SELECT EXISTS (SELECT * FROM pomodoro_stat WHERE date = :date)")
    suspend fun statExists(date: LocalDate): Boolean

    @Query("SELECT date FROM pomodoro_stat ORDER BY date DESC LIMIT 1")
    suspend fun getLastDate(): LocalDate?

    @Query("SELECT SUM(focusTimeQ1 + focusTimeQ2 + focusTimeQ3 + focusTimeQ4) FROM pomodoro_stat")
    fun getAllTimeTotalFocusTime(): Flow<Long?>

    @Query("DELETE FROM pomodoro_stat")
    suspend fun clearAll()
}
