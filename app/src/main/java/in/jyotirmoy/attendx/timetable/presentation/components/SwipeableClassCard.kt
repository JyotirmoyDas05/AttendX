package `in`.jyotirmoy.attendx.timetable.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import `in`.jyotirmoy.attendx.timetable.data.model.TimeTableScheduleWithSubject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableClassCard(
    scheduleWithSubject: TimeTableScheduleWithSubject,
    isSelected: Boolean,
    onSwipeDelete: () -> Unit,
    onLongPress: () -> Unit,
    onClick: () -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current
    var hasTriggeredHaptic by remember { mutableStateOf(false) }
    
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onSwipeDelete()
                true
            } else {
                false
            }
        }
    )
    
    // swipe progress (0 to 1)
    val progress = when (dismissState.targetValue) {
        SwipeToDismissBoxValue.EndToStart -> dismissState.progress
        else -> 0f
    }
    
    // haptic at 50% threshold
    LaunchedEffect(progress) {
        if (progress >= 0.5f && !hasTriggeredHaptic) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            hasTriggeredHaptic = true
        } else if (progress < 0.5f) {
            hasTriggeredHaptic = false
        }
    }

    // progressive color: transparent → errorContainer
    val backgroundColor by animateColorAsState(
        targetValue = lerp(
            Color.Transparent,
            MaterialTheme.colorScheme.errorContainer,
            progress.coerceIn(0f, 1f)
        ),
        animationSpec = tween(100),
        label = "dismissBackground"
    )
    
    // icon scale: 0.6 → 1.2 based on progress
    val iconScale by animateFloatAsState(
        targetValue = lerp(0.6f, 1.2f, progress.coerceIn(0f, 1f)),
        animationSpec = tween(100),
        label = "dismissIconScale"
    )
    
    // icon alpha: fade in as user swipes
    val iconAlpha by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(100),
        label = "dismissIconAlpha"
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(backgroundColor)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    modifier = Modifier
                        .scale(iconScale)
                        .alpha(iconAlpha),
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    ) {
        SelectableClassCard(
            scheduleWithSubject = scheduleWithSubject,
            isSelected = isSelected,
            onLongPress = onLongPress,
            onClick = onClick
        )
    }
}

private fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return start + (stop - start) * fraction
}
