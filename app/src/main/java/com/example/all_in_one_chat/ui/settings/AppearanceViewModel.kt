package com.example.all_in_one_chat.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.all_in_one_chat.data.local.prefs.AppearancePreferences
import com.example.all_in_one_chat.data.local.prefs.ThemeMode
import com.example.all_in_one_chat.service.analytics.AnalyticsHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppearanceState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColorEnabled: Boolean = true
)

@HiltViewModel
class AppearanceViewModel @Inject constructor(
    private val appearancePreferences: AppearancePreferences,
    private val analyticsHelper: AnalyticsHelper
) : ViewModel() {

    val uiState: StateFlow<AppearanceState> = combine(
        appearancePreferences.themeMode,
        appearancePreferences.dynamicColorEnabled
    ) { mode, dynamicColor ->
        AppearanceState(
            themeMode = ThemeMode.valueOf(mode),
            dynamicColorEnabled = dynamicColor
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppearanceState()
    )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            appearancePreferences.setThemeMode(mode)
            analyticsHelper.logThemeChanged(mode.name)
        }
    }

    fun setDynamicColorEnabled(enabled: Boolean) {
        viewModelScope.launch {
            appearancePreferences.setDynamicColorEnabled(enabled)
        }
    }
}
