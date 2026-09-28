package pab.rpg.android.ui.combat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pab.rpg.android.data.CombatRepository
import pab.rpg.android.network.ApiException
import pab.rpg.android.network.dto.CombatResponse
import pab.rpg.android.network.dto.PerformAttackRequest

data class CombatUiState(
    val isLoading: Boolean = false,
    val combat: CombatResponse? = null,
    val noActiveCombat: Boolean = false,
    val errorMessage: String? = null,
    val actionError: String? = null,
    val selectedTargetId: String? = null,
    val freeText: String = "",
    val isSubmittingAttack: Boolean = false,
    val isEnemyTurnProcessing: Boolean = false
)

class CombatViewModel(
    private val sessionId: String,
    private val repository: CombatRepository = CombatRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CombatUiState())
    val uiState: StateFlow<CombatUiState> = _uiState

    init {
        load()
    }

    fun retry() = load()

    fun consumeActionError() {
        _uiState.update { it.copy(actionError = null) }
    }

    fun onFreeTextChange(text: String) {
        _uiState.update { it.copy(freeText = text) }
    }

    fun selectTarget(participantId: String) {
        _uiState.update {
            it.copy(selectedTargetId = if (it.selectedTargetId == participantId) null else participantId)
        }
    }

    fun attackSelectedTarget() {
        val state = _uiState.value
        val combat = state.combat ?: return
        val targetId = state.selectedTargetId ?: return
        val attackerId = combat.participants.firstOrNull { it.team == "PLAYER" }?.id ?: return
        submitAttack(combat.combatId, PerformAttackRequest(attackerParticipantId = attackerId, targetParticipantId = targetId))
    }

    fun attackWithFreeText() {
        val state = _uiState.value
        val combat = state.combat ?: return
        val text = state.freeText
        if (text.isBlank()) return
        submitAttack(combat.combatId, PerformAttackRequest(text = text))
    }

    private fun submitAttack(combatId: String, request: PerformAttackRequest) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingAttack = true, actionError = null) }
            repository.attack(sessionId, combatId, request)
                .onSuccess { response ->
                    _uiState.update {
                        it.copy(isSubmittingAttack = false, combat = response, selectedTargetId = null, freeText = "")
                    }
                    runEnemyTurnLoop()
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isSubmittingAttack = false, actionError = error.toUserMessage()) }
                }
        }
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, noActiveCombat = false) }
            repository.getActiveCombat(sessionId)
                .onSuccess { response ->
                    _uiState.update { it.copy(isLoading = false, combat = response) }
                    runEnemyTurnLoop()
                }
                .onFailure { error ->
                    if (error is ApiException && error.apiError.code == "COMBAT_NOT_FOUND") {
                        _uiState.update { it.copy(isLoading = false, noActiveCombat = true) }
                    } else {
                        _uiState.update { it.copy(isLoading = false, errorMessage = error.toUserMessage()) }
                    }
                }
        }
    }

    // No server-side enemy AI: the app resolves ENEMY turns itself, one attack at a time, until it's
    // the player's turn again or the combat ends. Capped as a safety net against unexpected backend states.
    private fun runEnemyTurnLoop() {
        viewModelScope.launch {
            repeat(ENEMY_TURN_SAFETY_LIMIT) {
                val combat = _uiState.value.combat ?: return@launch
                if (combat.status != "ACTIVE") return@launch
                val current = combat.participants.firstOrNull { it.id == combat.currentParticipantId } ?: return@launch
                if (current.team != "ENEMY") return@launch
                val playerTarget = combat.participants.firstOrNull { it.team == "PLAYER" && it.status == "ACTIVE" }
                    ?: return@launch

                _uiState.update { it.copy(isEnemyTurnProcessing = true) }
                delay(ENEMY_TURN_DELAY_MS)
                val result = repository.attack(
                    sessionId,
                    combat.combatId,
                    PerformAttackRequest(attackerParticipantId = current.id, targetParticipantId = playerTarget.id)
                )
                result
                    .onSuccess { response -> _uiState.update { it.copy(combat = response) } }
                    .onFailure { error -> _uiState.update { it.copy(actionError = error.toUserMessage()) } }
                _uiState.update { it.copy(isEnemyTurnProcessing = false) }
                if (result.isFailure) return@launch
            }
        }
    }

    private fun Throwable.toUserMessage(): String = when (this) {
        is ApiException -> apiError.message
        else -> message ?: "No se pudo conectar con el servidor"
    }

    private companion object {
        const val ENEMY_TURN_DELAY_MS = 1200L
        const val ENEMY_TURN_SAFETY_LIMIT = 20
    }
}
