package pab.rpg.android.network

import pab.rpg.android.network.dto.ActionResponse
import pab.rpg.android.network.dto.CreateSessionRequest
import pab.rpg.android.network.dto.InventoryResponse
import pab.rpg.android.network.dto.ItemResponse
import pab.rpg.android.network.dto.NpcResponse
import pab.rpg.android.network.dto.SessionResponse
import pab.rpg.android.network.dto.SubmitActionRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

// Sessions + Actions + NPCs + Items endpoints for now (MVP v0.4 steps 1-4); quests/combat follow in later steps.
interface GameApi {

    @GET("api/v1/sessions")
    suspend fun listSessions(): List<SessionResponse>

    @POST("api/v1/sessions")
    suspend fun createSession(@Body request: CreateSessionRequest): SessionResponse

    @GET("api/v1/sessions/{sessionId}")
    suspend fun getSession(@Path("sessionId") sessionId: String): SessionResponse

    // Response<Unit> (not a plain Unit return) so a 204 empty body never hits the JSON converter.
    @DELETE("api/v1/sessions/{sessionId}")
    suspend fun deleteSession(@Path("sessionId") sessionId: String): Response<Unit>

    @GET("api/v1/sessions/{sessionId}/npcs")
    suspend fun getNpcs(@Path("sessionId") sessionId: String): List<NpcResponse>

    @GET("api/v1/sessions/{sessionId}/items")
    suspend fun getInventory(@Path("sessionId") sessionId: String): InventoryResponse

    @POST("api/v1/sessions/{sessionId}/items/{itemId}/pick-up")
    suspend fun pickUpItem(@Path("sessionId") sessionId: String, @Path("itemId") itemId: String): ItemResponse

    @POST("api/v1/sessions/{sessionId}/actions")
    suspend fun submitAction(@Path("sessionId") sessionId: String, @Body request: SubmitActionRequest): ActionResponse
}
