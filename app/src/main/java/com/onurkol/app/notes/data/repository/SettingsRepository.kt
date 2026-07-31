package com.onurkol.app.notes.data.repository

import com.onurkol.app.notes.data.local.SettingsDataStore
import com.onurkol.app.notes.data.model.AppLanguage
import com.onurkol.app.notes.data.model.AppSettings
import com.onurkol.app.notes.data.model.ThemeMode
import com.onurkol.app.notes.data.model.ViewMode
import kotlinx.coroutines.flow.Flow

class SettingsRepository(private val settingsDataStore: SettingsDataStore) {
    val settingsFlow: Flow<AppSettings> = settingsDataStore.settingsFlow

    suspend fun setThemeMode(themeMode: ThemeMode) {
        settingsDataStore.setThemeMode(themeMode)
    }

    suspend fun setUseDynamicColor(useDynamicColor: Boolean) {
        settingsDataStore.setUseDynamicColor(useDynamicColor)
    }

    suspend fun setViewMode(viewMode: ViewMode) {
        settingsDataStore.setViewMode(viewMode)
    }

    suspend fun setAppLanguage(appLanguage: AppLanguage) {
        settingsDataStore.setAppLanguage(appLanguage)
    }
}
