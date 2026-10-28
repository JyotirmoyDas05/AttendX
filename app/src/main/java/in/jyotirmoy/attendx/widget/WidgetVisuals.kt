package `in`.jyotirmoy.attendx.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import android.util.TypedValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ColumnScope
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.unit.ColorProvider
import `in`.jyotirmoy.attendx.R
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.sin

// Concentric corners: an inner container's radius is the card radius minus the card padding.
internal val CardPadding = 14.dp
internal val CardRadius = 28.dp
internal val InnerRadius = CardRadius - CardPadding
internal val Gap = 8.dp
private val TilePadding = 10.dp
private val BarHeight = 14.dp

/** Size classes: stretching a widget past these reveals extra content instead of empty space. */
internal val DpSize.isWide get() = width >= 320.dp
internal val DpSize.isTall get() = height >= 280.dp

/** Google Sans Flex axis presets, matching the app's Pomodoro display and body styles. */
internal enum class FlexStyle(val weight: Int, val width: Float, val round: Float) {
    Display(900, 112.5f, 35f),
    Title(750, 100f, 100f),
    // Slightly condensed so tile figures like "100%" fit a 2x2 tile without ellipsizing.
    Figure(850, 90f, 100f),
    Label(600, 100f, 100f),
    Body(450, 100f, 100f)
}

/**
 * Text set in the bundled Google Sans Flex. RemoteViews can't take a custom typeface, so the
 * text is drawn as a white mask and tinted by Glance: the tint resolves at inflation, which
 * keeps dark mode and dynamic colour live without re-rendering.
 */
@Composable
internal fun FlexText(
    text: String,
    sizeSp: Float,
    color: ColorProvider,
    style: FlexStyle = FlexStyle.Body,
    maxWidth: Dp? = null,
    maxLines: Int = 1,
    /** Shrink a single line (down to 60%) before ellipsizing. For figures, not names: names keep one size. */
    autoShrink: Boolean = false,
    modifier: GlanceModifier = GlanceModifier
) {
    val context = LocalContext.current
    Image(
        provider = ImageProvider(flexTextMask(context, text, sizeSp, style, maxWidth, maxLines, autoShrink)),
        contentDescription = text,
        colorFilter = ColorFilter.tint(color),
        modifier = modifier
    )
}

/** Outer shell shared by every widget: one tonal container, concentric with the launcher's corner. */
@Composable
internal fun WidgetCard(
    container: ColorProvider,
    onClick: Action,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(container)
            .cornerRadius(CardRadius)
            .padding(CardPadding)
            .clickable(onClick),
        content = content
    )
}

/** Bento tile: a big emphasised figure over a short label. */
@Composable
internal fun StatTile(
    value: String,
    label: String,
    valueColor: ColorProvider,
    width: Dp,
    modifier: GlanceModifier = GlanceModifier
) {
    val colors = GlanceTheme.colors
    Column(
        modifier = modifier
            .background(colors.surface)
            .cornerRadius(InnerRadius)
            .padding(horizontal = TilePadding, vertical = 10.dp)
    ) {
        FlexText(value, 22f, valueColor, FlexStyle.Figure, maxWidth = width - TilePadding * 2, autoShrink = true)
        FlexText(label, 11f, colors.onSurfaceVariant, FlexStyle.Label, maxWidth = width - TilePadding * 2, autoShrink = true)
    }
}

internal val IconButtonSize = 32.dp

/** Round icon button, M3 "filled tonal" style by default. */
@Composable
internal fun IconButton(
    icon: Int,
    description: String,
    onClick: Action,
    container: ColorProvider = GlanceTheme.colors.surface,
    content: ColorProvider = GlanceTheme.colors.onSurface
) {
    Box(
        modifier = GlanceModifier
            .size(IconButtonSize)
            .background(container)
            .cornerRadius(IconButtonSize / 2)
            .clickable(onClick),
        contentAlignment = Alignment.Center
    ) {
        Image(
            provider = ImageProvider(icon),
            contentDescription = description,
            colorFilter = ColorFilter.tint(content),
            modifier = GlanceModifier.size(18.dp)
        )
    }
}

/** Small section heading inside a card, e.g. "Coming up". */
@Composable
internal fun SectionLabel(text: String, color: ColorProvider) {
    FlexText(text, 12f, color, FlexStyle.Label, modifier = GlanceModifier.padding(bottom = 6.dp))
}

@Composable
internal fun Pill(text: String, container: ColorProvider, content: ColorProvider) {
    Box(
        modifier = GlanceModifier
            .background(container)
            .cornerRadius(50.dp)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        FlexText(text, 11f, content, FlexStyle.Label)
    }
}

/**
 * M3 Expressive wavy progress: a wavy active indicator, a gap, a flat track and a stop dot.
 * Fill and track are separate masks so each can carry its own theme tint.
 */
