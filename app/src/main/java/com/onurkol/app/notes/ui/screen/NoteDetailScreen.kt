package com.onurkol.app.notes.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.onurkol.app.notes.R
import com.onurkol.app.notes.ui.viewmodel.NoteViewModel
import com.onurkol.app.notes.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.flow.flowOf
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

private val noteColors = listOf(
    0xFFFFFFFF, // Default
    0xFFFFB3B3, // Pastel Red
    0xFFB3D9FF, // Pastel Blue
    0xFFC2F0C2, // Pastel Green
    0xFFFFE0B3, // Pastel Orange/Yellow
    0xFFE6CCFF  // Pastel Purple
)

private val categories = listOf(
    "Genel",
    "İş",
    "Kişisel",
    "Fikir",
    "Önemli"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    noteId: Long?,
    noteViewModel: NoteViewModel,
    settingsViewModel: SettingsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val existingNoteState = remember(noteId) {
        if (noteId != null && noteId != 0L) {
            noteViewModel.getNoteById(noteId)
        } else {
            flowOf(null)
        }
    }.collectAsStateWithLifecycle(initialValue = null)

    val settings by settingsViewModel.settingsState.collectAsStateWithLifecycle()

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Genel") }
    var isPinned by remember { mutableStateOf(false) }
    var colorHex by remember { mutableStateOf(0xFFFFFFFF) }
    var isLocked by remember { mutableStateOf(false) }
    var notePassword by remember { mutableStateOf<String?>(null) }

    var isInitialized by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var dialogPassword by remember { mutableStateOf("") }
    var dialogError by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(existingNoteState.value) {
        existingNoteState.value?.let { note ->
            if (!isInitialized) {
                title = note.title
                content = note.content
                category = note.category
                isPinned = note.isPinned
                colorHex = note.colorHex
                isLocked = note.isLocked
                notePassword = note.password
                isInitialized = true
            }
        }
    }

    val catGenel = stringResource(R.string.category_general)
    val catIs = stringResource(R.string.category_work)
    val catKisisel = stringResource(R.string.category_personal)
    val catFikir = stringResource(R.string.category_idea)
    val catOnemli = stringResource(R.string.category_important)

    fun getCategoryDisplayName(cat: String): String {
        return when (cat) {
            "Genel" -> catGenel
            "İş" -> catIs
            "Kişisel" -> catKisisel
            "Fikir" -> catFikir
            "Önemli" -> catOnemli
            else -> cat
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (noteId == null) stringResource(R.string.new_note) else stringResource(R.string.edit_note),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        showPasswordDialog = true
                        dialogPassword = ""
                        dialogError = false
                    }) {
                        Icon(
                            imageVector = if (isLocked) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                            contentDescription = stringResource(R.string.lock_note),
                            tint = if (isLocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { isPinned = !isPinned }) {
                        Icon(
                            imageVector = Icons.Rounded.PushPin,
                            contentDescription = stringResource(R.string.pin),
                            tint = if (isPinned) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (noteId != null && noteId != 0L) {
                        IconButton(onClick = {
                            showDeleteConfirmDialog = true
                        }) {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = stringResource(R.string.delete),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (title.isNotBlank() || content.isNotBlank()) {
                        noteViewModel.saveNote(
                            id = noteId,
                            title = title,
                            content = content,
                            isPinned = isPinned,
                            colorHex = colorHex,
                            category = category,
                            isLocked = isLocked,
                            password = notePassword,
                            onComplete = onBack
                        )
                    } else {
                        Toast.makeText(context, context.getString(R.string.empty_note_warning), Toast.LENGTH_SHORT).show()
                    }
                },
                icon = { Icon(Icons.Rounded.Save, contentDescription = null) },
                text = { Text(stringResource(R.string.save)) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Note Category Selector
            Text(
                text = stringResource(R.string.select_category),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = category == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { category = cat },
                        label = { Text(getCategoryDisplayName(cat)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            // Note Color Selector
            Text(
                text = stringResource(R.string.note_color),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                noteColors.forEach { colorVal ->
                    val isSelected = colorHex == colorVal
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(colorVal))
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.5f),
                                shape = CircleShape
                            )
                            .clickable { colorHex = colorVal },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = if (colorVal == 0xFFFFFFFF) Color.Black else Color.DarkGray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Title input
            TextField(
                value = title,
                onValueChange = { title = it },
                placeholder = {
                    Text(
                        stringResource(R.string.title_placeholder),
                        style = TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    )
                },
                textStyle = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Content input
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                placeholder = { Text(stringResource(R.string.content_placeholder)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .defaultMinSize(minHeight = 250.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                    unfocusedContainerColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(80.dp)) // Leave space for FAB
        }

        val strDeleteNoteTitle = stringResource(R.string.delete_note_title)
        val strDeleteNoteConfirm = stringResource(R.string.delete_note_confirm)
        val strDelete = stringResource(R.string.delete)
        val strCancel = stringResource(R.string.cancel)

        val strSetPasswordTitle = stringResource(if (isLocked) R.string.remove_password else R.string.set_password)
        val strEnterPassword = stringResource(R.string.enter_password)
        val strIncorrectPassword = stringResource(R.string.incorrect_password)
        val strOk = stringResource(R.string.ok)

        if (showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                title = { Text(strDeleteNoteTitle) },
                text = { Text(strDeleteNoteConfirm) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteConfirmDialog = false
                            if (noteId != null) {
                                noteViewModel.deleteNoteById(noteId)
                                onBack()
                            }
                        }
                    ) {
                        Text(strDelete, color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmDialog = false }) {
                        Text(strCancel)
                    }
                }
            )
        }

        if (showPasswordDialog) {
            AlertDialog(
                onDismissRequest = { showPasswordDialog = false },
                title = { Text(strSetPasswordTitle) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = dialogPassword,
                            onValueChange = { 
                                dialogPassword = it
                                dialogError = false
                            },
                            singleLine = true,
                            isError = dialogError,
                            placeholder = { Text(strEnterPassword) }
                        )
                        if (dialogError) {
                            Text(strIncorrectPassword, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (isLocked) {
                                if (dialogPassword == notePassword) {
                                    isLocked = false
                                    notePassword = null
                                    showPasswordDialog = false
                                } else {
                                    dialogError = true
                                }
                            } else {
                                if (dialogPassword.isNotBlank()) {
                                    isLocked = true
                                    notePassword = dialogPassword
                                    showPasswordDialog = false
                                }
                            }
                        }
                    ) {
                        Text(strOk)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPasswordDialog = false }) {
                        Text(strCancel)
                    }
                }
            )
        }
    }
}
