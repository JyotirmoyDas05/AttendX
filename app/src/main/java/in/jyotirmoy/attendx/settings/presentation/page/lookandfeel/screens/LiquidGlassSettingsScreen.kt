@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package `in`.jyotirmoy.attendx.settings.presentation.page.lookandfeel.screens

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Contrast
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.rounded.BlurOn
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.Contrast
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.FormatColorText
import androidx.compose.material.icons.rounded.Grain
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.HourglassBottom
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Opacity
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material.icons.rounded.Tonality
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.ViewDay
import androidx.compose.material.icons.rounded.WaterfallChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.jyotirmoy.attendx.core.ui.glass.ColorPickerDialog
import `in`.jyotirmoy.attendx.core.ui.glass.GlassEffectConfig
import `in`.jyotirmoy.attendx.core.ui.glass.GlassStyle
import `in`.jyotirmoy.attendx.core.ui.glass.LocalAppBackdrop
import `in`.jyotirmoy.attendx.core.ui.glass.backdrop.backdrops.layerBackdrop
import `in`.jyotirmoy.attendx.core.ui.glass.backdrop.backdrops.rememberLayerBackdrop
import `in`.jyotirmoy.attendx.core.ui.glass.isGlassAllowed
import `in`.jyotirmoy.attendx.core.ui.glass.isGlassSupported
import `in`.jyotirmoy.attendx.core.ui.glass.liquidGlass
import `in`.jyotirmoy.attendx.core.ui.glass.shapes.ContinuousRoundedRectangle
import `in`.jyotirmoy.attendx.settings.data.local.SettingsKeys
import `in`.jyotirmoy.attendx.settings.presentation.components.scaffold.SettingsScaffold
import `in`.jyotirmoy.attendx.settings.presentation.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

