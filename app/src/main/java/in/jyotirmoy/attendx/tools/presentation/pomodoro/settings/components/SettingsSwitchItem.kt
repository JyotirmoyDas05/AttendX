package `in`.jyotirmoy.attendx.tools.presentation.pomodoro.settings.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class SettingsSwitchItem(
    val checked: Boolean,
    val enabled: Boolean = true,
    val collapsible: Boolean = false,
    @param:DrawableRes val icon: Int,
    @param:StringRes val label: Int,
    @param:StringRes val description: Int,
    val onClick: (Boolean) -> Unit
)
