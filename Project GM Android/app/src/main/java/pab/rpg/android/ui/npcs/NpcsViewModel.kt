package pab.rpg.android.ui.npcs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pab.rpg.android.data.CombatRepository
import pab.rpg.android.data.NpcRepository
import pab.rpg.android.network.ApiException
import pab.rpg.android.network.dto.NpcResponse

data class NpcsUiState(
    val isLoading: Boolean = false,
    val npcs: List<NpcResponse> = emptyList(),
    val errorMessage: String? = null,
    val busyNpcId: String? = null,
    val actionError: String? = null,
    val combatSessionReady: Boolean = false
)

class NpcsViewModel(
    private val sessionId: String,
    private val repository: NpcRepository = NpcRepository(),
    private val combatRepository: CombatRepository = CombatRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(NpcsUiState())
    val uiState: StateFlow<NpcsUiState> = _uiState

    init {
        load()
    }

    fun retry() = load()

    fun consumeActionError() {
        _uiState.update { it.copy(actionError = null) }
    }

    fun consumeCombatNavigation() {
        _uiState.update { it.copy(combatSessionReady = false) }
    }

    fun attackNpc(npcId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(busyNpcId = npcId, actionError = null) }
            combatRepository.startCombat(sessionId, npcId)
                .onSuccess { _uiState.update { it.copy(busyNpcId = null, combatSessionReady = true) } }
                .onFailure { error -> handleAttackFailure(error) }
        }
    }

    // COMBAT_NOT_ALLOWED can mean "there's already an active combat" (e.g. against another NPC) — in that
    // case just navigate to it instead of surfacing an error the player can't act on.
    private suspend fun handleAttackFailure(error: Throwable) {
        if (error is ApiException && error.apiError.code == "COMBAT_NOT_ALLOWED") {
            combatRepository.getActiveCombat(sessionId)
                .onSuccess { _uiState.update { it.copy(busyNpcId = null, combatSessionReady = true) } }
                .onFailure { _uiState.update { it.copy(busyNpcId = null, actionError = error.toUserMessage()) } }
        } else {
            _uiState.update { it.copy(busyNpcId = null, actionError = error.toUserMessage()) }
        }
    }

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
