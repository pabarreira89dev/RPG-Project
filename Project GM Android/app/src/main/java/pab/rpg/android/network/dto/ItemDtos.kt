package pab.rpg.android.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class ItemResponse(
    val id: String,
    val templateCode: String,
    val name: String,
    val description: String,
    val quantity: Int,
    val durability: Int?
)

@Serializable
data class InventoryResponse(
    val inventory: List<ItemResponse>,
    val atLocation: List<ItemResponse>
)
