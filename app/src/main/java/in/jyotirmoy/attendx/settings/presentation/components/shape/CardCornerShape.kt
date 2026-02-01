package `in`.jyotirmoy.attendx.settings.presentation.components.shape

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

object CardCornerShape {
    val SINGLE_CARD = RoundedCornerShape(25.dp)

    val FIRST_CARD = RoundedCornerShape(
        topStart = 25.dp,
        topEnd = 25.dp,
        bottomStart = 10.dp,
        bottomEnd = 10.dp
    )

    val MIDDLE_CARD = RoundedCornerShape(10.dp)

    val LAST_CARD = RoundedCornerShape(
        topStart = 10.dp,
        topEnd = 10.dp,
        bottomStart = 25.dp,
        bottomEnd = 25.dp
    )

    fun getRoundedShape(
        index: Int,
        size: Int,
    ): RoundedCornerShape {
        return when {
            size == 1 -> SINGLE_CARD

            index == 0 -> FIRST_CARD

            index == size - 1 -> LAST_CARD

            else -> MIDDLE_CARD
        }
    }
}
