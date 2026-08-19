package `in`.jyotirmoy.attendx.tools.data.repository

import `in`.jyotirmoy.attendx.tools.data.dao.StatDao
import `in`.jyotirmoy.attendx.tools.data.model.Stat
import `in`.jyotirmoy.attendx.tools.data.model.StatTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

interface StatRepository {
    suspend fun insertStat(stat: Stat)
    suspend fun addFocusTime(focusTime: Long)
    suspend fun addBreakTime(breakTime: Long)
    fun getTodayStat(): Flow<Stat?>
    fun getLastNDaysStats(n: Int): Flow<List<Stat>>
    fun getLastNDaysAverageFocusTimes(n: Int): Flow<StatTime?>
    fun getAllTimeTotalFocusTime(): Flow<Long?>
    suspend fun getLastDate(): LocalDate?
    suspend fun deleteAllStats()
}

@Singleton
class AppStatRepository @Inject constructor(
    private val statDao: StatDao
) : StatRepository {
    private val ioDispatcher = Dispatchers.IO

    override suspend fun insertStat(stat: Stat) = statDao.insertStat(stat)

    override suspend fun addFocusTime(focusTime: Long) = withContext(ioDispatcher) {
        val currentDate = LocalDate.now()
        val currentTime = LocalTime.now().toSecondOfDay()
        val secondsInDay = 24 * 60 * 60

        if (statDao.statExists(currentDate)) {
            when (currentTime) {
                in 0..(secondsInDay / 4) -> statDao.addFocusTimeQ1(currentDate, focusTime)
                in (secondsInDay / 4)..(secondsInDay / 2) -> statDao.addFocusTimeQ2(currentDate, focusTime)
                in (secondsInDay / 2)..(3 * secondsInDay / 4) -> statDao.addFocusTimeQ3(currentDate, focusTime)
                else -> statDao.addFocusTimeQ4(currentDate, focusTime)
            }
        } else {
            when (currentTime) {
                in 0..(secondsInDay / 4) -> statDao.insertStat(Stat(currentDate, focusTime, 0, 0, 0, 0))
                in (secondsInDay / 4)..(secondsInDay / 2) -> statDao.insertStat(Stat(currentDate, 0, focusTime, 0, 0, 0))
                in (secondsInDay / 2)..(3 * secondsInDay / 4) -> statDao.insertStat(Stat(currentDate, 0, 0, focusTime, 0, 0))
                else -> statDao.insertStat(Stat(currentDate, 0, 0, 0, focusTime, 0))
            }
        }
    }

    override suspend fun addBreakTime(breakTime: Long) = withContext(ioDispatcher) {
        val currentDate = LocalDate.now()
        if (statDao.statExists(currentDate)) {
            statDao.addBreakTime(currentDate, breakTime)
        } else {
            statDao.insertStat(Stat(currentDate, 0, 0, 0, 0, breakTime))
        }
    }

    override fun getTodayStat(): Flow<Stat?> = statDao.getStat(LocalDate.now())

    override fun getLastNDaysStats(n: Int): Flow<List<Stat>> = statDao.getLastNDaysStats(n)

    override fun getLastNDaysAverageFocusTimes(n: Int): Flow<StatTime?> = statDao.getLastNDaysAvgStats(n)

    override fun getAllTimeTotalFocusTime(): Flow<Long?> = statDao.getAllTimeTotalFocusTime()

    override suspend fun getLastDate(): LocalDate? = withContext(ioDispatcher) { statDao.getLastDate() }

    override suspend fun deleteAllStats() = withContext(ioDispatcher) { statDao.clearAll() }
}
