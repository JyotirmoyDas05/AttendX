package `in`.jyotirmoy.attendx.notification.worker

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.jyotirmoy.attendx.BuildConfig
import `in`.jyotirmoy.attendx.core.di.entry.WorkerEntryPoint
import `in`.jyotirmoy.attendx.notification.NotificationSetup
import `in`.jyotirmoy.attendx.settings.domain.model.UpdateResult
import `in`.jyotirmoy.attendx.settings.domain.usecase.CheckUpdateUseCase

class UpdateCheckWorker(
    @param:ApplicationContext private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val checkUpdateUseCase: CheckUpdateUseCase by lazy {
        EntryPointAccessors.fromApplication(
            applicationContext,
            WorkerEntryPoint::class.java
        ).checkUpdateUseCase()
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun doWork(): Result {

        return try {
            val result = checkUpdateUseCase(BuildConfig.VERSION_NAME, false)

            if (result is UpdateResult.Success && result.isUpdateAvailable) {
                NotificationSetup.showUpdateAvailableNotification(applicationContext)
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
