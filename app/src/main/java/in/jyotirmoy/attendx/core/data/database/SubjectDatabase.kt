package `in`.jyotirmoy.attendx.core.data.database


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import `in`.jyotirmoy.attendx.core.data.model.AttendanceEntity
import `in`.jyotirmoy.attendx.core.data.model.ClassScheduleEntity
import `in`.jyotirmoy.attendx.core.data.model.SubjectEntity
import `in`.jyotirmoy.attendx.timetable.data.model.TimeTableScheduleEntity
import `in`.jyotirmoy.attendx.timetable.data.dao.TimeTableDao
import `in`.jyotirmoy.attendx.core.data.database.Converters

@Database(
    entities = [
        SubjectEntity::class, 
        AttendanceEntity::class, 
        ClassScheduleEntity::class, 
        TimeTableScheduleEntity::class
    ],
    version = 9,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SubjectDatabase : RoomDatabase() {
    abstract fun subjectDao(): SubjectDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun classScheduleDao(): ClassScheduleDao
    abstract fun timeTableDao(): TimeTableDao

    companion object {
        @Volatile
        private var INSTANCE: SubjectDatabase? = null

        /**
         * Get database instance for non-DI contexts (e.g., BroadcastReceiver)
         */
        fun getDatabase(context: Context): SubjectDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SubjectDatabase::class.java,
                    "subject_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

