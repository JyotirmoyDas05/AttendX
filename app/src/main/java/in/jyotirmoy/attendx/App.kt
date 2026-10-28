package `in`.jyotirmoy.attendx

import android.app.Application
import android.util.Log
import androidx.room.InvalidationTracker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import `in`.jyotirmoy.attendx.core.data.database.SubjectDatabase
import `in`.jyotirmoy.attendx.notification.scheduler.WorkScheduler
import `in`.jyotirmoy.attendx.widget.refreshAttendanceWidgets

@HiltAndroidApp
class App : Application() {

    companion object {
        private const val TAG = "AttendXApp"
    }

    private val widgetScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "AttendX Application started")

        // Initialize notification channels under AttendX Notifications group
        `in`.jyotirmoy.attendx.notification.NotificationSetup.initNotificationChannels(this)

        // Schedule timetable notification worker for background checks
        // This provides a fallback in case exact alarms don't fire
        WorkScheduler.scheduleTimetableNotifications(this)
        Log.d(TAG, "Timetable notification worker scheduled")

        // Any attendance, subject, or timetable write (from any screen, worker or receiver) refreshes the home screen widgets
        SubjectDatabase.getDatabase(this).invalidationTracker.addObserver(
            object : InvalidationTracker.Observer("attendance", "subjects", "class_schedule") {
                override fun onInvalidated(tables: Set<String>) {
                    widgetScope.launch { refreshAttendanceWidgets(this@App) }
                }
            }
        )
    }
}

