package `in`.jyotirmoy.attendx.core.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import `in`.jyotirmoy.attendx.calender.domain.usecase.GetWeekDayLabelsUseCase
import `in`.jyotirmoy.attendx.core.domain.repository.DownloadRepository
import `in`.jyotirmoy.attendx.core.domain.usecase.DownloadApkUseCase
import `in`.jyotirmoy.attendx.settings.domain.usecase.GetAllChangelogsUseCase
import `in`.jyotirmoy.attendx.timetable.domain.usecase.TimeTableUseCases
import `in`.jyotirmoy.attendx.timetable.domain.repository.TimeTableRepository
import `in`.jyotirmoy.attendx.timetable.domain.usecase.GetDailyScheduleUseCase
import `in`.jyotirmoy.attendx.timetable.domain.usecase.GetNextClassUseCase
import `in`.jyotirmoy.attendx.timetable.domain.usecase.GetCurrentClassUseCase
import `in`.jyotirmoy.attendx.timetable.domain.usecase.DetectTimeClashUseCase
import `in`.jyotirmoy.attendx.timetable.domain.usecase.AddClassSlotUseCase
import `in`.jyotirmoy.attendx.timetable.domain.usecase.DeleteClassUseCase
import `in`.jyotirmoy.attendx.timetable.presentation.notification.TimeTableNotificationScheduler

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    @Provides
    fun provideGetWeekDayLabelsUseCase(): GetWeekDayLabelsUseCase =
        GetWeekDayLabelsUseCase()

    @Provides
    fun provideGetChangelogsUseCase(@ApplicationContext context: Context): GetAllChangelogsUseCase =
        GetAllChangelogsUseCase(context)

    @Provides
    fun provideDownloadApkUseCase(repo: DownloadRepository): DownloadApkUseCase =
        DownloadApkUseCase(repo)

    @Provides
    fun provideTimeTableUseCases(
        repository: TimeTableRepository,
        scheduler: TimeTableNotificationScheduler,
        @ApplicationContext context: Context
    ): TimeTableUseCases {
        return TimeTableUseCases(
            getDailySchedule = GetDailyScheduleUseCase(repository),
            getNextClass = GetNextClassUseCase(repository),
            getCurrentClass = GetCurrentClassUseCase(repository),
            detectTimeClash = DetectTimeClashUseCase(repository),
            addClassSlot = AddClassSlotUseCase(repository, scheduler, context),
            deleteClass = DeleteClassUseCase(repository)
        )
    }
}