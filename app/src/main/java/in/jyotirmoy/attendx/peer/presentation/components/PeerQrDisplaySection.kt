package `in`.jyotirmoy.attendx.peer.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.rounded.HourglassBottom
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import `in`.jyotirmoy.attendx.peer.domain.model.PeerSessionState

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PeerQrDisplaySection(
    state: PeerSessionState.Hosting,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val minutes = state.secondsRemaining / 60
    val seconds = state.secondsRemaining % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Title & Instruction
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Show QR to your Peer",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Ask your friend to tap 'Scan & Compare' on their device and scan this code.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        // QR Code Card
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = state.qrBitmap.asImageBitmap(),
                    contentDescription = "Peer Comparison QR Code",
                    modifier = Modifier
                        .size(260.dp)
                        .clip(RoundedCornerShape(20.dp))
                )
            }
        }

        // Timer & Waiting status with Material Expressive animated Hourglass
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MaterialExpressiveHourglass(
                size = 20.dp,
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = "Expires in $formattedTime",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }

        // Waiting radar animation row
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                LoadingIndicator(
                    modifier = Modifier.size(36.dp)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Waiting for peer to scan...",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "The screen will advance automatically once paired.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(Modifier.weight(1f))

        // Cancel button
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(Icons.Outlined.Close, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Cancel Session")
        }
    }
}

/**
 * 3-Phase Material Expressive Animated Hourglass:
 * 1. Sand Flow: Drains from top to bottom (0% to 75% of cycle).
 * 2. Expressive Flip: Snappy 180-degree rotation using FastOutSlowInEasing (75% to 100%).
 * 3. Continuous Seamless Loop.
 */
@Composable
fun MaterialExpressiveHourglass(
    modifier: Modifier = Modifier,
    size: Dp = 18.dp,
    tint: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    cycleDurationMs: Int = 2200
) {
    val infiniteTransition = rememberInfiniteTransition(label = "HourglassLoop")

    val sandProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = cycleDurationMs
                0f at 0 with LinearEasing
                1f at (cycleDurationMs * 0.75f).toInt() with LinearEasing
                1f at cycleDurationMs with LinearEasing
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "SandFlow"
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 180f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = cycleDurationMs
                0f at 0 with FastOutSlowInEasing
                0f at (cycleDurationMs * 0.75f).toInt() with FastOutSlowInEasing
                180f at cycleDurationMs with FastOutSlowInEasing
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "HourglassFlip"
    )

    Box(
        modifier = modifier
            .size(size)
            .rotate(rotationAngle),
        contentAlignment = Alignment.Center
    ) {
        if (sandProgress < 0.5f) {
            Icon(
                imageVector = Icons.Rounded.HourglassTop,
                contentDescription = "Timer running - draining",
                modifier = Modifier.size(size),
                tint = tint
            )
        } else {
            Icon(
                imageVector = Icons.Rounded.HourglassBottom,
                contentDescription = "Timer running - filling",
                modifier = Modifier.size(size),
                tint = tint
            )
        }
    }
}

