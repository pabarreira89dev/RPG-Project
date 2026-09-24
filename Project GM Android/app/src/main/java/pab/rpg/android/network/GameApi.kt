package pab.rpg.android.network

import pab.rpg.android.network.dto.CreateSessionRequest
import pab.rpg.android.network.dto.SessionResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

// Only the Sessions endpoints for now (MVP v0.4 step 1); NPCs/items/quests/combat follow in later steps.
interface GameApi {

    @GET("api/v1/sessions")
    suspend fun listSessions(): List<SessionResponse>

    @POST("api/v1/sessions")
    suspend fun createSession(@Body request: CreateSessionRequest): SessionResponse

    @GET("api/v1/sessions/{sessionId}")
    suspend fun getSession(@Path("sessionId") sessionId: String): SessionResponse
}
