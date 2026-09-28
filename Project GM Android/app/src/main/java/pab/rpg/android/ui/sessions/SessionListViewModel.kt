package pab.rpg.android.ui.sessions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pab.rpg.android.data.SessionRepository
import pab.rpg.android.network.ApiException
import pab.rpg.android.network.dto.CreateSessionRequest
import pab.rpg.android.network.dto.SessionResponse
import retrofit2.HttpException

data class SessionListUiState(
    val isLoading: Boolean = false,
    val sessions: List<SessionResponse> = emptyList(),
    val errorMessage: String? = null,
    val isSessionExpired: Boolean = false
)

class SessionListViewModel(
    private val repository: SessionRepository = SessionRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SessionListUiState())
    val uiState: StateFlow<SessionListUiState> = _uiState

    init {
        loadSessions()
    }

    fun loadSessions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, isSessionExpired = false) }
            repository.listSessions()
                .onSuccess { sessions -> _uiState.update { it.copy(isLoading = false, sessions = sessions) } }
                .onFailure { error -> applyError(error) }
        }
    }

    fun createSession(characterName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, isSessionExpired = false) }
            repository.createSession(CreateSessionRequest.newCharacter(characterName = characterName))
                .onSuccess { loadSessions() }
                .onFailure { error -> applyError(error) }
        }
    }

    private fun applyError(error: Throwable) {
        if (error.isSessionExpired()) {
            _uiState.update { it.copy(isLoading = false, isSessionExpired = true) }
        } else {
            _uiState.update { it.copy(isLoading = false, errorMessage = error.toUserMessage()) }
        }
    }

    // 401s never carry the uniform ApiError body (see ApiError.kt), so an expired/invalid token
    // surfaces as a raw HttpException rather than an ApiException.
    private fun Throwable.isSessionExpired(): Boolean = this is HttpException && code() == 401

    private fun Throwable.toUserMessage(): String = when (this) {
        is ApiException -> apiError.message
        else -> message ?: "No se pudo conectar con el servidor"
    }
}
