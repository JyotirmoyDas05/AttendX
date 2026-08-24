package `in`.jyotirmoy.attendx.core.ui.glass

import android.app.ActivityManager
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import `in`.jyotirmoy.attendx.core.ui.glass.backdrop.Backdrop
import `in`.jyotirmoy.attendx.core.ui.glass.backdrop.BackdropEffectScope
import `in`.jyotirmoy.attendx.core.ui.glass.backdrop.drawBackdrop
import `in`.jyotirmoy.attendx.core.ui.glass.backdrop.effects.blur
import `in`.jyotirmoy.attendx.core.ui.glass.backdrop.effects.colorControls
import `in`.jyotirmoy.attendx.core.ui.glass.backdrop.effects.lens
import `in`.jyotirmoy.attendx.core.ui.glass.backdrop.highlight.Highlight
import `in`.jyotirmoy.attendx.core.ui.glass.backdrop.highlight.HighlightStyle
import `in`.jyotirmoy.attendx.core.ui.glass.backdrop.isRenderEffectSupported
import `in`.jyotirmoy.attendx.core.ui.glass.backdrop.shadow.Shadow

@Stable
data class GlassEffectConfig(
    val globalEnabled: Boolean = true,
    val vibrancy: Float = 1.2f,
    val blurRadius: Float = 2f,
    val lensHeight: Float = 0.40f,
    val lensAmount: Float = 0.60f,
    val chromaticAberration: Boolean = true,
    val depthEffect: Boolean = true,
    val surfaceTintColor: Color = Color.Unspecified,
    val highlightColor: Color = Color.Unspecified,
    val highlightOpacity: Float = EdgeHighlightAlpha,
    val style: GlassStyle = GlassStyle.LIQUID,
    val puckColor: Color = Color.Unspecified,
    val puckOpacity: Float = 0.70f,
    val surfaceOpacity: Float = 0.45f,
    val textColor: Color = Color.Unspecified,
    val navBarEnabled: Boolean = true,
) {
    fun isEnabledFor(component: GlassComponent): Boolean =
        globalEnabled && when (component) {
            GlassComponent.NAV_BAR -> navBarEnabled
        }

    val anyComponentEnabled: Boolean get() = globalEnabled && navBarEnabled
}

enum class GlassComponent { NAV_BAR }

enum class GlassStyle {
    LIQUID,
    BLUR,
    TRANSPARENT,
}

fun shouldUseTranslucentGlassFallback(style: GlassStyle, renderEffectSupported: Boolean): Boolean =
    style == GlassStyle.TRANSPARENT || !renderEffectSupported

internal const val LENS_MAX_DP = 48f
private const val EdgeHighlightAlpha = 0.55f
private val EdgeHighlightWidth = 0.8f.dp
private const val HighlightAngleFrozen = 45f

fun isGlassSupported(sdkInt: Int = Build.VERSION.SDK_INT): Boolean =
    sdkInt >= Build.VERSION_CODES.S

@Composable
fun isLowRamDevice(): Boolean {
    val context = LocalContext.current
    return remember { context.getSystemService(ActivityManager::class.java)?.isLowRamDevice ?: false }
}

@Composable
fun isGlassAllowed(): Boolean = isGlassSupported() && !isLowRamDevice()

fun glassSaturation(vibrancy: Float): Float = 1f + 0.5f * vibrancy.coerceIn(0f, 2f)

fun glassContentColorFor(behind: Color, tint: Color, opacity: Float): Color {
    val effective = if (tint.isSpecified) lerp(behind, tint, opacity.coerceIn(0f, 1f)) else behind
    return if (effective.luminance() > 0.5f) Color(0xFF1A1A1A) else Color.White
}

fun glassResolutionScale(blurRadiusDp: Float): Float {
    val t = (blurRadiusDp / FULL_QUALITY_BLUR_DP).coerceIn(0f, 1f)
    return 1f - t * (1f - MIN_GLASS_RESOLUTION_SCALE)
}

internal const val MIN_GLASS_RESOLUTION_SCALE = 0.30f
internal const val FULL_QUALITY_BLUR_DP = 8f

