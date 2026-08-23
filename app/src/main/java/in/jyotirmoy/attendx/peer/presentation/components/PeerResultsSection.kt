package `in`.jyotirmoy.attendx.peer.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.TrendingDown
import androidx.compose.material.icons.outlined.TrendingFlat
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import `in`.jyotirmoy.attendx.peer.domain.model.SubjectComparisonResult
import kotlin.math.abs

@Composable
fun PeerResultsSection(
    comparisons: List<SubjectComparisonResult>,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val avgDelta = if (comparisons.isNotEmpty()) {
        comparisons.map { it.delta }.average().toFloat()
    } else 0f

    val higherCount = comparisons.count { it.delta > 0 }
    val lowerCount = comparisons.count { it.delta < 0 }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Summary Header Card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (avgDelta >= 0) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.tertiaryContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = when {
                        avgDelta > 0.5f -> Icons.Outlined.TrendingUp
                        avgDelta < -0.5f -> Icons.Outlined.TrendingDown
                        else -> Icons.Outlined.TrendingFlat
                    },
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = if (avgDelta >= 0) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onTertiaryContainer
                )

                Text(
                    text = when {
                        avgDelta > 0.5f -> "${String.format("%.1f", avgDelta)}% Higher on Average"
                        avgDelta < -0.5f -> "${String.format("%.1f", abs(avgDelta))}% Lower on Average"
                        else -> "Equal Overall Attendance"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (avgDelta >= 0) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onTertiaryContainer
                )

                Text(
                    text = "Compared across ${comparisons.size} matched subject${if (comparisons.size > 1) "s" else ""} • Higher in $higherCount, Lower in $lowerCount",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (avgDelta >= 0) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    else MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Subject Breakdown Cards
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(comparisons) { item ->
                ResultRowCard(item = item)
            }
        }

        // Ephemeral Disclaimer Badge
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "This comparison session is temporary and was not stored.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Finish button
        Button(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Outlined.CheckCircle, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Done")
        }
    }
}

@Composable
private fun ResultRowCard(
    item: SubjectComparisonResult
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Subject Title & Delta Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                DeltaBadge(delta = item.delta)
            }

            // Dual Progress Comparison Bars
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                // You bar
                ProgressBarRow(
                    label = "You",
                    percentage = item.hostPercentage,
                    barColor = MaterialTheme.colorScheme.primary
                )

                // Peer bar
                ProgressBarRow(
                    label = "Peer",
                    percentage = item.guestPercentage,
                    barColor = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
private fun ProgressBarRow(
    label: String,
    percentage: Float,
    barColor: Color
) {
    val animatedProgress by animateFloatAsState(
        targetValue = (percentage / 100f).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800),
        label = "progress_bar"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(36.dp)
        )

        // Progress Bar
        Box(
            modifier = Modifier
                .weight(1f)
                .height(10.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .clip(CircleShape)
                    .background(barColor)
            )
        }

        Text(
            text = "${String.format("%.1f", percentage)}%",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(48.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun DeltaBadge(delta: Float) {
    val isPositive = delta > 0.05f
    val isNegative = delta < -0.05f

    val text = when {
        isPositive -> "+${String.format("%.1f", delta)}%"
        isNegative -> "${String.format("%.1f", delta)}%"
        else -> "0.0%"
    }

    val (bg, fg) = when {
        isPositive -> Pair(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )
        isNegative -> Pair(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer
        )
        else -> Pair(
            MaterialTheme.colorScheme.surfaceContainerHighest,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = fg
        )
    }
}
