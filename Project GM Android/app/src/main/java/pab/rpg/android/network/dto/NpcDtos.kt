package pab.rpg.android.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class NpcResponse(
    val id: String,
    val code: String,
    val name: String,
    val faction: String?,
    val status: String,
    val relationship: Int
)