val LocalGlassEffectConfig = staticCompositionLocalOf { GlassEffectConfig() }
val LocalAppBackdrop = staticCompositionLocalOf<Backdrop> { error("No AppBackdrop provided") }
val LocalBackdropLoopBucket = staticCompositionLocalOf<(() -> Int)?> { null }

@Composable
fun Modifier.liquidGlass(
    config: GlassEffectConfig,
    shape: CornerBasedShape = RoundedCornerShape(0.dp),
    applyEdgeEffects: Boolean = true,
    blurRadiusDp: Float = config.blurRadius,
    highlightAlpha: Float = EdgeHighlightAlpha,
    backdropScale: Float = glassResolutionScale(blurRadiusDp),
    frozen: () -> Boolean = { false },
    loopBucket: (() -> Int)? = LocalBackdropLoopBucket.current,
): Modifier {
    if (!isGlassAllowed()) return this
    val backdrop = LocalAppBackdrop.current
    val density = LocalDensity.current
    val resolutionScale = backdropScale.coerceIn(0.05f, 1f)
    val blurPx = with(density) { blurRadiusDp.dp.toPx() } * resolutionScale
    val saturation = glassSaturation(config.vibrancy)
    val lensHeightPx = with(density) { (config.lensHeight * LENS_MAX_DP).dp.toPx() } * resolutionScale
    val lensAmountPx = with(density) { (config.lensAmount * LENS_MAX_DP).dp.toPx() } * resolutionScale

    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val surfaceTintColor = if (config.surfaceTintColor.isSpecified) {
        config.surfaceTintColor
    } else if (isDark) {
        Color(0xFF4A4A4E)
    } else {
        Color(0xFFFAFAFA)
    }

    if (shouldUseTranslucentGlassFallback(config.style, isRenderEffectSupported())) {
        return this
            .clip(shape)
            .background(surfaceTintColor.copy(alpha = config.surfaceOpacity.coerceIn(0f, 1f)))
    }

    val animatedHighlightAngle = HighlightAngleFrozen
    val shapeBlock: () -> Shape = remember(shape) { { shape } }
    val plainBlur = config.style == GlassStyle.BLUR

    val effectsBlock: BackdropEffectScope.() -> Unit = remember(
        saturation, blurPx, applyEdgeEffects, plainBlur, lensHeightPx, lensAmountPx,
        config.depthEffect, config.chromaticAberration,
    ) {
        {
            if (saturation != 1f) colorControls(saturation = saturation)
            if (blurPx > 0f) blur(blurPx)
            if (!plainBlur && applyEdgeEffects &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                (lensHeightPx > 0f || lensAmountPx > 0f)
            ) {
                lens(
                    refractionHeight = lensHeightPx,
                    refractionAmount = lensAmountPx,
                    depthEffect = config.depthEffect,
                    chromaticAberration = config.chromaticAberration,
                )
            }
        }
    }

    val rimColor = if (config.highlightColor.isSpecified) config.highlightColor else Color.White
    val rimAlpha = (highlightAlpha * (config.highlightOpacity / EdgeHighlightAlpha)).coerceIn(0f, 1f)

    val highlightBlock: (() -> Highlight?)? = remember(applyEdgeEffects, plainBlur, rimColor, rimAlpha, animatedHighlightAngle) {
        if (applyEdgeEffects && !plainBlur) {
            {
                Highlight(
                    width = EdgeHighlightWidth,
                    style = HighlightStyle.Default(
                        color = rimColor.copy(alpha = rimAlpha),
                        angle = animatedHighlightAngle,
                    ),
                )
            }
        } else null
    }

    val shadowBlock: (() -> Shadow?)? = remember(applyEdgeEffects) {
        if (applyEdgeEffects) ({ Shadow.Default }) else null
    }

    val surfaceBlock: DrawScope.() -> Unit = remember(surfaceTintColor, config.surfaceOpacity) {
        {
            if (config.surfaceOpacity > 0f) {
                drawRect(color = surfaceTintColor.copy(alpha = config.surfaceOpacity), size = size)
            }
        }
    }

    return drawBackdrop(
        backdrop = backdrop,
        shape = shapeBlock,
        effects = effectsBlock,
        highlight = highlightBlock,
        shadow = shadowBlock,
        onDrawSurface = surfaceBlock,
        backdropScale = resolutionScale,
        frozen = frozen,
        loopBucket = loopBucket,
    )
}
