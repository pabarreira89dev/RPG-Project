package pab.rpg.android.ui.narration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pab.rpg.android.data.ActionRepository
import pab.rpg.android.data.ChatHistoryRepository
import pab.rpg.android.data.SessionRepository
import pab.rpg.android.data.local.ChatMessageEntity
import pab.rpg.android.network.ApiException
import pab.rpg.android.network.dto.SessionResponse
import pab.rpg.android.network.dto.SubmitActionRequest

const val ROLE_PLAYER = "PLAYER"
const val ROLE_NARRATOR = "NARRATOR"
const val ROLE_SYSTEM = "SYSTEM"

private data class PendingAction(val text: String, val idempotencyKey: String)

data class NarrationUiState(
    val isLoadingSession: Boolean = false,
    val session: SessionResponse? = null,
    val messages: List<ChatMessageEntity> = emptyList(),
    val inputText: String = "",
    val isSending: Boolean = false,
    val errorMessage: String? = null,
    val canRetry: Boolean = false,
    val isDeleted: Boolean = false
)

class NarrationViewModel(
    private val sessionId: String,
    private val sessionRepository: SessionRepository = SessionRepository(),
    private val actionRepository: ActionRepository = ActionRepository(),
    private val chatHistoryRepository: ChatHistoryRepository = ChatHistoryRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(NarrationUiState())
    val uiState: StateFlow<NarrationUiState> = _uiState

    // Tracks the session's optimistic-lock version and the one in-flight/failed action, if any.
    private var expectedVersion: Long = 0
    private var pendingAction: PendingAction? = null

    init {
        loadSession()
        viewModelScope.launch {
            chatHistoryRepository.observeMessages(sessionId).collect { messages ->
                _uiState.update { it.copy(messages = messages) }
            }
        }
    }

    fun loadSession() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingSession = true) }
            sessionRepository.getSession(sessionId)
                .onSuccess { session ->
                    expectedVersion = session.version
                    _uiState.update { it.copy(isLoadingSession = false, session = session) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoadingSession = false, errorMessage = error.toUserMessage()) }
                }
        }
    }

    fun onInputChange(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun send() {
        val text = _uiState.value.inputText.trim()
        if (text.isBlank() || _uiState.value.isSending) return

        val action = PendingAction(text, UUID.randomUUID().toString())
        pendingAction = action
        _uiState.update { it.copy(inputText = "", isSending = true, errorMessage = null, canRetry = false) }

        viewModelScope.launch {
            chatHistoryRepository.addMessage(sessionId, ROLE_PLAYER, action.text)
            submit(action)
        }
    }

    fun retry() {
        val action = pendingAction ?: return
        if (_uiState.value.isSending) return
        _uiState.update { it.copy(isSending = true, errorMessage = null, canRetry = false) }
        viewModelScope.launch { submit(action) }
    }

    private suspend fun submit(action: PendingAction) {
        val request = SubmitActionRequest(
            text = action.text,
            expectedVersion = expectedVersion,
            idempotencyKey = action.idempotencyKey
        )
        actionRepository.submitAction(sessionId, request)
            .onSuccess { response ->
                expectedVersion = response.stateVersion
                pendingAction = null
                chatHistoryRepository.addMessage(sessionId, ROLE_NARRATOR, response.narration)
                _uiState.update { it.copy(isSending = false) }
            }
            .onFailure { error -> handleFailure(error) }
    }

    private suspend fun handleFailure(error: Throwable) {
        when ((error as? ApiException)?.apiError?.code) {
            "STALE_SESSION_VERSION" -> {
                // Nothing committed server-side: resync the version and let the player retry the same text.
                sessionRepository.getSession(sessionId).onSuccess { session -> expectedVersion = session.version }
                _uiState.update { it.copy(isSending = false, errorMessage = error.toUserMessage(), canRetry = true) }
            }
            "ACTION_NOT_ALLOWED" -> {
                pendingAction = null
                chatHistoryRepository.addMessage(sessionId, ROLE_SYSTEM, error.toUserMessage())
                _uiState.update { it.copy(isSending = false) }
            }
            "OPENAI_UNAVAILABLE" -> {
                // The action already resolved server-side without narration; nothing to retry, just resync.
                pendingAction = null
                sessionRepository.getSession(sessionId).onSuccess { session -> expectedVersion = session.version }
                chatHistoryRepository.addMessage(
                    sessionId,
                    ROLE_SYSTEM,
                    "La IA no pudo narrar esta vez, pero la acción se resolvió."
                )
                _uiState.update { it.copy(isSending = false) }
            }
            else -> {
                _uiState.update { it.copy(isSending = false, errorMessage = error.toUserMessage(), canRetry = true) }
            }
        }
    }

    fun deleteSession() {
        viewModelScope.launch {
            sessionRepository.deleteSession(sessionId)
                .onSuccess { _uiState.update { it.copy(isDeleted = true) } }
                .onFailure { error -> _uiState.update { it.copy(errorMessage = error.toUserMessage()) } }
        }
    }

    private fun Throwable.toUserMessage(): String = when (this) {
        is ApiException -> apiError.message
        else -> message ?: "No se pudo conectar con el servidor"
    }
}
