package `in`.jyotirmoy.attendx.core.presentation.components.state

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import `in`.jyotirmoy.attendx.core.common.LocalWeakHaptic

/**
 * Centred empty / error state. The app previously inlined a bare centred Text at
 * every such point, which made "no results" and "the request failed" look
 * identical.
 */
@Composable
fun StatusMessage(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    StatusMessageLayout(
        modifier = modifier,
        title = title,
        description = description,
        actionLabel = actionLabel,
        onAction = onAction
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(48.dp)
        )
    }
}

/**
 * Same state, led by an illustration instead of an icon. For the empty states
 * worth giving some warmth to, rather than every "no results" in the app.
 *
 * The illustration is decorative — [title] already says what is going on — so it
 * carries no content description and screen readers skip straight to the text.
 */
@Composable
fun StatusMessage(
    @DrawableRes illustration: Int,
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    StatusMessageLayout(
        modifier = modifier,
        title = title,
        description = description,
        actionLabel = actionLabel,
        onAction = onAction
    ) {
        // Sized from what is actually available rather than a fixed dp: the same
        // artwork has to sit above two lines of text on a short landscape pane
        // and on a tall phone, and a constant would overflow one or look lost on
        // the other. ContentScale.Fit keeps the aspect ratio whichever wins.
        val illustrationWidth = minOf(maxWidth * 0.62f, maxHeight * 0.42f, 240.dp)

        // Below this the artwork is a smudge and, worse, squeezes the message it
        // is meant to support: at a 2x font scale on a short pane the title and
        // date alone already fill the space. Dropping to text only there is the
        // better-looking outcome, and was chosen after comparing both on device.
        if (illustrationWidth >= 96.dp) {
            Image(
                painter = painterResource(id = illustration),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.sizeIn(
                    maxWidth = illustrationWidth,
                    maxHeight = illustrationWidth
                )
            )
        }
    }
}

@Composable
private fun StatusMessageLayout(
    modifier: Modifier,
    title: String,
    description: String?,
    actionLabel: String?,
    onAction: (() -> Unit)?,
    graphic: @Composable BoxWithConstraintsScope.() -> Unit
) {
    val weakHaptic = LocalWeakHaptic.current

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Held so the graphic slot can still measure against the available space
        // once we are inside the Column's own scope.
        val available = this
        Column(
            modifier = Modifier
                .fillMaxSize()
                // Scrolls rather than crops: at a large font scale the title and
                // description alone can outgrow a landscape pane.
                .verticalScroll(rememberScrollState())
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            available.graphic()

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp)
            )

            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (actionLabel != null && onAction != null) {
                Button(
                    onClick = { weakHaptic(); onAction() },
                    modifier = Modifier.padding(top = 24.dp)
                ) {
                    Text(actionLabel)
                }
            }
        }
    }
}
