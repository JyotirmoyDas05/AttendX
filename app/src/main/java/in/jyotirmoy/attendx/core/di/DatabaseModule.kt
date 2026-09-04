package `in`.jyotirmoy.attendx.core.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import `in`.jyotirmoy.attendx.core.data.database.AttendanceDao
import `in`.jyotirmoy.attendx.core.data.database.ClassScheduleDao
import `in`.jyotirmoy.attendx.core.data.database.CourseMaterialDao
import `in`.jyotirmoy.attendx.core.data.database.SubjectDao
import `in`.jyotirmoy.attendx.core.data.database.SubjectDatabase
import `in`.jyotirmoy.attendx.core.data.database.SubjectTaskDao
import `in`.jyotirmoy.attendx.timetable.data.dao.TimeTableDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SubjectDatabase =
        SubjectDatabase.getDatabase(context)

    @Provides
    fun provideSubjectDao(db: SubjectDatabase): SubjectDao = db.subjectDao()

    @Provides
    fun provideAttendanceDao(db: SubjectDatabase): AttendanceDao = db.attendanceDao()

    @Provides
    fun provideClassScheduleDao(db: SubjectDatabase): ClassScheduleDao = db.classScheduleDao()

    @Provides
    fun provideTimeTableDao(db: SubjectDatabase): TimeTableDao = db.timeTableDao()

    @Provides
    fun provideSubjectTaskDao(db: SubjectDatabase): SubjectTaskDao = db.subjectTaskDao()

    @Provides
    fun provideCourseMaterialDao(db: SubjectDatabase): CourseMaterialDao = db.courseMaterialDao()
}


