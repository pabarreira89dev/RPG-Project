package pab.rpg.android.network

import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

// Retries once with a refreshed token on a 401; gives up (returns null) if refresh fails or this is
// already a retried request. A no-op in practice for the "local" flavor (refreshIfNeeded() is always null).
class AuthAuthenticator(private val authSessionManager: AuthSessionManager) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) return null
        val freshToken = authSessionManager.refreshIfNeeded() ?: return null
        return response.request.newBuilder()
            .header("Authorization", "Bearer $freshToken")
            .build()
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
