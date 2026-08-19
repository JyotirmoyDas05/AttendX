package `in`.jyotirmoy.attendx.tools.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pomodoro_topics")
data class Topic(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val colorHex: String = "#3B82F6",
    val shapeIndex: Int = 0,
    val focusTime: Long = 25 * 60 * 1000L,
    val shortBreakTime: Long = 5 * 60 * 1000L,
    val longBreakTime: Long = 15 * 60 * 1000L
)
