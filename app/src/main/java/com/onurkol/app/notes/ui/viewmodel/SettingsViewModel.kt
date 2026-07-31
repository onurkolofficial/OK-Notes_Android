package com.onurkol.app.notes.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onurkol.app.notes.data.model.AppLanguage
import com.onurkol.app.notes.data.model.AppSettings
import com.onurkol.app.notes.data.model.ThemeMode
import com.onurkol.app.notes.data.model.ViewMode
import com.onurkol.app.notes.data.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {

    val settingsState: StateFlow<AppSettings> = repository.settingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings()
        )

    fun setThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            repository.setThemeMode(themeMode)
        }
    }

    fun setUseDynamicColor(useDynamicColor: Boolean) {
        viewModelScope.launch {
            repository.setUseDynamicColor(useDynamicColor)
        }
    }

    fun setViewMode(viewMode: ViewMode) {
        viewModelScope.launch {
            repository.setViewMode(viewMode)
        }
    }

    fun setAppLanguage(appLanguage: AppLanguage) {
        viewModelScope.launch {
            repository.setAppLanguage(appLanguage)
        }
    }
}
