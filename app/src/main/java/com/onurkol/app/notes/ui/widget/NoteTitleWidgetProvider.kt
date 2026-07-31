package com.onurkol.app.notes.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.onurkol.app.notes.MainActivity
import com.onurkol.app.notes.R
import com.onurkol.app.notes.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NoteTitleWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            WidgetPrefs.deleteNoteIdPref(context, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val noteId = WidgetPrefs.loadNoteIdPref(context, appWidgetId)
            val views = RemoteViews(context.packageName, R.layout.widget_note_title)

            if (noteId == -1L) {
                views.setTextViewText(R.id.widget_note_title_text, context.getString(R.string.app_name))
                appWidgetManager.updateAppWidget(appWidgetId, views)
                return
            }

            // Launch app when clicked
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_note_title_text, pendingIntent)

            CoroutineScope(Dispatchers.IO).launch {
                val db = AppDatabase.getDatabase(context)
                val note = db.noteDao().getNoteByIdSync(noteId)

                if (note != null) {
                    val titleText = if (note.isLocked) {
                        context.getString(R.string.locked_note)
                    } else {
                        note.title.ifEmpty { context.getString(R.string.untitled_note) }
                    }
                    views.setTextViewText(R.id.widget_note_title_text, titleText)

                    if (note.colorHex == 0xFFFFFFFF || note.colorHex == 0xFF000000) {
                        views.setImageViewResource(R.id.widget_background_image, R.drawable.widget_background)
                    } else {
                        views.setImageViewResource(R.id.widget_background_image, R.drawable.widget_background_solid)
                        views.setInt(R.id.widget_background_image, "setColorFilter", note.colorHex.toInt())
                    }
                } else {
                    views.setTextViewText(R.id.widget_note_title_text, "Note not found")
                }

                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }
}
