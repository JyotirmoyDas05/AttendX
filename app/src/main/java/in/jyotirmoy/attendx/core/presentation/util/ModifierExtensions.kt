package `in`.jyotirmoy.attendx.core.presentation.util

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput

fun Modifier.bounceClick(
    scaleDown: Float = 0.90f,
    onClick: () -> Unit
) = composed {
    var buttonState by remember { mutableStateOf(ButtonState.Idle) }
    val scale by animateFloatAsState(
        if (buttonState == ButtonState.Pressed) scaleDown else 1f,
        label = "bounce"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
        .pointerInput(buttonState) {
            awaitPointerEventScope {
                buttonState = if (buttonState == ButtonState.Pressed) {
                    waitForUpOrCancellation()
                    ButtonState.Idle
                } else {
                    awaitFirstDown(false)
                    ButtonState.Pressed
                }
            }
        }
}

/**
 * Material 3 Expressive spring-based press effect with sympathetic neighbor bounce.
 *
 * Replicates the connected button group behavior from Material 3 Expressive music-app
 * controls, where pressing one button causes **neighboring buttons to react too** —
 * as if the entire group is connected by physical springs.
 *
 * ### How it works
 *
 * Each button in a connected group has two spring layers:
 *
 * 1. **Direct press** — when THIS button is pressed, it scales down to [scaleDown] using
 *    the Expressive FastSpatial spring (dampingRatio=0.6f, stiffness=800f). On release it
 *    overshoots past 1.0 before settling — the signature "juicy" bounce.
 *
 * 2. **Sympathetic bounce** — when a NEIGHBOR button is pressed, this button does a gentler
 *    squeeze to [neighborScaleDown] using the Expressive DefaultSpatial spring
 *    (dampingRatio=0.8f, stiffness=380f) — slower, softer, with subtle overshoot. This
 *    creates the illusion that the press force propagates through the group.
 *
 * The two scales are **multiplied together**, so if both fire simultaneously the effect
 * compounds naturally (e.g., rapid tapping across buttons).
 *
 * Applied via [graphicsLayer] for GPU-accelerated transforms (zero recomposition cost).
 *
 * @param interactionSource The [InteractionSource] of THIS button.
 * @param neighborInteractionSources [InteractionSource]s of adjacent buttons in the group.
 *   When any neighbor is pressed, this button applies the sympathetic squeeze.
 * @param scaleDown Target scale when THIS button is pressed. Default 0.88f.
 * @param neighborScaleDown Target scale when a NEIGHBOR is pressed. Default 0.96f (subtle).
 */
fun Modifier.springyPressEffect(
    interactionSource: InteractionSource,
    neighborInteractionSources: List<InteractionSource> = emptyList(),
    scaleDown: Float = 0.88f,
    neighborScaleDown: Float = 0.96f
) = composed {
    val isPressed by interactionSource.collectIsPressedAsState()

    // Track whether ANY neighbor is currently pressed
    val neighborPressed = neighborInteractionSources.map { source ->
        val pressed by source.collectIsPressedAsState()
        pressed
    }
    val anyNeighborPressed = neighborPressed.any { it }

    // ── Layer 1: Direct press spring (FastSpatial — bouncy) ──
    val directScale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1f,
        animationSpec = spring(
            dampingRatio = 0.6f,  // Underdamped → visible overshoot
            stiffness = 800f      // Snappy response
        ),
        label = "directPressScale"
    )

    // ── Layer 2: Sympathetic neighbor spring (DefaultSpatial — softer) ──
    val sympatheticScale by animateFloatAsState(
        targetValue = if (anyNeighborPressed && !isPressed) neighborScaleDown else 1f,
        animationSpec = spring(
            dampingRatio = 0.8f,  // Subtle overshoot — gentle ripple
            stiffness = 380f      // Slower, lazier response than direct press
        ),
        label = "sympatheticScale"
    )

    // Multiply both scales — compounds naturally if both fire at once
    this.graphicsLayer {
        val combinedScale = directScale * sympatheticScale
        scaleX = combinedScale
        scaleY = combinedScale
    }
}

enum class ButtonState {
    Idle,
    Pressed
}
