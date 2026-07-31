package com.onurkol.app.notes.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.onurkol.app.notes.data.model.AppDefaults
import com.onurkol.app.notes.data.model.AppLanguage
import com.onurkol.app.notes.data.model.AppSettings
import com.onurkol.app.notes.data.model.ThemeMode
import com.onurkol.app.notes.data.model.ViewMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings_prefs")

class SettingsDataStore(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey(PreferenceKeys.KEY_THEME_MODE)
        val USE_DYNAMIC_COLOR = booleanPreferencesKey(PreferenceKeys.KEY_USE_DYNAMIC_COLOR)
        val VIEW_MODE = stringPreferencesKey(PreferenceKeys.KEY_VIEW_MODE)
        val APP_LANGUAGE = stringPreferencesKey(PreferenceKeys.KEY_APP_LANGUAGE)
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { preferences ->
        val themeModeName = preferences[Keys.THEME_MODE] ?: AppDefaults.THEME_MODE.name
        val themeMode = runCatching { ThemeMode.valueOf(themeModeName) }.getOrDefault(AppDefaults.THEME_MODE)

        val useDynamicColor = preferences[Keys.USE_DYNAMIC_COLOR] ?: AppDefaults.USE_DYNAMIC_COLOR

        val viewModeName = preferences[Keys.VIEW_MODE] ?: AppDefaults.VIEW_MODE.name
        val viewMode = runCatching { ViewMode.valueOf(viewModeName) }.getOrDefault(AppDefaults.VIEW_MODE)

        val appLanguageName = preferences[Keys.APP_LANGUAGE] ?: AppDefaults.APP_LANGUAGE.name
        val appLanguage = runCatching { AppLanguage.valueOf(appLanguageName) }.getOrDefault(AppDefaults.APP_LANGUAGE)

        AppSettings(
            themeMode = themeMode,
            useDynamicColor = useDynamicColor,
            viewMode = viewMode,
            appLanguage = appLanguage
        )
    }

    suspend fun setThemeMode(themeMode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[Keys.THEME_MODE] = themeMode.name
        }
    }

    suspend fun setUseDynamicColor(useDynamicColor: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.USE_DYNAMIC_COLOR] = useDynamicColor
        }
    }

    suspend fun setViewMode(viewMode: ViewMode) {
        context.dataStore.edit { preferences ->
            preferences[Keys.VIEW_MODE] = viewMode.name
        }
    }

    suspend fun setAppLanguage(appLanguage: AppLanguage) {
        context.dataStore.edit { preferences ->
            preferences[Keys.APP_LANGUAGE] = appLanguage.name
        }
    }
}
