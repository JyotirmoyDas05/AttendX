package `in`.jyotirmoy.attendx.peer.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CompareArrows
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.LinkOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import `in`.jyotirmoy.attendx.peer.data.model.PeerSubjectPayload
import `in`.jyotirmoy.attendx.peer.domain.model.MatchConfidence
import `in`.jyotirmoy.attendx.peer.domain.model.SubjectMatchProposal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeerSubjectConfirmSection(
    proposals: List<SubjectMatchProposal>,
    allGuestSubjects: List<PeerSubjectPayload>,
    onUpdateProposal: (index: Int, guestSubject: PeerSubjectPayload?) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var editingIndex by remember { mutableStateOf<Int?>(null) }
    val sheetState = rememberModalBottomSheetState()

    val matchedCount = proposals.count { it.guestSubject != null }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.CompareArrows,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Confirm Subject Pairings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Auto-matched by subject codes and names. Tap any row to adjust or exclude subjects.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // List of subject pairs
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(proposals) { index, proposal ->
                SubjectProposalCard(
                    proposal = proposal,
                    onClick = { editingIndex = index }
                )
            }
        }

        // Bottom CTAs
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onConfirm,
                enabled = matchedCount > 0,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (matchedCount > 0) "Compare $matchedCount Subject${if (matchedCount > 1) "s" else ""}"
                    else "Match at least 1 subject"
                )
            }

            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel")
            }
        }
    }

    // BottomSheet for changing / pairing guest subjects
    if (editingIndex != null) {
        val index = editingIndex!!
        val currentProposal = proposals[index]

        ModalBottomSheet(
            onDismissRequest = { editingIndex = null },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Pair '${currentProposal.hostSubject.originalName}' with:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Option: Exclude / No Match
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            onUpdateProposal(index, null)
                            editingIndex = null
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RadioButton(
                        selected = currentProposal.guestSubject == null,
                        onClick = {
                            onUpdateProposal(index, null)
                            editingIndex = null
                        }
                    )
                    Text(
                        text = "Do not match (Exclude from comparison)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                // Available Guest Subjects
                allGuestSubjects.forEach { guestSub ->
                    val isSelected = currentProposal.guestSubject?.originalName == guestSub.originalName
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                onUpdateProposal(index, guestSub)
                                editingIndex = null
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = {
                                onUpdateProposal(index, guestSub)
                                editingIndex = null
                            }
                        )
                        Column {
                            Text(
                                text = guestSub.originalName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (!guestSub.subjectCode.isNullOrBlank()) {
                                Text(
                                    text = "Code: ${guestSub.subjectCode}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SubjectProposalCard(
    proposal: SubjectMatchProposal,
    onClick: () -> Unit
) {
    val isMatched = proposal.guestSubject != null

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isMatched) MaterialTheme.colorScheme.surfaceVariant
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Confidence Badge & Edit trigger
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ConfidenceBadge(confidence = proposal.confidence, isMatched = isMatched)
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "Edit Pairing",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Subject Comparison Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Host Subject Box
                SubjectPill(
                    name = proposal.hostSubject.originalName,
                    code = proposal.hostSubject.subjectCode,
                    percentage = proposal.hostSubject.attendancePercentage,
                    label = "You",
                    modifier = Modifier.weight(1f)
                )

                // Match Icon
                Icon(
                    imageVector = if (isMatched) Icons.Outlined.Link else Icons.Outlined.LinkOff,
                    contentDescription = null,
                    tint = if (isMatched) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )

                // Guest Subject Box
                if (proposal.guestSubject != null) {
                    SubjectPill(
                        name = proposal.guestSubject.originalName,
                        code = proposal.guestSubject.subjectCode,
                        percentage = proposal.guestSubject.attendancePercentage,
                        label = "Peer",
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f))
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Unmatched / Excluded",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SubjectPill(
    name: String,
    code: String?,
    percentage: Float,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${String.format("%.1f", percentage)}%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ConfidenceBadge(
    confidence: MatchConfidence,
    isMatched: Boolean
) {
    val (text, color, containerColor) = when {
        !isMatched -> Triple("No Match", MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.errorContainer)
        confidence == MatchConfidence.AUTO_CODE -> Triple("Code Match", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
        confidence == MatchConfidence.AUTO_NAME -> Triple("Name Match", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
        confidence == MatchConfidence.FUZZY -> Triple("Suggested", MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.tertiaryContainer)
        else -> Triple("Manual Pair", MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.secondaryContainer)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(containerColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
