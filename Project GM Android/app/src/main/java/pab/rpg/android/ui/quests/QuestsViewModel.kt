package pab.rpg.android.ui.quests

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pab.rpg.android.data.QuestRepository
import pab.rpg.android.network.ApiException
import pab.rpg.android.network.dto.AdvanceQuestRequest
import pab.rpg.android.network.dto.QuestStateResponse

enum class QuestStatusUi { NOT_STARTED, ACTIVE, COMPLETED }

data class QuestItemUi(
    val code: String,
    val title: String,
    val status: QuestStatusUi,
    val stageDescription: String? = null,
    val choices: List<QuestChoice> = emptyList()
)

data class QuestsUiState(
    val isLoading: Boolean = false,
    val activeAndAvailable: List<QuestItemUi> = emptyList(),
    val completed: List<QuestItemUi> = emptyList(),
    val completedExpanded: Boolean = false,
    val errorMessage: String? = null,
    val busyQuestCode: String? = null,
    val actionError: String? = null,
    val freeTextByCode: Map<String, String> = emptyMap()
)

class QuestsViewModel(
    private val sessionId: String,
    private val repository: QuestRepository = QuestRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuestsUiState())
    val uiState: StateFlow<QuestsUiState> = _uiState

    init {
        load()
    }

    fun retry() = load()

    fun toggleCompletedExpanded() {
        _uiState.update { it.copy(completedExpanded = !it.completedExpanded) }
    }

    fun consumeActionError() {
        _uiState.update { it.copy(actionError = null) }
    }

    fun onFreeTextChange(code: String, text: String) {
        _uiState.update { it.copy(freeTextByCode = it.freeTextByCode + (code to text)) }
    }

    fun startQuest(code: String) {
        runQuestAction(code) { repository.startQuest(sessionId, code) }
    }

    fun advanceWithChoice(code: String, choiceKey: String) {
        runQuestAction(code) { repository.advanceQuest(sessionId, code, AdvanceQuestRequest(choiceKey = choiceKey)) }
    }

    fun advanceWithText(code: String, text: String) {
        if (text.isBlank()) return
        runQuestAction(code) { repository.advanceQuest(sessionId, code, AdvanceQuestRequest(text = text)) }
    }

    private fun runQuestAction(code: String, block: suspend () -> Result<QuestStateResponse>) {
        viewModelScope.launch {
            _uiState.update { it.copy(busyQuestCode = code, actionError = null) }
            block()
                .onSuccess {
                    _uiState.update { it.copy(freeTextByCode = it.freeTextByCode - code) }
                    load()
                }
                .onFailure { error ->
                    _uiState.update { it.copy(busyQuestCode = null, actionError = error.toUserMessage()) }
                }
        }
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.getQuests(sessionId)
                .onSuccess { quests ->
                    val (activeAndAvailable, completed) = mergeWithCatalog(quests)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            busyQuestCode = null,
                            activeAndAvailable = activeAndAvailable,
                            completed = completed
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, busyQuestCode = null, errorMessage = error.toUserMessage()) }
                }
        }
    }

    // Left-joins the static client catalog with whatever quest states the backend already knows about.
    private fun mergeWithCatalog(quests: List<QuestStateResponse>): Pair<List<QuestItemUi>, List<QuestItemUi>> {
        val byCode = quests.associateBy { it.questCode }
        val activeAndAvailable = mutableListOf<QuestItemUi>()
        val completed = mutableListOf<QuestItemUi>()
        for (known in QuestCatalog.ALL) {
            val state = byCode[known.code]
            val item = when {
                state == null -> QuestItemUi(known.code, known.title, QuestStatusUi.NOT_STARTED)
                state.status == "COMPLETED" -> QuestItemUi(
                    code = known.code,
                    title = known.title,
                    status = QuestStatusUi.COMPLETED,
                    stageDescription = state.stageDescription
                )
                else -> QuestItemUi(
                    code = known.code,
                    title = known.title,
                    status = QuestStatusUi.ACTIVE,
                    stageDescription = state.stageDescription,
                    choices = known.choicesByStage[state.stageCode].orEmpty()
                )
            }
            if (item.status == QuestStatusUi.COMPLETED) completed += item else activeAndAvailable += item
        }
        return activeAndAvailable to completed
    }

    private fun Throwable.toUserMessage(): String = when (this) {
        is ApiException -> apiError.message
        else -> message ?: "No se pudo conectar con el servidor"
    }
}
