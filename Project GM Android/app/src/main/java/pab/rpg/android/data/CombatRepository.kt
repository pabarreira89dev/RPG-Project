package pab.rpg.android.data

import pab.rpg.android.network.ApiClient
import pab.rpg.android.network.GameApi
import pab.rpg.android.network.apiCall
import pab.rpg.android.network.dto.CombatResponse
import pab.rpg.android.network.dto.PerformAttackRequest
import pab.rpg.android.network.dto.StartCombatRequest

class CombatRepository(private val api: GameApi = ApiClient.gameApi) {

    suspend fun getActiveCombat(sessionId: String): Result<CombatResponse> = apiCall { api.getCombat(sessionId) }

    suspend fun startCombat(sessionId: String, npcId: String): Result<CombatResponse> =
        apiCall { api.startCombat(sessionId, StartCombatRequest(npcIds = listOf(npcId))) }

    suspend fun attack(sessionId: String, combatId: String, request: PerformAttackRequest): Result<CombatResponse> =
        apiCall { api.attack(sessionId, combatId, request) }
}
