package com.onurkol.app.notes.ui.localization

import android.content.Context
import com.onurkol.app.notes.data.model.AppLanguage
import java.util.Locale

object LocaleHelper {
    fun updateLocale(context: Context, appLanguage: AppLanguage): Context {
        val languageCode = if (appLanguage == AppLanguage.TURKISH) "tr" else "en"
        val locale = Locale.forLanguageTag(languageCode)
        Locale.setDefault(locale)
        
        val resources = context.resources
        val configuration = resources.configuration
        
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        
        return context.createConfigurationContext(configuration)
    }
}
