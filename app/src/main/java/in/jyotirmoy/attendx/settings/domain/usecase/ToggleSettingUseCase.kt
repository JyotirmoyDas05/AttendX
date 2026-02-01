package `in`.jyotirmoy.attendx.settings.domain.usecase

import `in`.jyotirmoy.attendx.settings.data.local.SettingsKeys
import `in`.jyotirmoy.attendx.settings.domain.repository.SettingsRepository

class ToggleSettingUseCase(private val repo: SettingsRepository) {
    suspend operator fun invoke(key: SettingsKeys) = repo.toggleSetting(key)
}