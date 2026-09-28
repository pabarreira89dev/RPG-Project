package pab.rpg.android.data

import pab.rpg.android.network.ApiClient
import pab.rpg.android.network.GameApi
import pab.rpg.android.network.apiCall
import pab.rpg.android.network.dto.NpcResponse

class NpcRepository(private val api: GameApi = ApiClient.gameApi) {

    suspend fun getNpcs(sessionId: String): Result<List<NpcResponse>> = apiCall { api.getNpcs(sessionId) }
}
