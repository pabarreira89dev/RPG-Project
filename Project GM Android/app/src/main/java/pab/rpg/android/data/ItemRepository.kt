package pab.rpg.android.data

import pab.rpg.android.network.ApiClient
import pab.rpg.android.network.GameApi
import pab.rpg.android.network.apiCall
import pab.rpg.android.network.dto.InventoryResponse
import pab.rpg.android.network.dto.ItemResponse

class ItemRepository(private val api: GameApi = ApiClient.gameApi) {

    suspend fun getInventory(sessionId: String): Result<InventoryResponse> = apiCall { api.getInventory(sessionId) }

    suspend fun pickUpItem(sessionId: String, itemId: String): Result<ItemResponse> =
        apiCall { api.pickUpItem(sessionId, itemId) }
}
