package com.onurkol.app.notes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.onurkol.app.notes.data.local.AppDatabase
import com.onurkol.app.notes.data.local.SettingsDataStore
import com.onurkol.app.notes.data.model.ThemeMode
import com.onurkol.app.notes.data.repository.NoteRepository
import com.onurkol.app.notes.data.repository.SettingsRepository
import com.onurkol.app.notes.ui.localization.LocaleHelper
import com.onurkol.app.notes.ui.screen.NoteDetailScreen
import com.onurkol.app.notes.ui.screen.NoteListScreen
import com.onurkol.app.notes.ui.screen.Screen
import com.onurkol.app.notes.ui.screen.SettingsScreen
import com.onurkol.app.notes.ui.theme.OKNotesTheme
import com.onurkol.app.notes.ui.viewmodel.NoteViewModel
import com.onurkol.app.notes.ui.viewmodel.SettingsViewModel
import com.onurkol.app.notes.ui.viewmodel.ViewModelFactory
import com.startapp.sdk.adsbase.StartAppSDK
import com.startapp.sdk.adsbase.StartAppAd

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize StartApp SDK
        StartAppSDK.setTestAdsEnabled(BuildConfig.DEBUG)
        val startAppId = getString(R.string.startapp_app_id)
        StartAppSDK.initParams(this, startAppId).init()
        StartAppAd.disableSplash()

        // Initialize dependencies
        val database = AppDatabase.getDatabase(applicationContext)
        val settingsDataStore = SettingsDataStore(applicationContext)

        val noteRepository = NoteRepository(database.noteDao())
        val settingsRepository = SettingsRepository(settingsDataStore)

        val viewModelFactory = ViewModelFactory(noteRepository, settingsRepository)

        val noteViewModel = ViewModelProvider(this, viewModelFactory)[NoteViewModel::class.java]
        val settingsViewModel = ViewModelProvider(this, viewModelFactory)[SettingsViewModel::class.java]

        setContent {
            val settings by settingsViewModel.settingsState.collectAsStateWithLifecycle()

            val darkTheme = when (settings.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            val context = LocalContext.current
            val localizedContext = remember(settings.appLanguage) {
                LocaleHelper.updateLocale(context, settings.appLanguage)
            }

            CompositionLocalProvider(LocalContext provides localizedContext) {
                OKNotesTheme(
                    darkTheme = darkTheme,
                    dynamicColor = settings.useDynamicColor
                ) {
                    val backStack = remember { mutableStateListOf<Any>(Screen.NoteList) }

                    NavDisplay(
                        backStack = backStack,
                        onBack = {
                            if (backStack.size > 1) {
                                backStack.removeAt(backStack.size - 1)
                            } else {
                                finish()
                            }
                        },
                        entryProvider = entryProvider {
                            entry<Screen.NoteList> {
                                NoteListScreen(
                                    noteViewModel = noteViewModel,
                                    settingsViewModel = settingsViewModel,
                                    onNavigateToSettings = { backStack.add(Screen.Settings) },
                                    onNavigateToNoteDetail = { noteId ->
                                        backStack.add(Screen.NoteDetail(noteId))
                                    }
                                )
                            }
                            entry<Screen.NoteDetail> { key ->
                                NoteDetailScreen(
                                    noteId = key.noteId,
                                    noteViewModel = noteViewModel,
                                    settingsViewModel = settingsViewModel,
                                    onBack = {
                                        if (backStack.size > 1) backStack.removeAt(backStack.size - 1)
                                    }
                                )
                            }
                            entry<Screen.Settings> {
                                SettingsScreen(
                                    settingsViewModel = settingsViewModel,
                                    onBack = {
                                        if (backStack.size > 1) backStack.removeAt(backStack.size - 1)
                                    }
                                )
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}