package pab.rpg.android.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pab.rpg.android.network.ApiClient
import pab.rpg.android.network.AuthSessionManager

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AuthViewModel(
    private val authSessionManager: AuthSessionManager = ApiClient.authSessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    fun login(username: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            authSessionManager.login(username, password)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess()
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message ?: "No se pudo iniciar sesión") }
                }
        }
    }

    fun register(username: String, password: String, email: String?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            authSessionManager.register(username, password, email)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess()
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message ?: "No se pudo crear la cuenta") }
                }
        }
    }
}
