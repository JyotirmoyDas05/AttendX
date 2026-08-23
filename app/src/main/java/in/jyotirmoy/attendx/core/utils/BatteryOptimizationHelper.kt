package `in`.jyotirmoy.attendx.core.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log

object BatteryOptimizationHelper {
    private const val TAG = "BatteryOptHelper"

    /**
     * Checks if the app is already exempt from battery optimization limitations.
     */
    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        return try {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return true
            powerManager.isIgnoringBatteryOptimizations(context.packageName)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking battery optimization status", e)
            false
        }
    }

    /**
     * Prompts the user with the system dialog to whitelist the app from background restrictions.
     */
    @SuppressLint("BatteryLife")
    fun requestIgnoreBatteryOptimization(context: Context) {
        if (isIgnoringBatteryOptimizations(context)) {
            Log.d(TAG, "App is already whitelisted from battery optimizations.")
            return
        }

        try {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            Log.d(TAG, "Triggered ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS dialog.")
        } catch (e: Exception) {
            Log.w(TAG, "Direct request failed, opening general battery saver settings", e)
            try {
                val fallbackIntent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallbackIntent)
            } catch (ex: Exception) {
                Log.e(TAG, "Failed to launch battery settings", ex)
            }
        }
    }
}
