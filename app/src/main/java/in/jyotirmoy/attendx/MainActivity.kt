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

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            CompositionLocals {
                SeedColorProvider.setSeedColor(LocalSeedColor.current)

                AttendXTheme {
                    Surface(
                        modifier = Modifier.Companion.fillMaxSize(),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        AppEntry()
                    }
                }
            }
        }
    }
}


