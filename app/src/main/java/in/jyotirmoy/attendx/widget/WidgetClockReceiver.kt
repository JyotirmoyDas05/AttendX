package `in`.jyotirmoy.attendx.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Re-renders the widgets when the clock jumps rather than ticks: after a reboot (a widget can
 * otherwise show yesterday's date until something else updates it), or a manual time or
 * time-zone change. Ordinary midnights and class boundaries are covered by the clock refresh work.
 */
class WidgetClockReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                refreshAttendanceWidgets(context.applicationContext)
            } finally {
                pending.finish()
            }
        }
    }
}
