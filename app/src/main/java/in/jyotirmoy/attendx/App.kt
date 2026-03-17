package `in`.jyotirmoy.attendx

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import `in`.jyotirmoy.attendx.notification.scheduler.WorkScheduler

@HiltAndroidApp
class App : Application() {

    companion object {
        private const val TAG = "AttendXApp"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "AttendX Application started")
        
        // Schedule timetable notification worker for background checks
        // This provides a fallback in case exact alarms don't fire
        WorkScheduler.scheduleTimetableNotifications(this)
        Log.d(TAG, "Timetable notification worker scheduled")
    }
}

