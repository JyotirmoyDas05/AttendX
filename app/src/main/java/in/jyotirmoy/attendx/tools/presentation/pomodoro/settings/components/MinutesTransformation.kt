package `in`.jyotirmoy.attendx.tools.presentation.pomodoro.settings.components

import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.core.text.isDigitsOnly

class MinutesInputTransformation(val maxDigits: Int) : InputTransformation {
    override fun TextFieldBuffer.transformInput() {
        if (!this.asCharSequence().isDigitsOnly() || this.length > maxDigits) {
            revertAllChanges()
        }
    }
}

val MinutesInputTransformation2Digits = MinutesInputTransformation(2)
val MinutesInputTransformation3Digits = MinutesInputTransformation(3)
