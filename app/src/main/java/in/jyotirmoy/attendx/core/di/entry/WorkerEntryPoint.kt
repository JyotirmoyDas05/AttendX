package `in`.jyotirmoy.attendx.core.di.entry

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import `in`.jyotirmoy.attendx.core.domain.repository.AttendanceRepository
import `in`.jyotirmoy.attendx.settings.domain.usecase.CheckUpdateUseCase

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WorkerEntryPoint {
    fun checkUpdateUseCase(): CheckUpdateUseCase
    fun attendanceRepository(): AttendanceRepository
}
