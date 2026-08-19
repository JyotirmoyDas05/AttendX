package `in`.jyotirmoy.attendx.tools.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import `in`.jyotirmoy.attendx.core.data.database.Converters
import `in`.jyotirmoy.attendx.tools.data.dao.PreferenceDao
import `in`.jyotirmoy.attendx.tools.data.dao.StatDao
import `in`.jyotirmoy.attendx.tools.data.dao.TopicDao
import `in`.jyotirmoy.attendx.tools.data.model.BooleanPreference
import `in`.jyotirmoy.attendx.tools.data.model.IntPreference
import `in`.jyotirmoy.attendx.tools.data.model.Stat
import `in`.jyotirmoy.attendx.tools.data.model.StringPreference
import `in`.jyotirmoy.attendx.tools.data.model.Topic

@Database(
    entities = [
        Stat::class,
        BooleanPreference::class,
        IntPreference::class,
        StringPreference::class,
        Topic::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PomodoroDatabase : RoomDatabase() {
    abstract fun statDao(): StatDao
    abstract fun preferenceDao(): PreferenceDao
    abstract fun topicDao(): TopicDao

    companion object {
        @Volatile
        private var INSTANCE: PomodoroDatabase? = null

        fun getDatabase(context: Context): PomodoroDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PomodoroDatabase::class.java,
                    "pomodoro_database"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
