package `in`.jyotirmoy.attendx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import `in`.jyotirmoy.attendx.core.common.CompositionLocals
import `in`.jyotirmoy.attendx.core.common.LocalSeedColor
import `in`.jyotirmoy.attendx.core.domain.provider.SeedColorProvider
import `in`.jyotirmoy.attendx.core.presentation.AppEntry
import `in`.jyotirmoy.attendx.core.presentation.theme.AttendXTheme
import `in`.jyotirmoy.attendx.settings.presentation.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val notificationIntent = mutableStateOf<Intent?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Eagerly initialize SettingsViewModel to start background workers
        settingsViewModel.refreshNotificationPermissionState()

        notificationIntent.value = intent

        enableEdgeToEdge()
        setContent {
            CompositionLocals {
                SeedColorProvider.setSeedColor(LocalSeedColor.current)

                AttendXTheme {
                    Surface(
                        modifier = Modifier.Companion.fillMaxSize(),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        AppEntry(
                            notificationIntent = notificationIntent.value,
                            onNotificationIntentHandled = {
                                notificationIntent.value = null
                                intent?.removeExtra("EXTRA_NAVIGATE_TO")
                                intent?.removeExtra("EXTRA_SUBJECT_ID")
                                intent?.removeExtra("EXTRA_SUBJECT_NAME")
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        notificationIntent.value = intent
    }
}


