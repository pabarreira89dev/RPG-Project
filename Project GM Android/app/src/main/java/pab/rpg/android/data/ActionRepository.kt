package pab.rpg.android.data

import pab.rpg.android.network.ApiClient
import pab.rpg.android.network.GameApi
import pab.rpg.android.network.apiCall
import pab.rpg.android.network.dto.ActionResponse
import pab.rpg.android.network.dto.SubmitActionRequest

class ActionRepository(private val api: GameApi = ApiClient.gameApi) {

    suspend fun submitAction(sessionId: String, request: SubmitActionRequest): Result<ActionResponse> =
        apiCall { api.submitAction(sessionId, request) }
}
