package pab.rpg.android.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pab.rpg.android.data.ItemRepository
import pab.rpg.android.network.ApiException
import pab.rpg.android.network.dto.ItemResponse

data class InventoryUiState(
    val isLoading: Boolean = false,
    val inventory: List<ItemResponse> = emptyList(),
    val atLocation: List<ItemResponse> = emptyList(),
    val errorMessage: String? = null,
    val pickingUpItemId: String? = null,
    val actionError: String? = null
)

class InventoryViewModel(
    private val sessionId: String,
    private val repository: ItemRepository = ItemRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(InventoryUiState())
    val uiState: StateFlow<InventoryUiState> = _uiState

    init {
        load()
    }

    fun retry() = load()

    fun consumeActionError() {
        _uiState.update { it.copy(actionError = null) }
    }

    fun pickUp(itemId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(pickingUpItemId = itemId, actionError = null) }
            repository.pickUpItem(sessionId, itemId)
                .onSuccess { load() }
                .onFailure { error ->
                    _uiState.update { it.copy(pickingUpItemId = null, actionError = error.toUserMessage()) }
                }
        }
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.getInventory(sessionId)
                .onSuccess { response ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            inventory = response.inventory,
                            atLocation = response.atLocation,
                            pickingUpItemId = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, pickingUpItemId = null, errorMessage = error.toUserMessage()) }
                }
        }
    }

    private fun Throwable.toUserMessage(): String = when (this) {
        is ApiException -> apiError.message
        else -> message ?: "No se pudo conectar con el servidor"
    }
}