@Composable
internal fun WavyProgress(fraction: Float, width: Dp, fill: ColorProvider, track: ColorProvider) {
    val f = fraction.coerceIn(0f, 1f)
    val context = LocalContext.current
    val (fillMask, trackMask) = progressMasks(context, width, f)
    Box(modifier = GlanceModifier.fillMaxWidth().height(BarHeight)) {
        trackMask?.let { ProgressLayer(it, track) }
        fillMask?.let { ProgressLayer(it, fill) }
    }
}

@Composable
private fun ProgressLayer(mask: Bitmap, color: ColorProvider) {
    Image(
        provider = ImageProvider(mask),
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        colorFilter = ColorFilter.tint(color),
        modifier = GlanceModifier.fillMaxSize()
    )
}

private var flexTypeface: Typeface? = null

private fun flexTextMask(
    context: Context,
    text: String,
    sizeSp: Float,
    style: FlexStyle,
    maxWidth: Dp?,
    maxLines: Int,
    autoShrink: Boolean
): Bitmap {
    val metrics = context.resources.displayMetrics
    val typeface = flexTypeface
        ?: (ResourcesCompat.getFont(context, R.font.google_sans_flex) ?: Typeface.DEFAULT)
            .also { flexTypeface = it }
    val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        this.typeface = typeface
        // Sp, so the widget follows the user's font scale like native text would.
        textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sizeSp, metrics)
        color = Color.WHITE
        setFontVariationSettings(
            "'wght' ${style.weight}, 'wdth' ${style.width}, 'ROND' ${style.round}, 'opsz' $sizeSp"
        )
    }
    val maxPx = maxWidth?.let { (it.value * metrics.density).toInt().coerceAtLeast(1) }
    var natural = ceil(paint.measureText(text)).toInt().coerceAtLeast(1)
    // Figures shrink to fit (down to 60%) before they ellipsize, so "100%" stays whole. The 3%
    // margin matters: StaticLayout measures a hair wider than measureText and would ellipsize
    // text shrunk to exactly the limit.
    if (autoShrink && maxLines == 1 && maxPx != null && natural > maxPx) {
        paint.textSize *= (maxPx * 0.97f / natural).coerceAtLeast(0.6f)
        natural = ceil(paint.measureText(text)).toInt().coerceAtLeast(1)
    }
    // A couple of pixels of slack for the same reason, so text that measured as fitting does fit.
    val limit = (maxPx?.coerceAtMost(natural) ?: natural) + (2 * metrics.density).toInt()
    val layout = StaticLayout.Builder.obtain(text, 0, text.length, paint, limit)
        .setMaxLines(maxLines)
        .setEllipsize(TextUtils.TruncateAt.END)
        .setIncludePad(false)
        .build()
    return Bitmap.createBitmap(limit, layout.height.coerceAtLeast(1), Bitmap.Config.ALPHA_8).apply {
        density = metrics.densityDpi
        layout.draw(Canvas(this))
    }
}

private fun progressMasks(context: Context, width: Dp, fraction: Float): Pair<Bitmap?, Bitmap?> {
    val density = context.resources.displayMetrics.density
    val w = (width.value * density).toInt().coerceAtLeast(1)
    val h = (BarHeight.value * density).toInt().coerceAtLeast(1)
    val stroke = 4.5f * density
    val amplitude = 3f * density
    val wavelength = 24f * density
    val gap = 4f * density
    val mid = h / 2f
    val start = stroke / 2
    val end = w - stroke / 2
    val split = start + (end - start) * fraction
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = stroke
        strokeCap = Paint.Cap.ROUND
    }

    val fill = if (fraction > 0f) {
        Bitmap.createBitmap(w, h, Bitmap.Config.ALPHA_8).also { bitmap ->
            val path = Path().apply {
                moveTo(start, mid)
                var x = start
                while (x < split) {
                    x = (x + density).coerceAtMost(split)
                    lineTo(x, mid + amplitude * sin(2 * PI * (x - start) / wavelength).toFloat())
                }
            }
            Canvas(bitmap).drawPath(path, paint)
        }
    } else null

    val trackStart = if (fraction > 0f) split + gap + stroke else start
    val track = if (fraction < 1f && trackStart < end) {
        Bitmap.createBitmap(w, h, Bitmap.Config.ALPHA_8).also { bitmap ->
            val canvas = Canvas(bitmap)
            val lineEnd = end - stroke * 2
            if (trackStart < lineEnd) canvas.drawLine(trackStart, mid, lineEnd, mid, paint)
            // Stop indicator: marks where 100% is.
            paint.style = Paint.Style.FILL
            canvas.drawCircle(end, mid, stroke / 2, paint)
        }
    } else null

    return fill to track
}
