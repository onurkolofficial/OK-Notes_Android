package com.onurkol.app.notes.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.onurkol.app.notes.R
import com.onurkol.app.notes.data.model.AppLanguage
import com.onurkol.app.notes.data.model.Note
import com.onurkol.app.notes.data.model.ViewMode
import com.onurkol.app.notes.ui.viewmodel.NoteViewModel
import com.onurkol.app.notes.ui.viewmodel.SettingsViewModel
import java.text.SimpleDateFormat
import java.util.*

private val categories = listOf(
    "Genel",
    "İş",
    "Kişisel",
    "Fikir",
    "Önemli"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun NoteListScreen(
    noteViewModel: NoteViewModel,
    settingsViewModel: SettingsViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToNoteDetail: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    val searchQuery by noteViewModel.searchQuery.collectAsStateWithLifecycle()
    val notes by noteViewModel.notesState.collectAsStateWithLifecycle()
    val settings by settingsViewModel.settingsState.collectAsStateWithLifecycle()

    var isSearchActive by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    val lang = settings.appLanguage
    
    // Filter notes dynamically by category selected in UI
    val filteredNotes = remember(notes, selectedCategory) {
        if (selectedCategory == null) {
            notes
        } else {
            notes.filter { it.category == selectedCategory }
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
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        letterSpacing = 0.5.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = stringResource(R.string.settings),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Prominent Add New Note Button directly below header
            Button(
                onClick = { onNavigateToNoteDetail(null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.secondaryContainer
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = stringResource(R.string.add_note),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.add_note),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Animated search input and horizontal category selector row
            AnimatedContent(
                targetState = isSearchActive,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "SearchAndFilterBarTransition"
            ) { active ->
                if (active) {
                    // Full Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { noteViewModel.updateSearchQuery(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        placeholder = { Text(stringResource(R.string.search_placeholder)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = null
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    noteViewModel.updateSearchQuery("")
                                    isSearchActive = false
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Close search"
                                )
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                        )
                    )
                } else {
                    // Category Chips with Fixed Search Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        // Scrollable Category Row
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(end = 56.dp)
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilterChip(
                                selected = selectedCategory == null,
                                onClick = { selectedCategory = null },
                                label = { Text(stringResource(R.string.category_all)) },
                                shape = RoundedCornerShape(12.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )

                            categories.forEach { cat ->
                                val isSelected = selectedCategory == cat
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedCategory = cat },
                                    label = { Text(getCategoryDisplayName(cat)) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }

                        // Fixed Search Button Layer with soft gradient underneath
                        Box(
                            modifier = Modifier
                                .width(72.dp)
                                .fillMaxHeight()
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            MaterialTheme.colorScheme.background.copy(alpha = 0.9f),
                                            MaterialTheme.colorScheme.background
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            FilledIconButton(
                                onClick = { isSearchActive = true },
                                shape = CircleShape,
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Search,
                                    contentDescription = "Search"
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredNotes.isEmpty()) {
                // Empty state view
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Description,
                            contentDescription = null,
                            modifier = Modifier
                                .size(96.dp)
                                .padding(bottom = 16.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                        )
                        Text(
                            text = if (searchQuery.isEmpty()) stringResource(R.string.empty_notes_title) else stringResource(R.string.empty_search_title),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (searchQuery.isEmpty()) stringResource(R.string.empty_notes_desc) else stringResource(R.string.empty_search_desc),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                // Content View
                if (settings.viewMode == ViewMode.GRID) {
                    NoteGrid(
                        notes = filteredNotes,
                        lang = lang,
                        onNoteClick = { onNavigateToNoteDetail(it.id) },
                        onPinClick = { noteViewModel.togglePin(it) },
                        onDeleteClick = { noteViewModel.deleteNote(it) },
                        onReorder = { reorderedList -> noteViewModel.persistNoteOrder(reorderedList) },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    NoteList(
                        notes = filteredNotes,
                        lang = lang,
                        onNoteClick = { onNavigateToNoteDetail(it.id) },
                        onPinClick = { noteViewModel.togglePin(it) },
                        onDeleteClick = { noteViewModel.deleteNote(it) },
                        onReorder = { reorderedList -> noteViewModel.persistNoteOrder(reorderedList) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun NoteGrid(
    notes: List<Note>,
    lang: AppLanguage,
    onNoteClick: (Note) -> Unit,
    onPinClick: (Note) -> Unit,
    onDeleteClick: (Note) -> Unit,
    onReorder: (List<Note>) -> Unit,
    modifier: Modifier = Modifier
) {
    val lazyGridState = rememberLazyGridState()
    var localNotes by remember(notes) { mutableStateOf(notes) }
    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }

    val pinnedNotes = remember(localNotes) { localNotes.filter { it.isPinned } }
    val otherNotes = remember(localNotes) { localNotes.filter { !it.isPinned } }

    LazyVerticalGrid(
        state = lazyGridState,
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .pointerInput(localNotes) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { offset ->
                        val visibleItems = lazyGridState.layoutInfo.visibleItemsInfo
                        val item = visibleItems.firstOrNull { visibleItem ->
                            offset.x.toInt() in visibleItem.offset.x..(visibleItem.offset.x + visibleItem.size.width) &&
                            offset.y.toInt() in visibleItem.offset.y..(visibleItem.offset.y + visibleItem.size.height)
                        }
                        if (item != null) {
                            val noteId = item.key as? Long
                            if (noteId != null) {
                                val idx = localNotes.indexOfFirst { it.id == noteId }
                                if (idx != -1) {
                                    draggedIndex = idx
                                    dragOffset = Offset.Zero
                                }
                            }
                        }
                    },
                    onDrag = { change, dragAmount ->
                        if (draggedIndex != null) {
                            dragOffset += dragAmount
                            
                            val visibleItems = lazyGridState.layoutInfo.visibleItemsInfo
                            val draggedItemInfo = visibleItems.firstOrNull { it.key == localNotes[draggedIndex!!].id }
                            if (draggedItemInfo != null) {
                                val draggedItemCenterX = draggedItemInfo.offset.x + draggedItemInfo.size.width / 2 + dragOffset.x
                                val draggedItemCenterY = draggedItemInfo.offset.y + draggedItemInfo.size.height / 2 + dragOffset.y
                                
                                val crossedItem = visibleItems.firstOrNull { otherItem ->
                                    otherItem.key is Long && otherItem.key != draggedItemInfo.key &&
                                    draggedItemCenterX.toInt() in otherItem.offset.x..(otherItem.offset.x + otherItem.size.width) &&
                                    draggedItemCenterY.toInt() in otherItem.offset.y..(otherItem.offset.y + otherItem.size.height)
                                }
                                
                                if (crossedItem != null) {
                                    val crossedNoteId = crossedItem.key as Long
                                    val targetIndex = localNotes.indexOfFirst { it.id == crossedNoteId }
                                    if (targetIndex != -1) {
                                        val mutableList = localNotes.toMutableList()
                                        // Custom swap implementation
                                        val temp = mutableList[draggedIndex!!]
                                        mutableList[draggedIndex!!] = mutableList[targetIndex]
                                        mutableList[targetIndex] = temp
                                        localNotes = mutableList
                                        
                                        dragOffset = Offset(
                                            x = dragOffset.x - (crossedItem.offset.x - draggedItemInfo.offset.x),
                                            y = dragOffset.y - (crossedItem.offset.y - draggedItemInfo.offset.y)
                                        )
                                        draggedIndex = targetIndex
                                    }
                                }
                            }
                        }
                    },
                    onDragEnd = {
                        if (draggedIndex != null) {
                            onReorder(localNotes)
                            draggedIndex = null
                            dragOffset = Offset.Zero
                        }
                    },
                    onDragCancel = {
                        draggedIndex = null
                        dragOffset = Offset.Zero
                    }
                )
            }
    ) {
        if (pinnedNotes.isNotEmpty()) {
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                SectionHeader(stringResource(R.string.pinned_section))
            }
            items(pinnedNotes, key = { it.id }) { note ->
                val isDraggingThis = draggedIndex != null && localNotes[draggedIndex!!].id == note.id
                NoteCard(
                    note = note,
                    lang = lang,
                    isDraggingThis = isDraggingThis,
                    dragOffset = dragOffset,
                    onClick = { onNoteClick(note) },
                    onPinClick = { onPinClick(note) },
                    onDeleteClick = { onDeleteClick(note) }
                )
            }
        }

        if (otherNotes.isNotEmpty()) {
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                SectionHeader(if (pinnedNotes.isNotEmpty()) stringResource(R.string.other_section) else stringResource(R.string.app_name))
            }
            items(otherNotes, key = { it.id }) { note ->
                val isDraggingThis = draggedIndex != null && localNotes[draggedIndex!!].id == note.id
                NoteCard(
                    note = note,
                    lang = lang,
                    isDraggingThis = isDraggingThis,
                    dragOffset = dragOffset,
                    onClick = { onNoteClick(note) },
                    onPinClick = { onPinClick(note) },
                    onDeleteClick = { onDeleteClick(note) }
                )
            }
        }
    }
}

@Composable
fun NoteList(
    notes: List<Note>,
    lang: AppLanguage,
    onNoteClick: (Note) -> Unit,
    onPinClick: (Note) -> Unit,
    onDeleteClick: (Note) -> Unit,
    onReorder: (List<Note>) -> Unit,
    modifier: Modifier = Modifier
) {
    val lazyListState = rememberLazyListState()
    var localNotes by remember(notes) { mutableStateOf(notes) }
    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }

    val pinnedNotes = remember(localNotes) { localNotes.filter { it.isPinned } }
    val otherNotes = remember(localNotes) { localNotes.filter { !it.isPinned } }

    LazyColumn(
        state = lazyListState,
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .pointerInput(localNotes) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { offset ->
                        val visibleItems = lazyListState.layoutInfo.visibleItemsInfo
                        val item = visibleItems.firstOrNull { visibleItem ->
                            offset.y.toInt() in visibleItem.offset..(visibleItem.offset + visibleItem.size)
                        }
                        if (item != null) {
                            val noteId = item.key as? Long
                            if (noteId != null) {
                                val idx = localNotes.indexOfFirst { it.id == noteId }
                                if (idx != -1) {
                                    draggedIndex = idx
                                    dragOffset = Offset.Zero
                                }
                            }
                        }
                    },
                    onDrag = { change, dragAmount ->
                        if (draggedIndex != null) {
                            dragOffset += dragAmount
                            
                            val visibleItems = lazyListState.layoutInfo.visibleItemsInfo
                            val draggedItemInfo = visibleItems.firstOrNull { it.key == localNotes[draggedIndex!!].id }
                            if (draggedItemInfo != null) {
                                val draggedItemCenterY = draggedItemInfo.offset + draggedItemInfo.size / 2 + dragOffset.y
                                
                                val crossedItem = visibleItems.firstOrNull { otherItem ->
                                    otherItem.key is Long && otherItem.key != draggedItemInfo.key &&
                                    draggedItemCenterY.toInt() in otherItem.offset..(otherItem.offset + otherItem.size)
                                }
                                
                                if (crossedItem != null) {
                                    val crossedNoteId = crossedItem.key as Long
                                    val targetIndex = localNotes.indexOfFirst { it.id == crossedNoteId }
                                    if (targetIndex != -1) {
                                        val mutableList = localNotes.toMutableList()
                                        // Custom swap implementation
                                        val temp = mutableList[draggedIndex!!]
                                        mutableList[draggedIndex!!] = mutableList[targetIndex]
                                        mutableList[targetIndex] = temp
                                        localNotes = mutableList
                                        
                                        dragOffset = Offset(
                                            x = 0f, // list reorder is vertical only
                                            y = dragOffset.y - (crossedItem.offset - draggedItemInfo.offset)
                                        )
                                        draggedIndex = targetIndex
                                    }
                                }
                            }
                        }
                    },
                    onDragEnd = {
                        if (draggedIndex != null) {
                            onReorder(localNotes)
                            draggedIndex = null
                            dragOffset = Offset.Zero
                        }
                    },
                    onDragCancel = {
                        draggedIndex = null
                        dragOffset = Offset.Zero
                    }
                )
            }
    ) {
        if (pinnedNotes.isNotEmpty()) {
            item {
                SectionHeader(stringResource(R.string.pinned_section))
            }
            items(pinnedNotes, key = { it.id }) { note ->
                val isDraggingThis = draggedIndex != null && localNotes[draggedIndex!!].id == note.id
                NoteCard(
                    note = note,
                    lang = lang,
                    isDraggingThis = isDraggingThis,
                    dragOffset = dragOffset,
                    onClick = { onNoteClick(note) },
                    onPinClick = { onPinClick(note) },
                    onDeleteClick = { onDeleteClick(note) }
                )
            }
        }

        if (otherNotes.isNotEmpty()) {
            item {
                SectionHeader(if (pinnedNotes.isNotEmpty()) stringResource(R.string.other_section) else stringResource(R.string.app_name))
            }
            items(otherNotes, key = { it.id }) { note ->
                val isDraggingThis = draggedIndex != null && localNotes[draggedIndex!!].id == note.id
                NoteCard(
                    note = note,
                    lang = lang,
                    isDraggingThis = isDraggingThis,
                    dragOffset = dragOffset,
                    onClick = { onNoteClick(note) },
                    onPinClick = { onPinClick(note) },
                    onDeleteClick = { onDeleteClick(note) }
                )
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteCard(
    note: Note,
    lang: AppLanguage,
    isDraggingThis: Boolean = false,
    dragOffset: Offset = Offset.Zero,
    onClick: () -> Unit,
    onPinClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val noteBgColor = if (note.colorHex == 0xFFFFFFFF) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    } else {
        val color = Color(note.colorHex)
        if (isDark) color.copy(alpha = 0.2f) else color.copy(alpha = 0.35f)
    }

    val pattern = stringResource(R.string.date_format)
    val formattedDate = remember(note.timestamp, lang, pattern) {
        val locale = if (lang == AppLanguage.TURKISH) Locale.forLanguageTag("tr-TR") else Locale.ENGLISH
        val sdf = SimpleDateFormat(pattern, locale)
        sdf.format(Date(note.timestamp))
    }

    val catGenel = stringResource(R.string.category_general)
    val catIs = stringResource(R.string.category_work)
    val catKisisel = stringResource(R.string.category_personal)
    val catFikir = stringResource(R.string.category_idea)
    val catOnemli = stringResource(R.string.category_important)

    val displayCategory = remember(note.category, catGenel, catIs, catKisisel, catFikir, catOnemli) {
        when (note.category) {
            "Genel" -> catGenel
            "İş" -> catIs
            "Kişisel" -> catKisisel
            "Fikir" -> catFikir
            "Önemli" -> catOnemli
            else -> note.category
        }
    }

    val translationX = if (isDraggingThis) dragOffset.x else 0f
    val translationY = if (isDraggingThis) dragOffset.y else 0f
    val scale = if (isDraggingThis) 1.06f else 1f
    val elevation = if (isDraggingThis) 12.dp else 0.dp
    val alpha = if (isDraggingThis) 0.9f else 1f

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = noteBgColor
        ),
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                this.translationX = translationX
                this.translationY = translationY
                this.scaleX = scale
                this.scaleY = scale
                this.alpha = alpha
            }
            .shadow(elevation, shape = RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onPinClick
            )
            .border(
                width = if (note.isPinned) 1.5.dp else 0.dp,
                color = if (note.isPinned) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Category Badge
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = displayCategory,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onPinClick,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PushPin,
                            contentDescription = stringResource(R.string.pin),
                            modifier = Modifier.size(16.dp),
                            tint = if (note.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = stringResource(R.string.delete),
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = note.title.ifEmpty { stringResource(R.string.untitled_note) },
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = note.content.ifEmpty { stringResource(R.string.no_content) },
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = formattedDate,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}
