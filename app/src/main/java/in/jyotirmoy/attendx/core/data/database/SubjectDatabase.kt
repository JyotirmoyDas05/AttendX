package `in`.jyotirmoy.attendx.core.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import `in`.jyotirmoy.attendx.core.data.model.AttendanceEntity
import `in`.jyotirmoy.attendx.core.data.model.ClassScheduleEntity
import `in`.jyotirmoy.attendx.core.data.model.SubjectEntity
import `in`.jyotirmoy.attendx.core.data.model.CourseMaterialEntity
import `in`.jyotirmoy.attendx.core.data.model.SubjectTaskEntity
import `in`.jyotirmoy.attendx.timetable.data.model.TimeTableScheduleEntity
import `in`.jyotirmoy.attendx.timetable.data.dao.TimeTableDao

@Database(
    entities = [
        SubjectEntity::class,
        AttendanceEntity::class,
        ClassScheduleEntity::class,
        TimeTableScheduleEntity::class,
        SubjectTaskEntity::class,
        CourseMaterialEntity::class
    ],
    version = 13,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SubjectDatabase : RoomDatabase() {
    abstract fun subjectDao(): SubjectDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun classScheduleDao(): ClassScheduleDao
    abstract fun timeTableDao(): TimeTableDao
    abstract fun subjectTaskDao(): SubjectTaskDao
    abstract fun courseMaterialDao(): CourseMaterialDao

    companion object {
        @Volatile
        private var INSTANCE: SubjectDatabase? = null

        /**
         * The single Room instance for the app. Hilt's DatabaseModule delegates here so
         * that DI callers and non-DI callers (BroadcastReceivers, Workers) can never end
         * up on different database files or migration sets.
         */
        fun getDatabase(context: Context): SubjectDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SubjectDatabase::class.java,
                    "attendance_app_db"
                )
                    .addMigrations(
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_5_6,
                        MIGRATION_6_7,
                        MIGRATION_7_8,
                        MIGRATION_8_9,
                        MIGRATION_9_10,
                        MIGRATION_10_11,
                        MIGRATION_11_12,
                        MIGRATION_12_13
                    )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
