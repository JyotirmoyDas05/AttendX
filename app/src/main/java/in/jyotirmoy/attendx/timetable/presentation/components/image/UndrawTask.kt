package `in`.jyotirmoy.attendx.timetable.presentation.components.image

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import `in`.jyotirmoy.attendx.R

/**
 * Undraw Task illustration with dynamic theming.
 * Uses ?attr/colorPrimary in XML for theme-aware accent color.
 */
@Composable
fun UndrawTask(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.undraw_task),
            contentDescription = "No classes scheduled",
            modifier = Modifier.padding(32.dp)
        )
    }
}


