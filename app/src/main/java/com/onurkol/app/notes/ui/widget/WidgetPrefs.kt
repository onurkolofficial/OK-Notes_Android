package com.onurkol.app.notes.ui.widget

import android.content.Context

object WidgetPrefs {
    private const val PREFS_NAME = "com.onurkol.app.notes.widget.WidgetPrefs"
    private const val PREF_PREFIX_KEY = "appwidget_"

    fun saveNoteIdPref(context: Context, appWidgetId: Int, noteId: Long) {
        val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
        prefs.putLong(PREF_PREFIX_KEY + appWidgetId, noteId)
        prefs.apply()
    }

    fun loadNoteIdPref(context: Context, appWidgetId: Int): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, 0)
        return prefs.getLong(PREF_PREFIX_KEY + appWidgetId, -1L)
    }

    fun deleteNoteIdPref(context: Context, appWidgetId: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
        prefs.remove(PREF_PREFIX_KEY + appWidgetId)
        prefs.apply()
    }
}
