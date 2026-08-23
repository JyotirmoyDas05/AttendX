package `in`.jyotirmoy.attendx.tools.presentation.pomodoro.settings.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.theme.PomodoroColors.listItemColors

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MinuteInputField(
    state: TextFieldState,
    enabled: Boolean,
    shape: Shape,
    modifier: Modifier = Modifier,
    inputTransformation: MinutesInputTransformation = MinutesInputTransformation2Digits,
    imeAction: ImeAction = ImeAction.Next
) {
    BasicTextField(
        state = state,
        enabled = enabled,
        lineLimits = TextFieldLineLimits.SingleLine,
        inputTransformation = inputTransformation,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.NumberPassword,
            imeAction = imeAction
        ),
        textStyle = TextStyle(
            fontFamily = typography.bodyLarge.fontFamily,
            fontSize = 57.sp,
            letterSpacing = (-2).sp,
            color = if (enabled) colorScheme.onSurfaceVariant else colorScheme.outlineVariant,
            textAlign = TextAlign.Center
        ),
        cursorBrush = SolidColor(colorScheme.onSurface),
        decorator = { innerTextField ->
            val text = state.text
            val width by animateDpAsState(
                if (text.length < 3) 112.dp else 140.dp,
                motionScheme.defaultSpatialSpec()
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = modifier
                    .size(width, 100.dp)
                    .background(
                        animateColorAsState(
                            if (text.isNotEmpty())
                                listItemColors.containerColor
                            else colorScheme.errorContainer,
                            motionScheme.defaultEffectsSpec()
                        ).value,
                        shape
                    )
            ) { innerTextField() }
        }
    )
}
