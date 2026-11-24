package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.PlanifyApplication
import com.example.core.i18n.JalaliCalendar
import com.example.data.ai.AssistantPersona
import com.example.data.ai.OpenRouterMessage
import com.example.data.ai.ProposedTask
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ConversationEntity
import com.example.data.local.TaskEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalCoroutinesApi::class)
class AiViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as PlanifyApplication
    private val repo = app.repository
    private val prefsRepo = app.preferencesRepository
    private val client = app.openRouterClient

    private val _selectedPersona = MutableStateFlow(AssistantPersona.GENERAL)
    val selectedPersona: StateFlow<AssistantPersona> = _selectedPersona.asStateFlow()

    private val _activeConversationId = MutableStateFlow<Long?>(null)
    val activeConversationId: StateFlow<Long?> = _activeConversationId.asStateFlow()

    private val _isStreaming = MutableStateFlow(false)
    val isStreaming: StateFlow<Boolean> = _isStreaming.asStateFlow()

    private val _streamingPreview = MutableStateFlow("")
    val streamingPreview: StateFlow<String> = _streamingPreview.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _pendingImageUri = MutableStateFlow<Uri?>(null)
    val pendingImageUri: StateFlow<Uri?> = _pendingImageUri.asStateFlow()

    private val _dismissedProposalKeys = MutableStateFlow<Set<String>>(emptySet())
    val dismissedProposalKeys: StateFlow<Set<String>> = _dismissedProposalKeys.asStateFlow()

    val conversations: StateFlow<List<ConversationEntity>> = repo.conversationsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val messages: StateFlow<List<ChatMessageEntity>> = _activeConversationId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repo.observeMessages(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var streamJob: Job? = null

    fun selectPersona(persona: AssistantPersona) {
        _selectedPersona.value = persona
        val matching = conversations.value.firstOrNull { it.assistantType == persona.id }
        _activeConversationId.value = matching?.id
        _errorMessage.value = null
    }

    fun selectConversation(conv: ConversationEntity) {
        _selectedPersona.value = AssistantPersona.entries.firstOrNull { it.id == conv.assistantType }
            ?: AssistantPersona.GENERAL
        _activeConversationId.value = conv.id
        _errorMessage.value = null
    }

    fun startNewConversation() {
        stopGeneration()
        _activeConversationId.value = null
        _errorMessage.value = null
    }

    fun setPendingImage(uri: Uri?) {
        _pendingImageUri.value = uri
    }

    fun sendMessage(context: Context, promptText: String, overrideModel: String? = null) {
        val cleanPrompt = promptText.trim()
        if (cleanPrompt.isEmpty() || _isStreaming.value) return

        val attachedUri = _pendingImageUri.value
        _pendingImageUri.value = null
        _errorMessage.value = null

        streamJob = viewModelScope.launch {
            val prefs = prefsRepo.getPreferencesOnce()
            val persona = _selectedPersona.value
            val model = overrideModel?.takeIf { it.isNotBlank() }
                ?: prefs.assistantModels[persona.id]?.takeIf { it.isNotBlank() }
                ?: prefs.openRouterDefaultModel

            val convId = _activeConversationId.value ?: run {
                val created = repo.createConversation(
                    assistantType = persona.id,
                    title = cleanPrompt.take(40),
                    modelId = model
                )
                _activeConversationId.value = created
                created
            }

            val attachLabel = attachedUri?.lastPathSegment
            repo.addChatMessage(
                conversationId = convId,
                role = "user",
                content = cleanPrompt,
                modelUsed = model,
                attachmentSummary = attachLabel
            )

            val encodedImage = attachedUri?.let { encodeImageAsDataUrl(context, it) }
            runStreamingRequest(
                convId = convId,
                model = model,
                persona = persona,
                latestImageDataUrl = encodedImage
            )
        }
    }

    fun regenerateLast() {
        val convId = _activeConversationId.value ?: return
        if (_isStreaming.value) return
        streamJob = viewModelScope.launch {
            val history = repo.getMessagesOnce(convId)
            val last = history.lastOrNull() ?: return@launch
            if (last.role == "assistant") {
                repo.deleteChatMessage(last.id)
            }
            val prefs = prefsRepo.getPreferencesOnce()
            val persona = _selectedPersona.value
            val model = prefs.assistantModels[persona.id]?.takeIf { it.isNotBlank() }
                ?: prefs.openRouterDefaultModel
            runStreamingRequest(convId, model, persona, null)
        }
    }

    fun stopGeneration() {
        streamJob?.cancel()
        streamJob = null
        val partial = _streamingPreview.value.trim()
        val convId = _activeConversationId.value
        if (_isStreaming.value && partial.isNotEmpty() && convId != null) {
            viewModelScope.launch {
                val prefs = prefsRepo.getPreferencesOnce()
                repo.addChatMessage(convId, "assistant", partial, prefs.openRouterDefaultModel)
            }
        }
        _isStreaming.value = false
        _streamingPreview.value = ""
    }

    fun clearCurrentConversation() {
        val convId = _activeConversationId.value ?: return
        stopGeneration()
        viewModelScope.launch {
            repo.clearConversationMessages(convId)
        }
    }

    fun deleteConversation(id: Long) {
        if (_activeConversationId.value == id) {
            _activeConversationId.value = null
        }
        viewModelScope.launch {
            repo.deleteConversation(id)
        }
    }

    fun approveProposedTask(proposal: ProposedTask, key: String) {
        _dismissedProposalKeys.value = _dismissedProposalKeys.value + key
        viewModelScope.launch {
            repo.saveTask(
                TaskEntity(
                    title = proposal.title,
                    dateEpochDay = JalaliCalendar.todayEpochDay(),
                    priority = proposal.priority,
                    categoryName = proposal.categoryName,
                    estimatedMinutes = proposal.estimatedMinutes
                )
            )
        }
    }

    fun dismissProposedTask(key: String) {
        _dismissedProposalKeys.value = _dismissedProposalKeys.value + key
    }

    private suspend fun runStreamingRequest(
        convId: Long,
        model: String,
        persona: AssistantPersona,
        latestImageDataUrl: String?
    ) {
        val prefs = prefsRepo.getPreferencesOnce()
        val history = repo.getMessagesOnce(convId)
        val systemText = buildString {
            append(persona.defaultSystemPrompt())
            if (prefs.aiSystemPrompt.isNotBlank()) {
                append("\n\nUser Preferences:\n")
                append(prefs.aiSystemPrompt)
            }
        }

        val apiMessages = buildList {
            add(OpenRouterMessage(role = "system", content = systemText))
            history.forEachIndexed { idx, msg ->
                val isLatestUser = idx == history.lastIndex && msg.role == "user"
                add(
                    OpenRouterMessage(
                        role = msg.role,
                        content = msg.content,
                        imageBase64DataUrl = if (isLatestUser) latestImageDataUrl else null
                    )
                )
            }
        }

        _isStreaming.value = true
        _streamingPreview.value = ""
        val sb = StringBuilder()

        try {
            withTimeout(90_000L) {
                client.streamChatCompletion(
                    apiKey = prefs.openRouterApiKey,
                    baseUrl = prefs.openRouterBaseUrl,
                    model = model,
                    messages = apiMessages,
                    temperature = prefs.aiTemperature,
                    maxTokens = prefs.aiMaxTokens
                ).collect { delta ->
                    sb.append(delta)
                    _streamingPreview.value = sb.toString()
                }
            }
            val finalOutput = sb.toString().trim()
            if (finalOutput.isNotEmpty()) {
                repo.addChatMessage(
                    conversationId = convId,
                    role = "assistant",
                    content = finalOutput,
                    modelUsed = model
                )
            } else {
                _errorMessage.value = "Empty response received from model."
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _errorMessage.value = e.localizedMessage ?: "OpenRouter request failed."
        } finally {
            _isStreaming.value = false
            _streamingPreview.value = ""
        }
    }

    private suspend fun encodeImageAsDataUrl(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        runCatching {
            val rawBytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return@withContext null
            val bitmap = BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size) ?: return@withContext null
            val maxDim = 1024
            val scale = minOf(1f, maxDim.toFloat() / maxOf(bitmap.width, bitmap.height))
            val scaled = if (scale < 1f) {
                Bitmap.createScaledBitmap(
                    bitmap,
                    (bitmap.width * scale).toInt().coerceAtLeast(1),
                    (bitmap.height * scale).toInt().coerceAtLeast(1),
                    true
                )
            } else bitmap
            val out = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, 80, out)
            val b64 = Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
            "data:image/jpeg;base64,$b64"
        }.getOrNull()
    }
}
