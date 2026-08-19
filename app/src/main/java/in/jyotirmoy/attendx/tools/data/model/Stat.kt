package `in`.jyotirmoy.attendx.tools.data.model

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Immutable
@Entity(tableName = "pomodoro_stat")
data class Stat(
    @PrimaryKey
    val date: LocalDate,
    val focusTimeQ1: Long = 0L,
    val focusTimeQ2: Long = 0L,
    val focusTimeQ3: Long = 0L,
    val focusTimeQ4: Long = 0L,
    val breakTime: Long = 0L
) {
    fun totalFocusTime(): Long = focusTimeQ1 + focusTimeQ2 + focusTimeQ3 + focusTimeQ4
    fun totalBreakTime(): Long = breakTime
}

data class StatTime(
    val focusTimeQ1: Long = 0L,
    val focusTimeQ2: Long = 0L,
    val focusTimeQ3: Long = 0L,
    val focusTimeQ4: Long = 0L,
    val breakTime: Long = 0L
)
