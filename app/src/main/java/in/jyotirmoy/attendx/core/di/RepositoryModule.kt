package `in`.jyotirmoy.attendx.core.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import `in`.jyotirmoy.attendx.core.data.repository.AttendanceRepositoryImpl
import `in`.jyotirmoy.attendx.core.data.repository.ClassScheduleRepositoryImpl
import `in`.jyotirmoy.attendx.core.data.repository.SubjectRepositoryImpl
import `in`.jyotirmoy.attendx.core.domain.repository.AttendanceRepository
import `in`.jyotirmoy.attendx.core.domain.repository.ClassScheduleRepository
import `in`.jyotirmoy.attendx.core.domain.repository.SubjectRepository
import `in`.jyotirmoy.attendx.settings.data.local.repository.BackupAndRestoreRepositoryImpl
import `in`.jyotirmoy.attendx.settings.domain.repository.BackupAndRestoreRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindSubjectRepository(
        subjectRepositoryImpl: SubjectRepositoryImpl
    ): SubjectRepository

    @Binds
    @Singleton
    abstract fun bindAttendanceRepository(
        attendanceRepositoryImpl: AttendanceRepositoryImpl
    ): AttendanceRepository

    @Binds
    @Singleton
    abstract fun bindClassScheduleRepository(
        classScheduleRepositoryImpl: ClassScheduleRepositoryImpl
    ): ClassScheduleRepository

    @Binds
    @Singleton
    abstract fun bindBackupAndRestoreRepository(
        backupAndRestoreRepositoryImpl: BackupAndRestoreRepositoryImpl
    ): BackupAndRestoreRepository
}

