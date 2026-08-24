package `in`.jyotirmoy.attendx.settings.presentation.components.item

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import `in`.jyotirmoy.attendx.settings.domain.model.PreferenceItem

@Composable
fun PreferenceItemView(
    item: PreferenceItem,
    modifier: Modifier = Modifier,
    roundedShape: RoundedCornerShape = RoundedCornerShape(25.dp),
    settingsViewModel: `in`.jyotirmoy.attendx.settings.presentation.viewmodel.SettingsViewModel = androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel()
) {
    when (item) {
        is PreferenceItem.BoolPreferenceItem -> BooleanPreferenceItemView(
            item = item,
            modifier = modifier,
            roundedShape = roundedShape,
            settingsViewModel = settingsViewModel
        )

        is PreferenceItem.IntPreferenceItem -> IntPreferenceItemView(
            item = item,
            modifier = modifier,
            settingsViewModel = settingsViewModel
        )

        is PreferenceItem.NullPreferenceItem -> NullPreferenceItemView(
            item = item,
            modifier = modifier,
            roundedShape = roundedShape,
            settingsViewModel = settingsViewModel
        )

        else -> {}
    }
}