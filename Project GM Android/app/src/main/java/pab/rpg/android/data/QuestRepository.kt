package pab.rpg.android.data

import pab.rpg.android.network.ApiClient
import pab.rpg.android.network.GameApi
import pab.rpg.android.network.apiCall
import pab.rpg.android.network.dto.AdvanceQuestRequest
import pab.rpg.android.network.dto.QuestStateResponse

class QuestRepository(private val api: GameApi = ApiClient.gameApi) {

    suspend fun getQuests(sessionId: String): Result<List<QuestStateResponse>> = apiCall { api.getQuests(sessionId) }

    suspend fun startQuest(sessionId: String, questCode: String): Result<QuestStateResponse> =
        apiCall { api.startQuest(sessionId, questCode) }

    suspend fun advanceQuest(sessionId: String, questCode: String, request: AdvanceQuestRequest): Result<QuestStateResponse> =
        apiCall { api.advanceQuest(sessionId, questCode, request) }
}
