package `in`.jyotirmoy.attendx

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import `in`.jyotirmoy.attendx.core.common.CompositionLocals
import `in`.jyotirmoy.attendx.core.common.LocalSeedColor
import `in`.jyotirmoy.attendx.core.domain.provider.SeedColorProvider
import `in`.jyotirmoy.attendx.core.presentation.AppEntry
import `in`.jyotirmoy.attendx.core.presentation.theme.AttendXTheme
import `in`.jyotirmoy.attendx.core.ui.glass.GlassEffectConfig
import `in`.jyotirmoy.attendx.core.ui.glass.GlassStyle
import `in`.jyotirmoy.attendx.core.ui.glass.LocalAppBackdrop
import `in`.jyotirmoy.attendx.core.ui.glass.LocalGlassEffectConfig
import `in`.jyotirmoy.attendx.core.ui.glass.isGlassSupported
import `in`.jyotirmoy.attendx.core.ui.glass.backdrop.backdrops.rememberLayerBackdrop
import `in`.jyotirmoy.attendx.settings.data.local.SettingsKeys
import `in`.jyotirmoy.attendx.settings.presentation.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

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
            val liquidGlassEnabled by settingsViewModel.isItemChecked(SettingsKeys.LIQUID_GLASS_EFFECT)
                .collectAsState(initial = isGlassSupported())
            val vibrancy by settingsViewModel.getFloat(SettingsKeys.LIQUID_GLASS_VIBRANCY)
                .collectAsState(initial = SettingsKeys.LIQUID_GLASS_VIBRANCY.defaultValue as Float)
            val blurRadius by settingsViewModel.getFloat(SettingsKeys.LIQUID_GLASS_BLUR_RADIUS)
                .collectAsState(initial = SettingsKeys.LIQUID_GLASS_BLUR_RADIUS.defaultValue as Float)
            val lensHeight by settingsViewModel.getFloat(SettingsKeys.LIQUID_GLASS_LENS_HEIGHT)
                .collectAsState(initial = SettingsKeys.LIQUID_GLASS_LENS_HEIGHT.defaultValue as Float)
            val lensAmount by settingsViewModel.getFloat(SettingsKeys.LIQUID_GLASS_LENS_AMOUNT)
                .collectAsState(initial = SettingsKeys.LIQUID_GLASS_LENS_AMOUNT.defaultValue as Float)
            val chromaticAberration by settingsViewModel.getBoolean(SettingsKeys.LIQUID_GLASS_CHROMATIC_ABERRATION)
                .collectAsState(initial = SettingsKeys.LIQUID_GLASS_CHROMATIC_ABERRATION.defaultValue as Boolean)
            val depthEffect by settingsViewModel.getBoolean(SettingsKeys.LIQUID_GLASS_DEPTH_EFFECT)
                .collectAsState(initial = SettingsKeys.LIQUID_GLASS_DEPTH_EFFECT.defaultValue as Boolean)
            val surfaceTintColorInt by settingsViewModel.getInt(SettingsKeys.LIQUID_GLASS_SURFACE_TINT_COLOR)
                .collectAsState(initial = SettingsKeys.LIQUID_GLASS_SURFACE_TINT_COLOR.defaultValue as Int)
            val surfaceOpacity by settingsViewModel.getFloat(SettingsKeys.LIQUID_GLASS_SURFACE_OPACITY)
                .collectAsState(initial = SettingsKeys.LIQUID_GLASS_SURFACE_OPACITY.defaultValue as Float)
            val glassStyleString by settingsViewModel.getString(SettingsKeys.LIQUID_GLASS_STYLE)
                .collectAsState(initial = SettingsKeys.LIQUID_GLASS_STYLE.defaultValue as String)
            val puckColorInt by settingsViewModel.getInt(SettingsKeys.LIQUID_GLASS_PUCK_COLOR)
                .collectAsState(initial = SettingsKeys.LIQUID_GLASS_PUCK_COLOR.defaultValue as Int)
            val puckOpacity by settingsViewModel.getFloat(SettingsKeys.LIQUID_GLASS_PUCK_OPACITY)
                .collectAsState(initial = SettingsKeys.LIQUID_GLASS_PUCK_OPACITY.defaultValue as Float)
            val highlightColorInt by settingsViewModel.getInt(SettingsKeys.LIQUID_GLASS_HIGHLIGHT_COLOR)
                .collectAsState(initial = SettingsKeys.LIQUID_GLASS_HIGHLIGHT_COLOR.defaultValue as Int)
            val highlightOpacity by settingsViewModel.getFloat(SettingsKeys.LIQUID_GLASS_HIGHLIGHT_OPACITY)
                .collectAsState(initial = SettingsKeys.LIQUID_GLASS_HIGHLIGHT_OPACITY.defaultValue as Float)
            val adaptiveContrast by settingsViewModel.getBoolean(SettingsKeys.LIQUID_GLASS_ADAPTIVE_CONTRAST)
                .collectAsState(initial = SettingsKeys.LIQUID_GLASS_ADAPTIVE_CONTRAST.defaultValue as Boolean)
            val textColorInt by settingsViewModel.getInt(SettingsKeys.LIQUID_GLASS_TEXT_COLOR)
                .collectAsState(initial = SettingsKeys.LIQUID_GLASS_TEXT_COLOR.defaultValue as Int)

            val glassConfig = remember(
                liquidGlassEnabled, vibrancy, blurRadius, lensHeight, lensAmount,
                chromaticAberration, depthEffect, surfaceTintColorInt, surfaceOpacity,
                glassStyleString, puckColorInt, puckOpacity, highlightColorInt,
                highlightOpacity, adaptiveContrast, textColorInt
            ) {
                GlassEffectConfig(
                    globalEnabled = liquidGlassEnabled,
                    vibrancy = vibrancy,
                    blurRadius = blurRadius,
                    lensHeight = lensHeight,
                    lensAmount = lensAmount,
                    chromaticAberration = chromaticAberration,
                    depthEffect = depthEffect,
                    surfaceTintColor = if (surfaceTintColorInt == 0) Color.Unspecified else Color(surfaceTintColorInt),
                    surfaceOpacity = surfaceOpacity,
                    style = try { GlassStyle.valueOf(glassStyleString) } catch (e: Exception) { GlassStyle.LIQUID },
                    puckColor = if (puckColorInt == 0) Color.Unspecified else Color(puckColorInt),
                    puckOpacity = puckOpacity,
                    highlightColor = if (highlightColorInt == 0) Color.Unspecified else Color(highlightColorInt),
                    highlightOpacity = highlightOpacity,
                    textColor = if (!adaptiveContrast && textColorInt != 0) Color(textColorInt) else Color.Unspecified,
                )
            }

            CompositionLocals {
                SeedColorProvider.setSeedColor(LocalSeedColor.current)

                AttendXTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        // The backdrop layer is painted with the opaque surface color
                        // BEFORE the content (Convx parity). Without this, every pixel
                        // the content doesn't draw — the whole 104dp zone behind the
                        // floating nav bar — is transparent in the recorded layer, and
                        // blur/refraction/dispersion of transparent pixels is still
                        // transparent: the bar renders as a bare 45% tint.
                        val backgroundColor = MaterialTheme.colorScheme.surface
                        val appBackdrop = rememberLayerBackdrop(
                            onDraw = remember(backgroundColor) {
                                val bg = backgroundColor
                                {
                                    drawRect(bg)
                                    drawContent()
                                }
                            }
                        )

                        CompositionLocalProvider(
                            LocalAppBackdrop provides appBackdrop,
                            LocalGlassEffectConfig provides glassConfig,
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
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        notificationIntent.value = intent
    }
}


