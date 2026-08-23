package `in`.jyotirmoy.attendx.tools.presentation.pomodoro.settings.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.theme.PomodoroColors.listItemColors

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SliderListItem(
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    enabled: Boolean,
    label: String,
    trailingLabel: (Float) -> String,
    shape: CornerBasedShape,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    containerColor: Color = listItemColors.containerColor,
    onValueChangeFinished: (Float) -> Unit,
) {
    var animateSliderValue by remember { mutableStateOf(true) }

    var localValue by remember(value) { mutableFloatStateOf(value) }
    val valueAnimated by animateFloatAsState(
        localValue,
        animationSpec = if (animateSliderValue) motionScheme.defaultSpatialSpec()
        else snap()
    )

    Column(modifier.background(containerColor, shape)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            if (icon != null) {
                CompositionLocalProvider(LocalContentColor provides colorScheme.onSurfaceVariant) {
                    icon()
                }
                Spacer(Modifier.width(16.dp))
            }
            Text(label, style = typography.bodyLarge)
            Spacer(Modifier.weight(1f))
            Text(
                trailingLabel(localValue),
                style = typography.labelMedium,
                color = colorScheme.onSurfaceVariant
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 8.dp)
        ) {
            if (icon != null) {
                Spacer(Modifier.width(36.dp))
            }
            Slider(
                value = valueAnimated,
                onValueChange = {
                    animateSliderValue = false
                    localValue = it
                },
                onValueChangeFinished = {
                    animateSliderValue = true
                    onValueChangeFinished(localValue)
                },
                valueRange = valueRange,
                enabled = enabled,
                modifier = Modifier.weight(1f)
            )
            if (trailingIcon != null) {
                Spacer(Modifier.width(20.dp))
                trailingIcon()
            }
        }
    }
}
