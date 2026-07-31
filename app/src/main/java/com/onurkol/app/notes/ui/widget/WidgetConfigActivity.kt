package com.onurkol.app.notes.ui.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.onurkol.app.notes.data.local.AppDatabase
import com.onurkol.app.notes.data.model.Note
import com.onurkol.app.notes.ui.theme.OKNotesTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WidgetConfigActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the result to CANCELED. This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(Activity.RESULT_CANCELED)

        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val noteDao = AppDatabase.getDatabase(applicationContext).noteDao()

        setContent {
            OKNotesTheme {
                var notes by remember { mutableStateOf<List<Note>>(emptyList()) }
                val coroutineScope = rememberCoroutineScope()

                LaunchedEffect(Unit) {
                    coroutineScope.launch(Dispatchers.IO) {
                        noteDao.getAllNotes().collect { fetchedNotes ->
                            withContext(Dispatchers.Main) {
                                notes = fetchedNotes
                            }
                        }
                    }
                }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Select a Note") }
                        )
                    }
                ) { innerPadding ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(notes) { note ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onNoteSelected(note.id)
                                    }
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = note.title.ifEmpty { "Untitled Note" },
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    if (note.isLocked) {
                                        Text(
                                            text = "Locked Note",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun onNoteSelected(noteId: Long) {
        val context = this
        WidgetPrefs.saveNoteIdPref(context, appWidgetId, noteId)

        // Update the widget
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val providerInfo = appWidgetManager.getAppWidgetInfo(appWidgetId)
        
        if (providerInfo != null) {
            val providerClass = providerInfo.provider.className
            if (providerClass.contains("NoteTitleWidgetProvider")) {
                NoteTitleWidgetProvider.updateAppWidget(context, appWidgetManager, appWidgetId)
            } else if (providerClass.contains("NoteContentWidgetProvider")) {
                NoteContentWidgetProvider.updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }

        // Make sure we pass back the original appWidgetId
        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(Activity.RESULT_OK, resultValue)
        finish()
    }
}
