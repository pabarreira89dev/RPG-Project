package pab.rpg.android.data

import pab.rpg.android.network.ApiClient
import pab.rpg.android.network.GameApi
import pab.rpg.android.network.dto.CreateSessionRequest
import pab.rpg.android.network.dto.SessionResponse
import pab.rpg.android.network.toApiExceptionOrNull
import retrofit2.HttpException

class SessionRepository(private val api: GameApi = ApiClient.gameApi) {

    suspend fun listSessions(): Result<List<SessionResponse>> = apiCall { api.listSessions() }

    suspend fun createSession(request: CreateSessionRequest): Result<SessionResponse> = apiCall { api.createSession(request) }

    suspend fun getSession(sessionId: String): Result<SessionResponse> = apiCall { api.getSession(sessionId) }

    suspend fun deleteSession(sessionId: String): Result<Unit> = apiCall {
        val response = api.deleteSession(sessionId)
        if (!response.isSuccessful) throw HttpException(response)
    }

    private suspend fun <T> apiCall(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (error: Throwable) {
        Result.failure(error.toApiExceptionOrNull() ?: error)
    }
}
