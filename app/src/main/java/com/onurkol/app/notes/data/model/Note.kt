package com.onurkol.app.notes.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val colorHex: Long = 0xFFFFFFFF,
    val category: String = "General",
    val isLocked: Boolean = false,
    val password: String? = null
)
