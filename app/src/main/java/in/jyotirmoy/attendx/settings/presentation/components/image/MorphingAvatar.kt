@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.jyotirmoy.attendx.settings.presentation.components.image

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationEndReason
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicatorDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val QuarterRotation = 90f
private const val FullRotation = 360f
private const val MorphIntervalMillis = 650L
private const val GlobalRotationDurationMillis = 4666

// lazy dev: expressive loading indicator morph avatar
@Composable
fun MorphingProfileAvatar(
    avatarUrl: String,
    modifier: Modifier = Modifier,
    avatarSize: Dp = 104.dp,
    showGlowBorder: Boolean = true,
    contentDescription: String? = null
) {
    val polygons: List<RoundedPolygon> = remember {
        try {
            LoadingIndicatorDefaults.IndeterminateIndicatorPolygons
        } catch (_: Exception) {
            listOf(
                MaterialShapes.SoftBurst,
                MaterialShapes.Cookie9Sided,
                MaterialShapes.Pentagon,
                MaterialShapes.Pill,
                MaterialShapes.Sunny,
                MaterialShapes.Cookie4Sided,
                MaterialShapes.Oval
            )
        }
    }

    val morphs = remember(polygons) {
        List(polygons.size) { i ->
            Morph(polygons[i], polygons[(i + 1) % polygons.size])
        }
    }

    val morphProgress = remember { Animatable(0f) }
    var morphRotationTargetAngle by remember { mutableFloatStateOf(QuarterRotation) }
    val globalRotation = remember { Animatable(0f) }
    var currentMorphIndex by remember(polygons) { mutableIntStateOf(0) }

    LaunchedEffect(polygons) {
        // 1. Spring-based shape morph loop with quarter-turn step
        launch {
            val morphAnimationSpec = spring<Float>(
                dampingRatio = 0.6f,
                stiffness = 200f,
                visibilityThreshold = 0.05f
            )
            while (true) {
                val deferred = async {
                    morphProgress.animateTo(
                        targetValue = 1f,
                        animationSpec = morphAnimationSpec
                    )
                }
                delay(MorphIntervalMillis)
                val animationResult = deferred.await()
                if (animationResult.endReason == AnimationEndReason.Finished) {
                    currentMorphIndex = (currentMorphIndex + 1) % morphs.size
                    morphProgress.snapTo(0f)
                    morphRotationTargetAngle = (morphRotationTargetAngle + QuarterRotation) % FullRotation
                }
            }
        }

        // 2. Smooth continuous global rotation
        launch {
            globalRotation.animateTo(
                targetValue = FullRotation,
                animationSpec = infiniteRepeatable(
                    animation = tween(GlobalRotationDurationMillis, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
        }
    }

    val borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)

    Box(
        modifier = modifier
            .size(avatarSize)
            .graphicsLayer {
                compositingStrategy = CompositingStrategy.Offscreen
            }
            .drawWithContent {
                val cWidth = size.width
                val cHeight = size.height
                val progress = morphProgress.value.coerceIn(0f, 1f)

                val currentMorph = morphs[currentMorphIndex]
                val path = currentMorph
                    .toPath(progress = progress)
                    .asComposePath()

                val bounds = path.getBounds()
                val targetSize = minOf(cWidth, cHeight) * 0.90f
                val scale = if (bounds.width > 0f && bounds.height > 0f) {
                    targetSize / maxOf(bounds.width, bounds.height)
                } else {
                    1f
                }

                // Center and scale normalized polygon into canvas
                val matrix = Matrix().apply {
                    reset()
                    translate(cWidth / 2f, cHeight / 2f)
                    scale(scale, scale)
                    translate(-bounds.left - bounds.width / 2f, -bounds.top - bounds.height / 2f)
                }
                path.transform(matrix)

                val totalRotation = progress * QuarterRotation + morphRotationTargetAngle + globalRotation.value

                // 1. Draw rotated morph mask shape
                rotate(totalRotation) {
                    drawPath(
                        path = path,
                        color = Color.Black
                    )
                }

                // 2. Draw avatar image over mask using BlendMode.SrcIn
                drawContent()

                // 3. Glowing border following the morphing rotation
                if (showGlowBorder) {
                    rotate(totalRotation) {
                        drawPath(
                            path = path,
                            color = borderColor,
                            style = Stroke(width = 2.5.dp.toPx())
                        )
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(avatarUrl)
                .crossfade(true)
                .build(),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    blendMode = BlendMode.SrcIn
                }
        )
    }
}
