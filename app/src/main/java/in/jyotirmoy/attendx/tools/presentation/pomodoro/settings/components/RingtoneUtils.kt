package `in`.jyotirmoy.attendx.tools.presentation.pomodoro.settings.components

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import `in`.jyotirmoy.attendx.R
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.model.SettingsAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun rememberRingtonePickerLauncherCallback(
    alarmSoundFilePath: String?,
    onResult: (SettingsAction) -> Unit
): () -> Unit {
    val alarmSoundString = stringResource(R.string.alarm_sound)

    val ringtonePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    result.data?.getParcelableExtra(
                        RingtoneManager.EXTRA_RINGTONE_PICKED_URI,
                        Uri::class.java
                    )
                } else {
                    @Suppress("DEPRECATION")
                    result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
                }
            onResult(SettingsAction.SaveAlarmSound(uri?.toString() ?: ""))
        }
    }

    val ringtonePickerIntent = remember(alarmSoundFilePath) {
        Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
            putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, alarmSoundString)
            putExtra(
                RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                if (alarmSoundFilePath.isNullOrEmpty()) null else alarmSoundFilePath.toUri()
            )
        }
    }

    return { ringtonePickerLauncher.launch(ringtonePickerIntent) }
}

@Composable
fun rememberRingtoneNameProviderCallback(): suspend (String?) -> String {
    val context = LocalContext.current

    return remember {
        { alarmSoundFilePath ->
            withContext(Dispatchers.IO) {
                if (alarmSoundFilePath.isNullOrEmpty()) {
                    "System default"
                } else {
                    try {
                        RingtoneManager.getRingtone(context, alarmSoundFilePath.toUri())
                            ?.getTitle(context) ?: "System default"
                    } catch (e: Exception) {
                        Log.e("AlarmSettings", "Unable to get ringtone title: ${e.message}")
                        "System default"
                    }
                }
            }
        }
    }
}
