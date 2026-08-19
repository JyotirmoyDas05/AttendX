package `in`.jyotirmoy.attendx.tools.presentation.pomodoro.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.jyotirmoy.attendx.tools.data.repository.PreferenceRepository
import `in`.jyotirmoy.attendx.tools.data.repository.StatRepository
import `in`.jyotirmoy.attendx.tools.data.repository.StateRepository
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.model.SettingsAction
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.model.SettingsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferenceRepository: PreferenceRepository,
    private val statRepository: StatRepository,
    private val stateRepository: StateRepository
) : ViewModel() {

    val settingsState: StateFlow<SettingsState> = stateRepository.settingsState

    private val _isShowingEraseDialog = MutableStateFlow(false)
    val isShowingEraseDialog: StateFlow<Boolean> = _isShowingEraseDialog.asStateFlow()

    fun onAction(action: SettingsAction) {
        viewModelScope.launch {
            when (action) {
                is SettingsAction.SaveAlarmEnabled -> {
                    preferenceRepository.saveBooleanPreference("alarm_enabled", action.enabled)
                    stateRepository.reloadSettings()
                }
                is SettingsAction.SaveVibrateEnabled -> {
                    preferenceRepository.saveBooleanPreference("vibrate_enabled", action.enabled)
                    stateRepository.reloadSettings()
                }
                is SettingsAction.SaveBlackTheme -> {
                    preferenceRepository.saveBooleanPreference("black_theme", action.enabled)
                    stateRepository.reloadSettings()
                }
                is SettingsAction.SaveAodEnabled -> {
                    preferenceRepository.saveBooleanPreference("aod_enabled", action.enabled)
                    stateRepository.reloadSettings()
                }
                is SettingsAction.SaveDndEnabled -> {
                    preferenceRepository.saveBooleanPreference("dnd_enabled", action.enabled)
                    stateRepository.reloadSettings()
                }
                is SettingsAction.SaveMediaVolumeForAlarm -> {
                    preferenceRepository.saveBooleanPreference("media_volume_for_alarm", action.enabled)
                    stateRepository.reloadSettings()
                }
                is SettingsAction.SaveSingleProgressBar -> {
                    preferenceRepository.saveBooleanPreference("single_progress_bar", action.enabled)
                    stateRepository.reloadSettings()
                }
                is SettingsAction.SaveAutostartNextSession -> {
                    preferenceRepository.saveBooleanPreference("autostart_next_session", action.enabled)
                    stateRepository.reloadSettings()
                }
                is SettingsAction.SaveSecureAod -> {
                    preferenceRepository.saveBooleanPreference("secure_aod", action.enabled)
                    stateRepository.reloadSettings()
                }
                is SettingsAction.SaveAlarmSound -> {
                    preferenceRepository.saveStringPreference("alarm_sound", action.uri ?: "")
                    stateRepository.reloadSettings()
                }
                is SettingsAction.SaveTheme -> {
                    preferenceRepository.saveStringPreference("theme", action.theme)
                    stateRepository.reloadSettings()
                }
                is SettingsAction.SaveColorScheme -> {
                    preferenceRepository.saveStringPreference("color_scheme", action.color.toString())
                    stateRepository.reloadSettings()
                }
                is SettingsAction.SaveFocusGoal -> {
                    preferenceRepository.saveIntPreference("focus_goal", action.goal.toInt())
                    stateRepository.reloadSettings()
                }
                is SettingsAction.SaveFocusTime -> {
                    preferenceRepository.saveIntPreference("focus_time", action.time.toInt())
                    stateRepository.reloadSettings()
                }
                is SettingsAction.SaveShortBreakTime -> {
                    preferenceRepository.saveIntPreference("short_break_time", action.time.toInt())
                    stateRepository.reloadSettings()
                }
                is SettingsAction.SaveLongBreakTime -> {
                    preferenceRepository.saveIntPreference("long_break_time", action.time.toInt())
                    stateRepository.reloadSettings()
                }
                is SettingsAction.SaveSessionLength -> {
                    preferenceRepository.saveIntPreference("session_length", action.length)
                    stateRepository.reloadSettings()
                }
                is SettingsAction.SaveVibrationOnDuration -> {
                    preferenceRepository.saveIntPreference("vibration_on_duration", action.duration.toInt())
                    stateRepository.reloadSettings()
                }
                is SettingsAction.SaveVibrationOffDuration -> {
                    preferenceRepository.saveIntPreference("vibration_off_duration", action.duration.toInt())
                    stateRepository.reloadSettings()
                }
                is SettingsAction.SaveVibrationAmplitude -> {
                    preferenceRepository.saveIntPreference("vibration_amplitude", action.amplitude)
                    stateRepository.reloadSettings()
                }
                is SettingsAction.AskEraseData -> {
                    _isShowingEraseDialog.update { true }
                }
                is SettingsAction.CancelEraseData -> {
                    _isShowingEraseDialog.update { false }
                }
                is SettingsAction.EraseData -> {
                    _isShowingEraseDialog.update { false }
                    statRepository.deleteAllStats()
                }
            }
        }
    }
}
