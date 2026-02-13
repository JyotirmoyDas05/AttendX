package `in`.jyotirmoy.attendx.timetable.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Publish
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExpressiveFabMenu(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onNavigateToUpload: () -> Unit,
    onNavigateToMarketplace: () -> Unit,
    onManualAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.BottomEnd
    ) {
        FloatingActionButtonMenu(
            expanded = isExpanded,
            button = {
                ToggleFloatingActionButton(
                    checked = isExpanded,
                    onCheckedChange = { onToggle() },
                ) {
                    val rotation by animateFloatAsState(
                        targetValue = if (isExpanded) 135f else 0f,
                        label = "fabRotation"
                    )
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = if (isExpanded) "Close Menu" else "Add Options",
                        modifier = Modifier.rotate(rotation)
                    )
                }
            }
        ) {
            // Note: Native FAB Menu usually displays items from Bottom to Top (closest to FAB = last declared?)
            // Or Top to Bottom?
            // Material 3 guidelines: "The first item in the list is the one closest to the FAB" ?
            // Let's assume declarations order matches visual stack.
            // Check order: usually it's declared order.
          
            FloatingActionButtonMenuItem(
                onClick = { 
                    onToggle()
                    onManualAdd() 
                },
                icon = { Icon(Icons.Rounded.Edit, contentDescription = null) },
                text = { Text(text = "Start from scratch") },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            FloatingActionButtonMenuItem(
                onClick = { 
                    onToggle()
                    onNavigateToMarketplace() 
                },
                icon = { Icon(Icons.Rounded.Download, contentDescription = null) },
                text = { Text(text = "Import") },
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            )
            
            FloatingActionButtonMenuItem(
                onClick = { 
                    onToggle()
                    onNavigateToUpload() 
                },
                icon = { Icon(Icons.Rounded.Publish, contentDescription = null) },
                text = { Text(text = "Export") },
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}