@Composable
fun LiquidGlassSettingsScreen(
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
) {
    val scope = rememberCoroutineScope()
    val isSupported = isGlassAllowed()

    // Master switch
    val masterEnabled by settingsViewModel.isItemChecked(SettingsKeys.LIQUID_GLASS_EFFECT)
        .collectAsState(initial = isSupported)

    // Effects
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

    // Appearance
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

    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val currentStyle = try { GlassStyle.valueOf(glassStyleString) } catch (e: Exception) { GlassStyle.LIQUID }

    val resolvedSurfaceTint = if (surfaceTintColorInt != 0) Color(surfaceTintColorInt)
        else if (isDark) Color(0xFF2C2C2E) else Color(0xFFFFFFFF)

    val resolvedPuckColor = if (puckColorInt != 0) Color(puckColorInt)
        else if (isDark) Color(0xFF48484A) else Color(0xFFFFFFFF)

    val resolvedHighlightColor = if (highlightColorInt != 0) Color(highlightColorInt) else Color.White

    val resolvedTextColor = if (!adaptiveContrast && textColorInt != 0) Color(textColorInt)
        else if (isDark) Color.White else MaterialTheme.colorScheme.onSurface

    // Live preview config — constructed directly from screen values
    val previewConfig = remember(
        masterEnabled, vibrancy, blurRadius, lensHeight, lensAmount,
        chromaticAberration, depthEffect, surfaceTintColorInt, surfaceOpacity,
        currentStyle, puckColorInt, puckOpacity, highlightColorInt, highlightOpacity,
        adaptiveContrast, textColorInt, isDark
    ) {
        GlassEffectConfig(
            globalEnabled = masterEnabled && isSupported,
            vibrancy = vibrancy,
            blurRadius = blurRadius,
            lensHeight = lensHeight,
            lensAmount = lensAmount,
            chromaticAberration = chromaticAberration,
            depthEffect = depthEffect,
            surfaceTintColor = if (surfaceTintColorInt == 0) Color.Unspecified else Color(surfaceTintColorInt),
            surfaceOpacity = surfaceOpacity,
            style = currentStyle,
            puckColor = if (puckColorInt == 0) Color.Unspecified else Color(puckColorInt),
            puckOpacity = puckOpacity,
            highlightColor = if (highlightColorInt == 0) Color.Unspecified else Color(highlightColorInt),
            highlightOpacity = highlightOpacity,
            textColor = if (!adaptiveContrast && textColorInt != 0) Color(textColorInt) else Color.Unspecified,
        )
    }

    // Dialog state holders
    var showVibrancyDialog by rememberSaveable { mutableStateOf(false) }
    var showBlurRadiusDialog by rememberSaveable { mutableStateOf(false) }
    var showLensHeightDialog by rememberSaveable { mutableStateOf(false) }
    var showLensAmountDialog by rememberSaveable { mutableStateOf(false) }
    var showSurfaceTintDialog by rememberSaveable { mutableStateOf(false) }
    var showSurfaceOpacityDialog by rememberSaveable { mutableStateOf(false) }
    var showGlassStyleDialog by rememberSaveable { mutableStateOf(false) }
    var showPuckColorDialog by rememberSaveable { mutableStateOf(false) }
    var showPuckOpacityDialog by rememberSaveable { mutableStateOf(false) }
    var showHighlightColorDialog by rememberSaveable { mutableStateOf(false) }
    var showHighlightOpacityDialog by rememberSaveable { mutableStateOf(false) }
    var showTextColorDialog by rememberSaveable { mutableStateOf(false) }

    val listState = rememberLazyListState()

    SettingsScaffold(
        modifier = modifier,
        listState = listState,
        topBarTitle = "Liquid Glass",
        content = { innerPadding, topBarScrollBehavior ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(topBarScrollBehavior.nestedScrollConnection)
                    .padding(horizontal = 16.dp),
                state = listState,
                contentPadding = innerPadding,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Live interactive glass preview card
                item {
                    Spacer(Modifier.height(8.dp))
                    GlassLivePreview(config = previewConfig)
                }

                // Master switch card
                item {
                    val subtitle = when {
                        !isSupported -> "Requires Android 12+ (API 31). Not available on this device."
                        else -> "GPU-accelerated backdrop blur, chromatic refraction & specular highlights"
                    }

                    GlassSettingCard(
                        shape = RoundedCornerShape(24.dp),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = isSupported) {
                                    scope.launch {
                                        settingsViewModel.setBoolean(
                                            SettingsKeys.LIQUID_GLASS_EFFECT,
                                            !masterEnabled
                                        )
                                    }
                                }
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Tune,
                                contentDescription = null,
                                tint = if (isSupported) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Liquid Glass Effect",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSupported) MaterialTheme.colorScheme.onSurface
                                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                )
                                Text(
                                    text = subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Switch(
                                checked = masterEnabled && isSupported,
                                enabled = isSupported,
                                onCheckedChange = null,
                            )
                        }
                    }
                }

                // Dials enabled when master switch is ON and supported
                val dialsEnabled = masterEnabled && isSupported
                val sectionAlpha = if (dialsEnabled) 1f else 0.4f

                // Effects Group
                item {
                    Text(
                        text = "EFFECTS",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = sectionAlpha),
                        modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                    )
                }

                item {
                    GlassSettingCard(
                        modifier = Modifier.alpha(sectionAlpha),
                        shape = RoundedCornerShape(24.dp),
                    ) {
                        Column {
                            GlassSettingItem(
                                icon = Icons.Rounded.WaterfallChart,
                                title = "Vibrancy",
                                subtitle = "Color saturation enhancement (%.2fx)".format(vibrancy),
                                enabled = dialsEnabled,
                                onClick = { showVibrancyDialog = true }
                            )
                            GlassDivider()
                            GlassSettingItem(
                                icon = Icons.Rounded.BlurOn,
                                title = "Blur Radius",
                                subtitle = "Frosted glass diffusion depth (%.0f dp)".format(blurRadius),
                                enabled = dialsEnabled,
                                onClick = { showBlurRadiusDialog = true }
                            )
                            GlassDivider()
                            GlassSettingItem(
                                icon = Icons.Rounded.Layers,
                                title = "Lens Refraction Height",
                                subtitle = "Convex lens edge thickness (%.2f)".format(lensHeight),
                                enabled = dialsEnabled,
                                onClick = { showLensHeightDialog = true }
                            )
                            GlassDivider()
                            GlassSettingItem(
                                icon = Icons.Rounded.Grain,
                                title = "Lens Refraction Amount",
                                subtitle = "Chromatic glass bending magnitude (%.2f)".format(lensAmount),
                                enabled = dialsEnabled,
                                onClick = { showLensAmountDialog = true }
                            )
                            GlassDivider()
                            GlassSettingToggle(
                                icon = Icons.Rounded.ColorLens,
                                title = "Chromatic Aberration",
                                subtitle = "Color dispersion spectrum on touch drag",
                                checked = chromaticAberration,
                                enabled = dialsEnabled,
                                onCheckedChange = {
                                    scope.launch {
                                        settingsViewModel.setBoolean(
                                            SettingsKeys.LIQUID_GLASS_CHROMATIC_ABERRATION,
                                            it
                                        )
                                    }
                                }
                            )
                            GlassDivider()
                            GlassSettingToggle(
                                icon = Icons.Rounded.Tonality,
                                title = "Depth Effect",
                                subtitle = "Refraction z-depth layering",
                                checked = depthEffect,
                                enabled = dialsEnabled,
                                onCheckedChange = {
                                    scope.launch {
                                        settingsViewModel.setBoolean(
                                            SettingsKeys.LIQUID_GLASS_DEPTH_EFFECT,
                                            it
                                        )
                                    }
                                }
                            )
                        }
                    }
                }

                // Appearance Group
                item {
                    Text(
                        text = "APPEARANCE",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = sectionAlpha),
                        modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                    )
                }

                item {
                    GlassSettingCard(
                        modifier = Modifier.alpha(sectionAlpha),
                        shape = RoundedCornerShape(24.dp),
                    ) {
                        Column {
                            GlassSettingItem(
                                icon = Icons.Rounded.Palette,
                                title = "Surface Tint",
                                subtitle = if (surfaceTintColorInt == 0) "Theme Adaptive (Auto)" else "Custom Color",
                                trailing = { ColorSwatch(resolvedSurfaceTint) },
                                enabled = dialsEnabled,
                                onClick = { showSurfaceTintDialog = true }
                            )
                            GlassDivider()
                            GlassSettingItem(
                                icon = Icons.Rounded.Opacity,
                                title = "Surface Opacity",
                                subtitle = "Glass base opacity (%.2f)".format(surfaceOpacity),
                                enabled = dialsEnabled,
                                onClick = { showSurfaceOpacityDialog = true }
                            )
                            GlassDivider()
                            GlassSettingItem(
                                icon = Icons.Rounded.Style,
                                title = "Glass Style",
                                subtitle = when (currentStyle) {
                                    GlassStyle.LIQUID -> "Liquid Glass (Refraction + Highlights)"
                                    GlassStyle.BLUR -> "Frosted Blur Only"
                                    GlassStyle.TRANSPARENT -> "Translucent Fallback"
                                },
                                enabled = dialsEnabled,
                                onClick = { showGlassStyleDialog = true }
                            )
                            GlassDivider()
                            GlassSettingItem(
                                icon = Icons.Rounded.ViewDay,
                                title = "Active Tab Puck Color",
                                subtitle = if (puckColorInt == 0) "Theme Adaptive (Auto)" else "Custom Color",
                                trailing = { ColorSwatch(resolvedPuckColor) },
                                enabled = dialsEnabled,
                                onClick = { showPuckColorDialog = true }
                            )
                            GlassDivider()
                            GlassSettingItem(
                                icon = Icons.Rounded.Opacity,
                                title = "Puck Opacity",
                                subtitle = "Resting opacity of active tab pill (%.2f)".format(puckOpacity),
                                enabled = dialsEnabled,
                                onClick = { showPuckOpacityDialog = true }
                            )
                            GlassDivider()
                            GlassSettingItem(
                                icon = Icons.Rounded.Palette,
                                title = "Specular Highlight Color",
                                subtitle = if (highlightColorInt == 0) "White Specular Rim" else "Custom Color",
                                trailing = { ColorSwatch(resolvedHighlightColor) },
                                enabled = dialsEnabled,
                                onClick = { showHighlightColorDialog = true }
                            )
                            GlassDivider()
                            GlassSettingItem(
                                icon = Icons.Rounded.Opacity,
                                title = "Highlight Opacity",
                                subtitle = "Edge rim reflection intensity (%.2f)".format(highlightOpacity),
                                enabled = dialsEnabled,
                                onClick = { showHighlightOpacityDialog = true }
                            )
                            GlassDivider()
                            GlassSettingToggle(
                                icon = Icons.Rounded.Contrast,
                                title = "Adaptive Contrast",
                                subtitle = "Automatically set icon and label contrast",
                                checked = adaptiveContrast,
                                enabled = dialsEnabled,
                                onCheckedChange = {
                                    scope.launch {
                                        settingsViewModel.setBoolean(
                                            SettingsKeys.LIQUID_GLASS_ADAPTIVE_CONTRAST,
                                            it
                                        )
                                    }
                                }
                            )
                            if (!adaptiveContrast) {
                                GlassDivider()
                                GlassSettingItem(
                                    icon = Icons.Rounded.FormatColorText,
                                    title = "Text & Icon Color",
                                    subtitle = "Custom foreground color for tabs",
                                    trailing = { ColorSwatch(resolvedTextColor) },
                                    enabled = dialsEnabled,
                                    onClick = { showTextColorDialog = true }
                                )
                            }
                        }
                    }
                }

                // Reset Group
                item {
                    GlassSettingCard(
                        modifier = Modifier.alpha(sectionAlpha),
                        shape = RoundedCornerShape(24.dp),
                    ) {
                        GlassSettingItem(
                            icon = Icons.Rounded.Restore,
                            title = "Reset to Defaults",
                            subtitle = "Restore all Liquid Glass parameters to factory defaults",
                            enabled = dialsEnabled,
                            onClick = {
                                scope.launch {
                                    settingsViewModel.setFloat(SettingsKeys.LIQUID_GLASS_VIBRANCY, SettingsKeys.LIQUID_GLASS_VIBRANCY.defaultValue as Float)
                                    settingsViewModel.setFloat(SettingsKeys.LIQUID_GLASS_BLUR_RADIUS, SettingsKeys.LIQUID_GLASS_BLUR_RADIUS.defaultValue as Float)
                                    settingsViewModel.setFloat(SettingsKeys.LIQUID_GLASS_LENS_HEIGHT, SettingsKeys.LIQUID_GLASS_LENS_HEIGHT.defaultValue as Float)
                                    settingsViewModel.setFloat(SettingsKeys.LIQUID_GLASS_LENS_AMOUNT, SettingsKeys.LIQUID_GLASS_LENS_AMOUNT.defaultValue as Float)
                                    settingsViewModel.setBoolean(SettingsKeys.LIQUID_GLASS_CHROMATIC_ABERRATION, SettingsKeys.LIQUID_GLASS_CHROMATIC_ABERRATION.defaultValue as Boolean)
                                    settingsViewModel.setBoolean(SettingsKeys.LIQUID_GLASS_DEPTH_EFFECT, SettingsKeys.LIQUID_GLASS_DEPTH_EFFECT.defaultValue as Boolean)
                                    settingsViewModel.setInt(SettingsKeys.LIQUID_GLASS_SURFACE_TINT_COLOR, SettingsKeys.LIQUID_GLASS_SURFACE_TINT_COLOR.defaultValue as Int)
                                    settingsViewModel.setFloat(SettingsKeys.LIQUID_GLASS_SURFACE_OPACITY, SettingsKeys.LIQUID_GLASS_SURFACE_OPACITY.defaultValue as Float)
                                    settingsViewModel.setString(SettingsKeys.LIQUID_GLASS_STYLE, SettingsKeys.LIQUID_GLASS_STYLE.defaultValue as String)
                                    settingsViewModel.setInt(SettingsKeys.LIQUID_GLASS_PUCK_COLOR, SettingsKeys.LIQUID_GLASS_PUCK_COLOR.defaultValue as Int)
                                    settingsViewModel.setFloat(SettingsKeys.LIQUID_GLASS_PUCK_OPACITY, SettingsKeys.LIQUID_GLASS_PUCK_OPACITY.defaultValue as Float)
                                    settingsViewModel.setInt(SettingsKeys.LIQUID_GLASS_HIGHLIGHT_COLOR, SettingsKeys.LIQUID_GLASS_HIGHLIGHT_COLOR.defaultValue as Int)
                                    settingsViewModel.setFloat(SettingsKeys.LIQUID_GLASS_HIGHLIGHT_OPACITY, SettingsKeys.LIQUID_GLASS_HIGHLIGHT_OPACITY.defaultValue as Float)
                                    settingsViewModel.setBoolean(SettingsKeys.LIQUID_GLASS_ADAPTIVE_CONTRAST, SettingsKeys.LIQUID_GLASS_ADAPTIVE_CONTRAST.defaultValue as Boolean)
                                    settingsViewModel.setInt(SettingsKeys.LIQUID_GLASS_TEXT_COLOR, SettingsKeys.LIQUID_GLASS_TEXT_COLOR.defaultValue as Int)
                                }
                            }
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    )

    // Slider Dialogs
    if (showVibrancyDialog) {
        GlassSliderDialog(
            title = "Vibrancy",
            currentValue = vibrancy,
            defaultValue = SettingsKeys.LIQUID_GLASS_VIBRANCY.defaultValue as Float,
            valueRange = 0f..2f,
            valueFormatter = { "%.2fx".format(it) },
            onDismiss = { showVibrancyDialog = false },
            onConfirm = {
                scope.launch { settingsViewModel.setFloat(SettingsKeys.LIQUID_GLASS_VIBRANCY, it) }
                showVibrancyDialog = false
            }
        )
    }

    if (showBlurRadiusDialog) {
        GlassSliderDialog(
            title = "Blur Radius",
            currentValue = blurRadius,
            defaultValue = SettingsKeys.LIQUID_GLASS_BLUR_RADIUS.defaultValue as Float,
            valueRange = 0f..32f,
            valueFormatter = { "%.0f dp".format(it) },
            onDismiss = { showBlurRadiusDialog = false },
            onConfirm = {
                scope.launch { settingsViewModel.setFloat(SettingsKeys.LIQUID_GLASS_BLUR_RADIUS, it) }
                showBlurRadiusDialog = false
            }
        )
    }

    if (showLensHeightDialog) {
        GlassSliderDialog(
            title = "Lens Height",
            currentValue = lensHeight,
            defaultValue = SettingsKeys.LIQUID_GLASS_LENS_HEIGHT.defaultValue as Float,
            valueRange = 0f..1f,
            valueFormatter = { "%.2f".format(it) },
            onDismiss = { showLensHeightDialog = false },
            onConfirm = {
                scope.launch { settingsViewModel.setFloat(SettingsKeys.LIQUID_GLASS_LENS_HEIGHT, it) }
                showLensHeightDialog = false
            }
        )
    }

    if (showLensAmountDialog) {
        GlassSliderDialog(
            title = "Lens Amount",
            currentValue = lensAmount,
            defaultValue = SettingsKeys.LIQUID_GLASS_LENS_AMOUNT.defaultValue as Float,
            valueRange = 0f..1f,
            valueFormatter = { "%.2f".format(it) },
            onDismiss = { showLensAmountDialog = false },
            onConfirm = {
                scope.launch { settingsViewModel.setFloat(SettingsKeys.LIQUID_GLASS_LENS_AMOUNT, it) }
                showLensAmountDialog = false
            }
        )
    }

    if (showSurfaceOpacityDialog) {
        GlassSliderDialog(
            title = "Surface Opacity",
            currentValue = surfaceOpacity,
            defaultValue = SettingsKeys.LIQUID_GLASS_SURFACE_OPACITY.defaultValue as Float,
            valueRange = 0f..1f,
            valueFormatter = { "%.2f".format(it) },
            onDismiss = { showSurfaceOpacityDialog = false },
            onConfirm = {
                scope.launch { settingsViewModel.setFloat(SettingsKeys.LIQUID_GLASS_SURFACE_OPACITY, it) }
                showSurfaceOpacityDialog = false
            }
        )
    }

    if (showPuckOpacityDialog) {
        GlassSliderDialog(
            title = "Puck Opacity",
            currentValue = puckOpacity,
            defaultValue = SettingsKeys.LIQUID_GLASS_PUCK_OPACITY.defaultValue as Float,
            valueRange = 0f..1f,
            valueFormatter = { "%.2f".format(it) },
            onDismiss = { showPuckOpacityDialog = false },
            onConfirm = {
                scope.launch { settingsViewModel.setFloat(SettingsKeys.LIQUID_GLASS_PUCK_OPACITY, it) }
                showPuckOpacityDialog = false
            }
        )
    }

    if (showHighlightOpacityDialog) {
        GlassSliderDialog(
            title = "Highlight Opacity",
            currentValue = highlightOpacity,
            defaultValue = SettingsKeys.LIQUID_GLASS_HIGHLIGHT_OPACITY.defaultValue as Float,
            valueRange = 0f..1f,
            valueFormatter = { "%.2f".format(it) },
            onDismiss = { showHighlightOpacityDialog = false },
            onConfirm = {
                scope.launch { settingsViewModel.setFloat(SettingsKeys.LIQUID_GLASS_HIGHLIGHT_OPACITY, it) }
                showHighlightOpacityDialog = false
            }
        )
    }

    // Glass Style Dialog
    if (showGlassStyleDialog) {
        AlertDialog(
            onDismissRequest = { showGlassStyleDialog = false },
            title = { Text("Glass Style") },
            confirmButton = {
                TextButton(onClick = { showGlassStyleDialog = false }) { Text("Cancel") }
            },
            text = {
                Column {
                    GlassStyle.entries.forEach { style ->
                        val (title, desc) = when (style) {
                            GlassStyle.LIQUID -> "Liquid Glass" to "Real-time backdrop refraction, blur and specular rims"
                            GlassStyle.BLUR -> "Frosted Blur" to "Frosted backdrop blur without lens refraction"
                            GlassStyle.TRANSPARENT -> "Translucent" to "Lightweight translucent tint fallback"
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    scope.launch {
                                        settingsViewModel.setString(
                                            SettingsKeys.LIQUID_GLASS_STYLE,
                                            style.name
                                        )
                                    }
                                    showGlassStyleDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 4.dp)
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (style == currentStyle) FontWeight.Bold else FontWeight.Normal,
                                color = if (style == currentStyle) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        )
    }

    // Color Picker Dialogs
    if (showSurfaceTintDialog) {
        ColorPickerDialog(
            initialColor = resolvedSurfaceTint,
            title = "Surface Tint Color",
            onDismiss = { showSurfaceTintDialog = false },
            onConfirm = { color ->
                scope.launch { settingsViewModel.setInt(SettingsKeys.LIQUID_GLASS_SURFACE_TINT_COLOR, color.toArgb()) }
                showSurfaceTintDialog = false
            },
            onReset = {
                scope.launch { settingsViewModel.setInt(SettingsKeys.LIQUID_GLASS_SURFACE_TINT_COLOR, 0) }
                showSurfaceTintDialog = false
            }
        )
    }

    if (showPuckColorDialog) {
        ColorPickerDialog(
            initialColor = resolvedPuckColor,
            title = "Active Tab Puck Color",
            onDismiss = { showPuckColorDialog = false },
            onConfirm = { color ->
                scope.launch { settingsViewModel.setInt(SettingsKeys.LIQUID_GLASS_PUCK_COLOR, color.toArgb()) }
                showPuckColorDialog = false
            },
            onReset = {
                scope.launch { settingsViewModel.setInt(SettingsKeys.LIQUID_GLASS_PUCK_COLOR, 0) }
                showPuckColorDialog = false
            }
        )
    }

    if (showHighlightColorDialog) {
        ColorPickerDialog(
            initialColor = resolvedHighlightColor,
            title = "Specular Highlight Color",
            onDismiss = { showHighlightColorDialog = false },
            onConfirm = { color ->
                scope.launch { settingsViewModel.setInt(SettingsKeys.LIQUID_GLASS_HIGHLIGHT_COLOR, color.toArgb()) }
                showHighlightColorDialog = false
            },
            onReset = {
                scope.launch { settingsViewModel.setInt(SettingsKeys.LIQUID_GLASS_HIGHLIGHT_COLOR, 0) }
                showHighlightColorDialog = false
            }
        )
    }

    if (showTextColorDialog) {
        ColorPickerDialog(
            initialColor = resolvedTextColor,
            title = "Text & Icon Color",
            onDismiss = { showTextColorDialog = false },
            onConfirm = { color ->
                scope.launch { settingsViewModel.setInt(SettingsKeys.LIQUID_GLASS_TEXT_COLOR, color.toArgb()) }
                showTextColorDialog = false
            },
            onReset = {
                scope.launch { settingsViewModel.setInt(SettingsKeys.LIQUID_GLASS_TEXT_COLOR, 0) }
                showTextColorDialog = false
            }
        )
    }
}

@Composable
private fun GlassLivePreview(
    config: GlassEffectConfig,
    modifier: Modifier = Modifier,
) {
    val previewBackdrop = rememberLayerBackdrop()
    val pillShape = ContinuousRoundedRectangle(percent = 50)
    var selectedPreviewTab by remember { mutableStateOf(0) }
    val previewScroll = rememberScrollState()

    val tabs = listOf(
        Triple("Home", Icons.Rounded.Home, Icons.Outlined.Home),
        Triple("Tools", Icons.Rounded.HourglassBottom, Icons.Outlined.HourglassEmpty),
        Triple("Timetable", Icons.Rounded.DateRange, Icons.Outlined.DateRange),
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(24.dp)),
        contentAlignment = Alignment.BottomCenter,
    ) {
        // Recorded colorful background panel
        BoxWithConstraints(
            modifier = Modifier
                .matchParentSize()
                .layerBackdrop(previewBackdrop)
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFFFF3366),
                            Color(0xFF7928CA),
                            Color(0xFF0070F3),
                            Color(0xFF00DFD8),
                            Color(0xFF7928CA),
                        )
                    )
                ),
        ) {
            val panelWidth = maxWidth
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(previewScroll),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Decorative colorful shapes underneath the glass to showcase refraction
                Row(
                    modifier = Modifier
                        .width(panelWidth * 1.6f)
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier
                            .size(70.dp)
                            .background(Color.White.copy(alpha = 0.25f), CircleShape)
                    )
                    Box(
                        Modifier
                            .size(90.dp)
                            .background(Color(0xFFFFF066).copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                    )
                    Box(
                        Modifier
                            .size(60.dp)
                            .background(Color(0xFFFF0080).copy(alpha = 0.35f), CircleShape)
                    )
                    Box(
                        Modifier
                            .size(80.dp)
                            .background(Color.White.copy(alpha = 0.3f), CircleShape)
                    )
                }
            }
        }

        // Live Liquid Glass Nav Bar sampling the recorded preview backdrop
        CompositionLocalProvider(LocalAppBackdrop provides previewBackdrop) {
            val previewGlassModifier = if (isGlassAllowed() && config.globalEnabled) {
                Modifier.liquidGlass(
                    config = config,
                    shape = pillShape,
                    highlightAlpha = config.highlightOpacity
                )
            } else {
                Modifier.background(
                    MaterialTheme.colorScheme.surfaceContainerHigh,
                    pillShape
                )
            }

            Box(
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .fillMaxWidth(0.92f)
                    .then(previewGlassModifier)
                    .padding(vertical = 4.dp, horizontal = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val isGlassActive = isGlassAllowed() && config.globalEnabled
                    tabs.forEachIndexed { index, (title, selectedIcon, unselectedIcon) ->
                        val isSelected = index == selectedPreviewTab
                        val iconColor = if (isSelected) {
                            when {
                                config.textColor.isSpecified -> config.textColor
                                isGlassActive -> {
                                    if (MaterialTheme.colorScheme.surface.luminance() < 0.5f) Color.White
                                    else MaterialTheme.colorScheme.onSurface
                                }
                                else -> MaterialTheme.colorScheme.onSecondaryContainer
                            }
                        } else {
                            when {
                                config.textColor.isSpecified -> config.textColor.copy(alpha = 0.55f)
                                isGlassActive -> {
                                    if (MaterialTheme.colorScheme.surface.luminance() < 0.5f) Color.White.copy(alpha = 0.55f)
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                                }
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        }

                        val puckModifier = if (isSelected) {
                            if (isGlassActive) {
                                Modifier
                                    .background(
                                        (if (config.puckColor.isSpecified) config.puckColor
                                         else if (MaterialTheme.colorScheme.surface.luminance() < 0.5f) Color(0xFF48484A)
                                         else Color.White)
                                            .copy(alpha = config.puckOpacity),
                                        ContinuousRoundedRectangle(percent = 50)
                                    )
                                    .border(
                                        width = 0.8.dp,
                                        color = Color.White.copy(alpha = config.highlightOpacity),
                                        shape = ContinuousRoundedRectangle(percent = 50)
                                    )
                            } else {
                                Modifier.background(
                                    MaterialTheme.colorScheme.secondaryContainer,
                                    ContinuousRoundedRectangle(percent = 50)
                                )
                            }
                        } else Modifier

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .then(puckModifier)
                                .clickable { selectedPreviewTab = index }
                                .padding(vertical = 6.dp, horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                imageVector = if (isSelected) selectedIcon else unselectedIcon,
                                contentDescription = null,
                                tint = iconColor,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = title,
                                color = iconColor,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GlassSliderDialog(
    title: String,
    currentValue: Float,
    defaultValue: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    valueFormatter: (Float) -> String,
    onDismiss: () -> Unit,
    onConfirm: (Float) -> Unit,
) {
    var tempValue by remember { mutableFloatStateOf(currentValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        confirmButton = {
            TextButton(onClick = { onConfirm(tempValue) }) { Text("OK") }
        },
        dismissButton = {
            Row {
                TextButton(onClick = { tempValue = defaultValue }) { Text("Reset") }
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = valueFormatter(tempValue),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
                Slider(
                    value = tempValue,
                    onValueChange = { tempValue = it },
                    valueRange = valueRange,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}

@Composable
private fun ColorSwatch(color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(26.dp)
            .clip(CircleShape)
            .background(color)
            .border(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), CircleShape)
    )
}

@Composable
private fun GlassSettingCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(20.dp),
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        content()
    }
}

@Composable
private fun GlassSettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    enabled: Boolean = true,
    trailing: (@Composable () -> Unit)? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (trailing != null) {
            Spacer(Modifier.width(8.dp))
            trailing()
        }
    }
}

@Composable
private fun GlassSettingToggle(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.width(8.dp))
        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = null,
        )
    }
}

@Composable
private fun GlassDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 60.dp)
            .height(0.8.dp)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    )
}
