package pab.rpg.android.ui.npcs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pab.rpg.android.data.NpcRepository
import pab.rpg.android.network.ApiException
import pab.rpg.android.network.dto.NpcResponse

data class NpcsUiState(
    val isLoading: Boolean = false,
    val npcs: List<NpcResponse> = emptyList(),
    val errorMessage: String? = null
)

class NpcsViewModel(
    private val sessionId: String,
    private val repository: NpcRepository = NpcRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(NpcsUiState())
    val uiState: StateFlow<NpcsUiState> = _uiState

    init {
        load()
    }

    fun retry() = load()

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.getNpcs(sessionId)
                .onSuccess { npcs -> _uiState.update { it.copy(isLoading = false, npcs = npcs) } }
                .onFailure { error -> _uiState.update { it.copy(isLoading = false, errorMessage = error.toUserMessage()) } }
        }
    }

    private fun Throwable.toUserMessage(): String = when (this) {
        is ApiException -> apiError.message
        else -> message ?: "No se pudo conectar con el servidor"
    }
}
