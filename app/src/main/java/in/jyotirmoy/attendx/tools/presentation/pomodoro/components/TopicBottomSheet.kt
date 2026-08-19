package `in`.jyotirmoy.attendx.tools.presentation.pomodoro.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.jyotirmoy.attendx.tools.data.model.Topic

val TOPIC_COLORS = listOf(
    Color(0xFFA8C7FA),
    Color(0xFFFFB4AB),
    Color(0xFFF2B8B5),
    Color(0xFFE8B7ED),
    Color(0xFFC4C0FF),
    Color(0xFF7DD6FA)
)

val TOPIC_SHAPES_COUNT = 25

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicBottomSheet(
    initialTopic: Topic? = null,
    onDismiss: () -> Unit,
    onSaveTopic: (Topic) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    var topicName by remember { mutableStateOf(initialTopic?.name ?: "") }
    var selectedColorIndex by remember {
        val idx = TOPIC_COLORS.indexOfFirst {
            val hex = "#" + Integer.toHexString(it.toArgb()).uppercase()
            initialTopic?.colorHex.equals(hex, ignoreCase = true)
        }
        mutableIntStateOf(if (idx >= 0) idx else 0)
    }
    var selectedShapeIndex by remember { mutableIntStateOf(initialTopic?.shapeIndex ?: 0) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colorScheme.surfaceContainerHigh
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Text(
                text = if (initialTopic == null) "Create new topic" else "Edit topic",
                style = typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 28.sp
                ),
                color = colorScheme.onSurface
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Name",
                style = typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = topicName,
                onValueChange = { topicName = it },
                placeholder = { Text("Topic name...") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colorScheme.surfaceContainer,
                    unfocusedContainerColor = colorScheme.surfaceContainer,
                    focusedBorderColor = colorScheme.primary,
                    unfocusedBorderColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Color",
                style = typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                TOPIC_COLORS.forEachIndexed { index, color ->
                    val isSelected = selectedColorIndex == index
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(color)
                            .clickable { selectedColorIndex = index }
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Shape",
                style = typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                items(TOPIC_SHAPES_COUNT) { index ->
                    val isSelected = selectedShapeIndex == index
                    val shape = when (index % 5) {
                        0 -> CircleShape
                        1 -> RoundedCornerShape(12.dp)
                        2 -> RoundedCornerShape(16.dp, 4.dp, 16.dp, 4.dp)
                        3 -> RoundedCornerShape(20.dp, 20.dp, 4.dp, 4.dp)
                        else -> RoundedCornerShape(4.dp, 20.dp, 4.dp, 20.dp)
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(shape)
                            .background(
                                if (isSelected) colorScheme.primary else colorScheme.surfaceContainer
                            )
                            .border(
                                width = if (isSelected) 0.dp else 1.5.dp,
                                color = if (isSelected) Color.Transparent else colorScheme.outline.copy(alpha = 0.4f),
                                shape = shape
                            )
                            .clickable { selectedShapeIndex = index }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(shape)
                                .border(
                                    width = 1.5.dp,
                                    color = if (isSelected) colorScheme.onPrimary else colorScheme.primary,
                                    shape = shape
                                )
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", fontSize = 16.sp)
                }
                Spacer(Modifier.width(12.dp))
                Button(
                    onClick = {
                        val chosenColor = TOPIC_COLORS[selectedColorIndex]
                        val hex = "#" + Integer.toHexString(chosenColor.toArgb()).uppercase()
                        val finalName = topicName.ifBlank { "New Topic" }
                        onSaveTopic(
                            Topic(
                                id = initialTopic?.id ?: 0,
                                name = finalName,
                                colorHex = hex,
                                shapeIndex = selectedShapeIndex,
                                focusTime = initialTopic?.focusTime ?: (25 * 60 * 1000L),
                                shortBreakTime = initialTopic?.shortBreakTime ?: (5 * 60 * 1000L),
                                longBreakTime = initialTopic?.longBreakTime ?: (15 * 60 * 1000L)
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primary,
                        contentColor = colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text(
                        text = if (initialTopic == null) "Next" else "Save",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
