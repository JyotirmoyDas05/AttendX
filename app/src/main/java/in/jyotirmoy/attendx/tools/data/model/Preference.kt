package `in`.jyotirmoy.attendx.tools.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pomodoro_boolean_preference")
data class BooleanPreference(
    @PrimaryKey
    val key: String,
    val value: Boolean
)

@Entity(tableName = "pomodoro_int_preference")
data class IntPreference(
    @PrimaryKey
    val key: String,
    val value: Int
)

@Entity(tableName = "pomodoro_string_preference")
data class StringPreference(
    @PrimaryKey
    val key: String,
    val value: String
)
