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
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.shapes
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.jyotirmoy.attendx.tools.data.model.Topic

// M3 color palette for topics
val TOPIC_COLORS = listOf(
    Color(0xFFA8C7FA), // Blue
    Color(0xFFFFB4AB), // Red
    Color(0xFFF2B8B5), // Rose
    Color(0xFFE8B7ED), // Purple
    Color(0xFFC4C0FF), // Indigo
    Color(0xFF7DD6FA), // Cyan
    Color(0xFFA8DAB5), // Green
    Color(0xFFFFD6A5), // Amber
)

// M3 shape variants — matching Tomato+ M3 Expressive shapes
val TOPIC_SHAPES: List<@Composable () -> Shape> = listOf(
    { CircleShape },
    { shapes.large },
    { shapes.extraLarge },
    { RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 4.dp, bottomEnd = 4.dp) },
    { RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 28.dp, bottomEnd = 28.dp) },
    { RoundedCornerShape(topStart = 28.dp, topEnd = 4.dp, bottomStart = 4.dp, bottomEnd = 28.dp) },
    { RoundedCornerShape(topStart = 4.dp, topEnd = 28.dp, bottomStart = 28.dp, bottomEnd = 4.dp) },
    { CutCornerShape(16.dp) },
    { CutCornerShape(topStart = 16.dp, topEnd = 0.dp, bottomStart = 0.dp, bottomEnd = 16.dp) },
    { CutCornerShape(topStart = 0.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 0.dp) },
    { RoundedCornerShape(50) },
    { RoundedCornerShape(topStartPercent = 50, topEndPercent = 50, bottomStartPercent = 0, bottomEndPercent = 0) },
    { RoundedCornerShape(topStartPercent = 0, topEndPercent = 0, bottomStartPercent = 50, bottomEndPercent = 50) },
    { CutCornerShape(topStart = 24.dp, topEnd = 0.dp, bottomStart = 24.dp, bottomEnd = 0.dp) },
    { CutCornerShape(topStart = 0.dp, topEnd = 24.dp, bottomStart = 0.dp, bottomEnd = 24.dp) },
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
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

            // Name
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
                shape = shapes.large,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colorScheme.surfaceContainer,
                    unfocusedContainerColor = colorScheme.surfaceContainer,
                    focusedBorderColor = colorScheme.primary,
                    unfocusedBorderColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))

            // Color picker
            Text(
                text = "Color",
                style = typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(8),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                items(TOPIC_COLORS.size) { index ->
                    val isSelected = selectedColorIndex == index
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(CircleShape)
                            .background(TOPIC_COLORS[index])
                            .then(
                                if (isSelected) Modifier.border(
                                    width = 3.dp,
                                    color = colorScheme.onSurface,
                                    shape = CircleShape
                                ) else Modifier
                            )
                            .clickable { selectedColorIndex = index }
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Shape picker — using M3 shapes from Tomato
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
                items(TOPIC_SHAPES.size) { index ->
                    val isSelected = selectedShapeIndex == index
                    val shape = TOPIC_SHAPES[index]()
                    val topicColor = TOPIC_COLORS[selectedColorIndex]

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(shape)
                            .background(
                                if (isSelected) topicColor else colorScheme.surfaceContainer
                            )
                            .border(
                                width = if (isSelected) 0.dp else 1.5.dp,
                                color = if (isSelected) Color.Transparent else colorScheme.outline.copy(
                                    alpha = 0.4f
                                ),
                                shape = shape
                            )
                            .clickable { selectedShapeIndex = index }
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = Color.Black.copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Actions
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
                    shape = shapes.large,
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
