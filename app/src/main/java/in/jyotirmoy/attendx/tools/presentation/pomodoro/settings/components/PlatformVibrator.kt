package `in`.jyotirmoy.attendx.tools.presentation.pomodoro.settings.components

import android.Manifest
import android.content.Context
import android.content.Context.VIBRATOR_MANAGER_SERVICE
import android.content.Context.VIBRATOR_SERVICE
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

const val SYSTEM_DEFAULT_AMPLITUDE = -1

interface PlatformVibrator {
    val hasVibrator: Boolean
    val hasAmplitudeControl: Boolean

    fun playWaveform(onDuration: Long, offDuration: Long, amplitude: Int)
    fun cancel()
}

class AndroidPlatformVibrator(context: Context) : PlatformVibrator {
    private val vibrator =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                context.getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(VIBRATOR_SERVICE) as Vibrator
        }

    override val hasVibrator: Boolean = vibrator.hasVibrator()

    override val hasAmplitudeControl: Boolean = vibrator.hasAmplitudeControl()

    @RequiresPermission(Manifest.permission.VIBRATE)
    override fun playWaveform(onDuration: Long, offDuration: Long, amplitude: Int) {
        if (!hasVibrator) return

        val timings = longArrayOf(0, onDuration, offDuration, onDuration)
        val amplitudes = intArrayOf(0, amplitude, 0, amplitude)

        val effect = VibrationEffect.createWaveform(timings, amplitudes, 2)

        vibrator.vibrate(effect)
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    override fun cancel() {
        vibrator.cancel()
    }
}

@RequiresPermission(Manifest.permission.VIBRATE)
@Composable
fun rememberPlatformVibrator(): PlatformVibrator {
    val context = LocalContext.current
    val vibrator = remember { AndroidPlatformVibrator(context) }

    DisposableEffect(vibrator) {
        onDispose { vibrator.cancel() }
    }
    return vibrator
}
