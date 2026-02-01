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
}