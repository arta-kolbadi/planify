package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.core.i18n.JalaliCalendar
import com.example.core.i18n.LocalAppLanguage
import com.example.core.i18n.LocalCalendarSystem
import com.example.core.i18n.LocalStrings
import com.example.data.local.NoteEntity
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.PlanifyCard
import com.example.ui.components.PlanifyMarkdownView
import com.example.ui.theme.LocalPlanifyColors
import kotlinx.coroutines.delay

@Composable
fun NotesScreen(
    notes: List<NoteEntity>,
    onSaveNote: (NoteEntity, ((Long) -> Unit)?) -> Unit,
    onDeleteNote: (NoteEntity) -> Unit
) {
    val strings = LocalStrings.current
    val lang = LocalAppLanguage.current
    val calSystem = LocalCalendarSystem.current
    val extra = LocalPlanifyColors.current

    var tabIndex by remember { mutableIntStateOf(0) } // 0=Notes, 1=Journal
    var searchQuery by remember { mutableStateOf("") }
    var filterPinned by remember { mutableStateOf(false) }
    var filterFav by remember { mutableStateOf(false) }
    var editingNote by remember { mutableStateOf<NoteEntity?>(null) }

    if (editingNote != null) {
        NoteEditorPane(
            initial = editingNote!!,
            onSave = onSaveNote,
            onBack = { editingNote = null }
        )
        return
    }

    val filtered = remember(notes, tabIndex, searchQuery, filterPinned, filterFav) {
        notes.filter { n ->
            n.isJournal == (tabIndex == 1) &&
                (!filterPinned || n.isPinned) &&
                (!filterFav || n.isFavorite) &&
                (searchQuery.isBlank() ||
                    n.title.contains(searchQuery, ignoreCase = true) ||
                    n.content.contains(searchQuery, ignoreCase = true) ||
                    n.tagsCsv.contains(searchQuery, ignoreCase = true))
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val today = JalaliCalendar.todayEpochDay()
                    editingNote = NoteEntity(
                        title = "",
                        content = "",
                        isJournal = tabIndex == 1,
                        journalDateEpochDay = if (tabIndex == 1) today else 0L
                    )
                },
                modifier = Modifier.testTag("notes_add_fab")
            ) {
                Icon(Icons.Rounded.Add, contentDescription = strings.notesNewNote)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(
                selectedTabIndex = tabIndex,
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.68f)
            ) {
                Tab(selected = tabIndex == 0, onClick = { tabIndex = 0 }, text = { Text(strings.notesTabNotes) })
                Tab(selected = tabIndex == 1, onClick = { tabIndex = 1 }, text = { Text(strings.notesTabJournal) })
            }

            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = strings.search) },
                    placeholder = { Text(strings.search) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = filterPinned,
                        onClick = { filterPinned = !filterPinned },
                        label = { Text(strings.notesPinned) }
                    )
                    FilterChip(
                        selected = filterFav,
                        onClick = { filterFav = !filterFav },
                        label = { Text(strings.notesFavorites) }
                    )
                }
            }

            if (filtered.isEmpty()) {
                EmptyStateCard(
                    icon = Icons.Rounded.EditNote,
                    title = strings.notesEmpty,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 92.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filtered, key = { it.id }) { note ->
                        PlanifyCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { editingNote = note }
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = note.title.ifBlank { strings.notesTitleHint },
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (note.isJournal && note.journalDateEpochDay > 0L) {
                                            Text(
                                                text = JalaliCalendar.formatDate(note.journalDateEpochDay, lang, calSystem),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    Row {
                                        IconButton(onClick = { onSaveNote(note.copy(isPinned = !note.isPinned), null) }) {
                                            Icon(
                                                Icons.Rounded.PushPin,
                                                contentDescription = strings.notesPinned,
                                                tint = if (note.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        IconButton(onClick = { onSaveNote(note.copy(isFavorite = !note.isFavorite), null) }) {
                                            Icon(
                                                imageVector = if (note.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                                contentDescription = strings.notesFavorites,
                                                tint = if (note.isFavorite) extra.danger else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        IconButton(onClick = { onDeleteNote(note) }) {
                                            Icon(Icons.Rounded.Delete, contentDescription = strings.delete, tint = extra.danger, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                                if (note.content.isNotBlank()) {
                                    Text(
                                        text = note.content.take(160),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (note.tagsCsv.isNotBlank()) {
                                    Text(
                                        text = note.tagsCsv.split(",").filter { it.isNotBlank() }.joinToString("  ") { "#${it.trim()}" },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
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

@Composable
private fun NoteEditorPane(
    initial: NoteEntity,
    onSave: (NoteEntity, ((Long) -> Unit)?) -> Unit,
    onBack: () -> Unit
) {
    val strings = LocalStrings.current
    var noteId by remember { mutableStateOf(initial.id) }
    var title by remember { mutableStateOf(initial.title) }
    var content by remember { mutableStateOf(initial.content) }
    var category by remember { mutableStateOf(initial.category) }
    var tags by remember { mutableStateOf(initial.tagsCsv) }
    var attachmentUri by remember { mutableStateOf(initial.attachmentUri) }
    var previewMode by remember { mutableStateOf(false) }
    var autoSaveStamp by remember { mutableStateOf("") }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) attachmentUri = uri.toString()
    }

    val handleBack = {
        if (title.isNotBlank() || content.isNotBlank()) {
            onSave(
                initial.copy(
                    id = noteId,
                    title = title.ifBlank { strings.notesNewNote },
                    content = content,
                    category = category,
                    tagsCsv = tags,
                    attachmentUri = attachmentUri
                ),
                null
            )
        }
        onBack()
    }

    BackHandler(onBack = handleBack)

    // Auto-save debounce
    LaunchedEffect(title, content, category, tags, attachmentUri) {
        if (title.isNotBlank() || content.isNotBlank()) {
            delay(900L)
            onSave(
                initial.copy(
                    id = noteId,
                    title = title,
                    content = content,
                    category = category,
                    tagsCsv = tags,
                    attachmentUri = attachmentUri
                )
            ) { savedId ->
                if (noteId == 0L) noteId = savedId
                autoSaveStamp = strings.notesAutoSaved
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = handleBack) {
                Text("← ${strings.back}")
            }
            if (autoSaveStamp.isNotBlank()) {
                Text(autoSaveStamp, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
            Row {
                IconButton(onClick = {
                    picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) {
                    Icon(Icons.Rounded.AttachFile, contentDescription = strings.notesAttachPhoto)
                }
                IconButton(onClick = { previewMode = !previewMode }) {
                    Icon(Icons.Rounded.Visibility, contentDescription = strings.notesPreviewMode)
                }
            }
        }

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            placeholder = { Text(strings.notesTitleHint) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("note_title_input")
        )

        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("# " to "H1", "## " to "H2", "**bold**" to "B", "- " to "List", "\n```\ncode\n```\n" to "Code").forEach { (snippet, label) ->
                AssistChip(
                    onClick = { content += snippet },
                    label = { Text(label) }
                )
            }
        }

        if (!attachmentUri.isNullOrBlank()) {
            AsyncImage(
                model = Uri.parse(attachmentUri),
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
        }

        if (previewMode) {
            PlanifyCard(modifier = Modifier.weight(1f).fillMaxWidth()) {
                LazyColumn(contentPadding = PaddingValues(14.dp)) {
                    item { PlanifyMarkdownView(markdown = content) }
                }
            }
        } else {
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                placeholder = { Text(strings.notesBodyHint) },
                modifier = Modifier.weight(1f).fillMaxWidth().testTag("note_body_input")
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text(strings.notesCategoryLabel) },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = tags,
                onValueChange = { tags = it },
                label = { Text(strings.notesTagsHint) },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
