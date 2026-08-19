package `in`.jyotirmoy.attendx.tools.presentation.pomodoro.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.jyotirmoy.attendx.R
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.model.TimerMode
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.model.TimerState

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AlwaysOnDisplay(
    timerState: TimerState,
    progress: () -> Float,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val color by animateColorAsState(
        if (timerState.timerMode == TimerMode.FOCUS) colorScheme.primary else colorScheme.tertiary,
        label = "aodColor"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(onClick = onDismiss)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = when (timerState.timerMode) {
                    TimerMode.FOCUS -> if (timerState.infiniteFocus) stringResource(R.string.infinite_focus) else stringResource(R.string.focus)
                    TimerMode.SHORT_BREAK -> stringResource(R.string.short_break)
                    TimerMode.LONG_BREAK -> stringResource(R.string.long_break)
                    else -> stringResource(R.string.timer)
                },
                style = MaterialTheme.typography.titleMedium,
                color = color.copy(alpha = 0.8f)
            )

            Spacer(Modifier.height(32.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .widthIn(max = 300.dp)
                    .aspectRatio(1f)
            ) {
                if (!timerState.infiniteFocus) {
                    if (timerState.timerMode == TimerMode.FOCUS) {
                        CircularProgressIndicator(
                            progress = progress,
                            modifier = Modifier.fillMaxSize(0.9f),
                            color = color,
                            trackColor = Color.DarkGray.copy(alpha = 0.4f),
                            strokeWidth = 8.dp,
                            gapSize = 4.dp
                        )
                    } else {
                        CircularWavyProgressIndicator(
                            progress = progress,
                            modifier = Modifier.fillMaxSize(0.9f),
                            color = color,
                            trackColor = Color.DarkGray.copy(alpha = 0.4f),
                            stroke = Stroke(width = 8f, cap = StrokeCap.Round),
                            trackStroke = Stroke(width = 8f, cap = StrokeCap.Round),
                            wavelength = 40.dp,
                            gapSize = 4.dp
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = timerState.timeStr,
                        style = TextStyle(
                            fontSize = 64.sp,
                            letterSpacing = (-1.5).sp,
                            color = Color.White.copy(alpha = 0.9f)
                        ),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(
                            R.string.timer_session_count,
                            timerState.currentFocusCount,
                            timerState.totalFocusCount
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.always_on_display_desc),
                style = MaterialTheme.typography.bodySmall,
                color = Color.DarkGray,
                textAlign = TextAlign.Center
            )
        }
    }
}
