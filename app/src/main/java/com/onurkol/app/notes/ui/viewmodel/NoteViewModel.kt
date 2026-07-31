package com.onurkol.app.notes.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onurkol.app.notes.data.model.Note
import com.onurkol.app.notes.data.repository.NoteRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NoteViewModel(private val repository: NoteRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val notesState: StateFlow<List<Note>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                repository.getAllNotes()
            } else {
                repository.searchNotes(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun getNoteById(id: Long) = repository.getNoteById(id)

    fun saveNote(
        id: Long? = null,
        title: String,
        content: String,
        isPinned: Boolean = false,
        colorHex: Long = 0xFFFFFFFF,
        category: String = "General",
        isLocked: Boolean = false,
        password: String? = null,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            if (id == null || id == 0L) {
                val newNote = Note(
                    title = title,
                    content = content,
                    isPinned = isPinned,
                    colorHex = colorHex,
                    category = category,
                    isLocked = isLocked,
                    password = password
                )
                repository.insertNote(newNote)
            } else {
                val existingNote = Note(
                    id = id,
                    title = title,
                    content = content,
                    isPinned = isPinned,
                    colorHex = colorHex,
                    category = category,
                    isLocked = isLocked,
                    password = password
                )
                repository.updateNote(existingNote)
            }
            onComplete()
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    fun deleteNoteById(id: Long) {
        viewModelScope.launch {
            repository.deleteNoteById(id)
        }
    }

    fun togglePin(note: Note) {
        viewModelScope.launch {
            repository.updateNote(note.copy(isPinned = !note.isPinned))
        }
    }

    fun persistNoteOrder(reorderedList: List<Note>) {
        viewModelScope.launch {
            val currentTime = System.currentTimeMillis()
            reorderedList.forEachIndexed { index, note ->
                val updatedNote = note.copy(timestamp = currentTime - index * 1000L)
                repository.updateNote(updatedNote)
            }
        }
    }
}
