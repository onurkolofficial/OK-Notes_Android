package com.onurkol.app.notes.data.model

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class ViewMode {
    GRID,
    LIST
}

enum class AppLanguage {
    ENGLISH,
    TURKISH
}

interface AppDefaults {
    companion object {
        val THEME_MODE = ThemeMode.SYSTEM
        const val USE_DYNAMIC_COLOR = false
        val VIEW_MODE = ViewMode.GRID
        val APP_LANGUAGE = AppLanguage.ENGLISH
    }
}

data class AppSettings(
    val themeMode: ThemeMode = AppDefaults.THEME_MODE,
    val useDynamicColor: Boolean = AppDefaults.USE_DYNAMIC_COLOR,
    val viewMode: ViewMode = AppDefaults.VIEW_MODE,
    val appLanguage: AppLanguage = AppDefaults.APP_LANGUAGE
)
