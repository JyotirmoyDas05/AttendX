@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class)

package `in`.jyotirmoy.attendx.timetable.presentation.screens.template.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.jyotirmoy.attendx.core.common.LocalWeakHaptic
import `in`.jyotirmoy.attendx.core.presentation.components.card.RoundedCornerCard
import `in`.jyotirmoy.attendx.timetable.data.model.community.TemplateClassEntry
import `in`.jyotirmoy.attendx.timetable.data.model.community.TemplateSubjectEntry
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

/**
 * The subject and class pickers shared by the import and export screens.
 *
 * Both screens ask the same question of the same data, so they render from one
 * source here rather than each maintaining its own copy that drifts. They are
 * `LazyListScope` extensions rather than composables so each screen keeps
 * control of its own list — headers, padding and empty states differ.
 */

/**
 * Count plus a select-all toggle. Sits above a list so the user can clear a long
 * selection without scrolling through every row to untick it.
 */
@Composable
fun SelectionSummary(
    label: String,
    selectedCount: Int,
    totalCount: Int,
    onToggleAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val weakHaptic = LocalWeakHaptic.current
    val allSelected = selectedCount == totalCount && totalCount > 0

    // Flows to a second line instead of squeezing the label when the count text
    // or the button label grows.
    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
        verticalArrangement = Arrangement.Center,
        itemVerticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$selectedCount of $totalCount $label selected",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f, fill = false)
        )
        TextButton(onClick = {
            weakHaptic()
            onToggleAll()
        }) {
            Text(if (allSelected) "Clear all" else "Select all")
        }
    }
}

fun LazyListScope.subjectSelectionItems(
    subjects: List<TemplateSubjectEntry>,
    selected: List<TemplateSubjectEntry>,
    onToggle: (TemplateSubjectEntry) -> Unit
) {
    items(subjects.size, key = { subjects[it].name }) { index ->
        val subject = subjects[index]
        val isSelected = selected.contains(subject)

        ListItem(
            headlineContent = {
                Text(text = subject.name, fontWeight = FontWeight.Medium)
            },
            supportingContent = if (!subject.code.isNullOrBlank()) {
                { Text(text = subject.code) }
            } else null,
            leadingContent = {
                SubjectInitial(name = subject.name, isSelected = isSelected)
            },
            trailingContent = {
                Checkbox(checked = isSelected, onCheckedChange = { onToggle(subject) })
            },
            colors = ListItemDefaults.colors(
                containerColor = if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                } else {
                    MaterialTheme.colorScheme.surfaceContainer
                }
            ),
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clip(MaterialTheme.shapes.medium)
                .clickable { onToggle(subject) }
        )
    }
}

fun LazyListScope.classSelectionItems(
    classes: List<TemplateClassEntry>,
    selected: List<TemplateClassEntry>,
    onToggle: (TemplateClassEntry) -> Unit
) {
    val grouped = classes.groupBy { it.dayOfWeek }.toSortedMap()
    val days = grouped.keys.toList()

    items(days.size, key = { days[it] }) { index ->
        val day = days[index]
        val classesForDay = grouped.getValue(day).sortedBy { it.startTime }

        RoundedCornerCard(
            roundedCornerShape = RoundedCornerShape(20.dp),
            paddingValues = PaddingValues(0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = dayLabel(day),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                classesForDay.forEachIndexed { position, classEntry ->
                    ClassSelectionRow(
                        classEntry = classEntry,
                        isSelected = selected.contains(classEntry),
                        onToggle = { onToggle(classEntry) }
                    )
                    if (position != classesForDay.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ClassSelectionRow(
    classEntry: TemplateClassEntry,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    ListItem(
        headlineContent = {
            Text(text = classEntry.subject, fontWeight = FontWeight.SemiBold)
        },
        supportingContent = {
            Column {
                if (!classEntry.room.isNullOrBlank()) {
                    Text(
                        text = classEntry.room,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "${classEntry.type} • ${formatTemplateTime(classEntry.startTime)} - " +
                        formatTemplateTime(classEntry.endTime),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        },
        trailingContent = {
            Checkbox(checked = isSelected, onCheckedChange = { onToggle() })
        },
        colors = ListItemDefaults.colors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        modifier = Modifier
            .padding(vertical = 4.dp)
            .clip(MaterialTheme.shapes.medium)
            .clickable { onToggle() }
    )
}

@Composable
private fun SubjectInitial(name: String, isSelected: Boolean) {
    Box(
        modifier = Modifier
            // Grows with the user's font scale rather than cropping the letter,
            // but never shrinks below the 40dp the layout is drawn around.
            .defaultMinSize(minWidth = 40.dp, minHeight = 40.dp)
            .sizeIn(maxWidth = 64.dp, maxHeight = 64.dp)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.small
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.take(1).uppercase(),
            style = MaterialTheme.typography.titleMedium,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

private fun dayLabel(dayOfWeek: Int): String =
    runCatching {
        DayOfWeek.of(dayOfWeek).getDisplayName(TextStyle.FULL, Locale.getDefault())
    }.getOrElse { "Day $dayOfWeek" }

/** Minutes past midnight to a locale-formatted 12-hour time. */
internal fun formatTemplateTime(minutes: Long): String {
    val hour = minutes / 60
    val minute = minutes % 60
    val isPm = hour >= 12
    val displayHour = when {
        hour == 0L -> 12L
        hour > 12L -> hour - 12L
        else -> hour
    }
    return String.format(
        Locale.getDefault(),
        "%d:%02d %s",
        displayHour,
        minute,
        if (isPm) "pm" else "am"
    )
}
