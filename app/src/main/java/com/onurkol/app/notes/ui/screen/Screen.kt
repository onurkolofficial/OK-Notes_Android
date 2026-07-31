package com.onurkol.app.notes.ui.screen

import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable
    data object NoteList : Screen

    @Serializable
    data class NoteDetail(val noteId: Long? = null) : Screen

    @Serializable
    data object Settings : Screen
}
