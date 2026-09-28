package pab.rpg.android.network

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

// Single source of truth for "the current session is no longer valid" (401/403), so any screen
// can react without each ViewModel special-casing auth errors on its own.
object AuthEvents {
    private val _authFailures = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val authFailures: SharedFlow<Unit> = _authFailures

    fun notifyAuthFailure() {
        _authFailures.tryEmit(Unit)
    }
}
