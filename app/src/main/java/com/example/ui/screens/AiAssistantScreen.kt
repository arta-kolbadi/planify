package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.i18n.LocalStrings
import com.example.data.ai.AssistantPersona
import com.example.data.ai.ProposedTask
import com.example.data.ai.SuggestedOpenRouterModels
import com.example.data.ai.TaskProposalParser
import com.example.data.local.AppPreferencesState
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ConversationEntity
import androidx.compose.ui.graphics.Color
import com.example.ui.components.LiquidGlassHeroCard
import com.example.ui.components.PlanifyCard
import com.example.ui.components.PlanifyMarkdownView
import com.example.ui.components.liquidGlassBorder
import com.example.ui.components.liquidGlassContainer
import com.example.ui.theme.LocalPlanifyColors

@Composable
fun AiAssistantScreen(
    prefs: AppPreferencesState,
    selectedPersona: AssistantPersona,
    conversations: List<ConversationEntity>,
    activeConversationId: Long?,
    messages: List<ChatMessageEntity>,
    isStreaming: Boolean,
    streamingPreview: String,
    errorMessage: String?,
    pendingImageUri: Uri?,
    dismissedProposalKeys: Set<String>,
    onSelectPersona: (AssistantPersona) -> Unit,
    onSelectConversation: (ConversationEntity) -> Unit,
    onNewChat: () -> Unit,
    onDeleteConversation: (Long) -> Unit,
    onClearChat: () -> Unit,
    onSetPendingImage: (Uri?) -> Unit,
    onSendMessage: (String, String?) -> Unit,
    onStopGeneration: () -> Unit,
    onRegenerate: () -> Unit,
    onApproveTask: (ProposedTask, String) -> Unit,
    onDismissTask: (String) -> Unit,
    onOpenAiSettings: () -> Unit
) {
    val strings = LocalStrings.current
    val extra = LocalPlanifyColors.current
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    var inputText by remember { mutableStateOf("") }
    var overrideModel by remember(selectedPersona, prefs) {
        mutableStateOf(
            prefs.assistantModels[selectedPersona.id]?.takeIf { it.isNotBlank() }
                ?: prefs.openRouterDefaultModel
        )
    }
    var showModelMenu by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) onSetPendingImage(uri)
    }

    val listState = rememberLazyListState()
    LaunchedEffect(messages.size, streamingPreview) {
        if (messages.isNotEmpty() || streamingPreview.isNotEmpty()) {
            listState.animateScrollToItem((messages.size).coerceAtLeast(0))
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Persona Selector Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AssistantPersona.entries.forEach { persona ->
                FilterChip(
                    selected = selectedPersona == persona,
                    onClick = { onSelectPersona(persona) },
                    label = { Text(persona.label(strings)) },
                    modifier = Modifier.testTag("ai_persona_${persona.id}")
                )
            }
        }

        // Toolbar: Model Selector, History, New Chat, Clear
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                AssistChip(
                    onClick = { showModelMenu = true },
                    label = { Text(overrideModel.substringAfterLast("/"), maxLines = 1) },
                    leadingIcon = { Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                DropdownMenu(expanded = showModelMenu, onDismissRequest = { showModelMenu = false }) {
                    SuggestedOpenRouterModels.forEach { model ->
                        DropdownMenuItem(
                            text = { Text(model) },
                            onClick = {
                                overrideModel = model
                                showModelMenu = false
                            }
                        )
                    }
                }
            }
            Row {
                IconButton(onClick = { showHistoryDialog = true }) {
                    Icon(Icons.Rounded.History, contentDescription = strings.aiHistoryTitle)
                }
                IconButton(onClick = onNewChat) {
                    Icon(Icons.Rounded.Add, contentDescription = strings.aiNewChat)
                }
                if (activeConversationId != null) {
                    IconButton(onClick = onClearChat) {
                        Icon(Icons.Rounded.Delete, contentDescription = strings.aiClearChat)
                    }
                }
            }
        }

        // Missing API Key Guide State
        if (prefs.openRouterApiKey.isBlank()) {
            PlanifyCard(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Rounded.Key,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(strings.aiMissingKeyTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        text = strings.aiMissingKeyBody,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = onOpenAiSettings,
                        modifier = Modifier.testTag("ai_open_settings_btn")
                    ) {
                        Text(strings.aiConfigureNow)
                    }
                }
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (messages.isEmpty() && !isStreaming) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = strings.aiQuickPromptsTitle,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        listOf(
                            strings.aiPromptExplain,
                            strings.aiPromptPlanDay,
                            strings.aiPromptQuiz,
                            strings.aiPromptFlashcards,
                            strings.aiPromptSummarizeNotes
                        ).forEach { quick ->
                            val chipShape = RoundedCornerShape(16.dp)
                            Surface(
                                shape = chipShape,
                                color = Color.Transparent,
                                border = liquidGlassBorder(heroAccent = false),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .liquidGlassContainer(shape = chipShape, elevation = 3.dp, heroAccent = false)
                                    .clickable {
                                        if (prefs.openRouterApiKey.isNotBlank()) {
                                            onSendMessage(quick, overrideModel)
                                        } else {
                                            inputText = quick
                                        }
                                    }
                            ) {
                                Text(
                                    text = quick,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            items(messages, key = { it.id }) { msg ->
                val isUser = msg.role == "user"
                val proposals = if (!isUser) TaskProposalParser.extractProposals(msg.content) else emptyList()
                val displayMarkdown = if (!isUser) TaskProposalParser.stripTags(msg.content) else msg.content

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                ) {
                    val bubbleShape = RoundedCornerShape(20.dp)
                    Surface(
                        shape = bubbleShape,
                        color = if (isUser) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f) else Color.Transparent,
                        border = liquidGlassBorder(heroAccent = isUser),
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .let {
                                if (!isUser) it.liquidGlassContainer(shape = bubbleShape, elevation = 4.dp, heroAccent = false)
                                else it
                            }
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (!msg.attachmentSummary.isNullOrBlank()) {
                                Text(
                                    text = "📎 ${msg.attachmentSummary}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            PlanifyMarkdownView(markdown = displayMarkdown)

                            if (!isUser) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    IconButton(
                                        onClick = { clipboard.setText(AnnotatedString(msg.content)) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Rounded.ContentCopy, contentDescription = strings.copy, modifier = Modifier.size(16.dp))
                                    }
                                    if (msg.id == messages.lastOrNull()?.id && !isStreaming) {
                                        IconButton(
                                            onClick = onRegenerate,
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Rounded.Refresh, contentDescription = strings.aiRegenerate, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Confirmation cards for AI-proposed tasks
                    proposals.forEachIndexed { idx, proposal ->
                        val pKey = "${msg.id}_$idx"
                        if (!dismissedProposalKeys.contains(pKey)) {
                            Spacer(modifier = Modifier.height(6.dp))
                            PlanifyCard(modifier = Modifier.fillMaxWidth(0.92f)) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(strings.aiProposedTaskTitle, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                    Text(
                                        text = "${proposal.title} (${proposal.estimatedMinutes}m • ${proposal.priority})",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(onClick = { onApproveTask(proposal, pKey) }) {
                                            Text(strings.aiApproveTask)
                                        }
                                        OutlinedButton(onClick = { onDismissTask(pKey) }) {
                                            Text(strings.aiDismissTask)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (isStreaming) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth(0.92f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                            if (streamingPreview.isNotBlank()) {
                                PlanifyMarkdownView(markdown = TaskProposalParser.stripTags(streamingPreview))
                            }
                        }
                    }
                }
            }

            if (!errorMessage.isNullOrBlank()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = extra.danger.copy(alpha = 0.14f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage,
                            color = extra.danger,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }

        // Pending image badge
        if (pendingImageUri != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🖼 ${pendingImageUri.lastPathSegment ?: "image"}", style = MaterialTheme.typography.labelMedium)
                IconButton(onClick = { onSetPendingImage(null) }, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Rounded.Close, contentDescription = strings.close, modifier = Modifier.size(16.dp))
                }
            }
        }

        // Liquid Glass Composer Dock
        val composerShape = RoundedCornerShape(26.dp)
        Surface(
            shape = composerShape,
            color = Color.Transparent,
            border = liquidGlassBorder(heroAccent = true),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .liquidGlassContainer(shape = composerShape, elevation = 8.dp, heroAccent = true)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                IconButton(
                    onClick = {
                        photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                ) {
                    Icon(Icons.Rounded.AttachFile, contentDescription = strings.aiAttach)
                }
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text(strings.aiInputHint) },
                    maxLines = 4,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.weight(1f).testTag("ai_chat_input")
                )
                if (isStreaming) {
                    IconButton(onClick = onStopGeneration, modifier = Modifier.testTag("ai_stop_btn")) {
                        Icon(Icons.Rounded.Stop, contentDescription = strings.aiStop, tint = extra.danger)
                    }
                } else {
                    IconButton(
                        enabled = inputText.isNotBlank() && prefs.openRouterApiKey.isNotBlank(),
                        onClick = {
                            val prompt = inputText
                            inputText = ""
                            onSendMessage(prompt, overrideModel)
                        },
                        modifier = Modifier.testTag("ai_send_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = strings.aiSend, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }

    if (showHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showHistoryDialog = false },
            title = { Text(strings.aiHistoryTitle) },
            text = {
                if (conversations.isEmpty()) {
                    Text(strings.noResults)
                } else {
                    LazyColumn(modifier = Modifier.height(300.dp)) {
                        items(conversations, key = { it.id }) { conv ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelectConversation(conv)
                                        showHistoryDialog = false
                                    }
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(conv.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                    Text(conv.modelId, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                IconButton(onClick = { onDeleteConversation(conv.id) }) {
                                    Icon(Icons.Rounded.Delete, contentDescription = strings.delete, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showHistoryDialog = false }) { Text(strings.close) }
            }
        )
    }
}
