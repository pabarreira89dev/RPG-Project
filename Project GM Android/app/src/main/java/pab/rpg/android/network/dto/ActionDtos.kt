package pab.rpg.android.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class SubmitActionRequest(
    val text: String,
    val actionType: String? = null,
    val targetNpcId: String? = null,
    val expectedVersion: Long,
    val idempotencyKey: String
)

@Serializable
data class RollDetailsDto(
    val d20: Int,
    val modifier: Int,
    val total: Int,
    val difficulty: Int
)

@Serializable
data class ResultDetailsDto(
    val type: String,
    val roll: RollDetailsDto
)

@Serializable
data class ActionResponse(
    val actionId: String,
    val status: String,
    val narration: String,
    val result: ResultDetailsDto,
    val events: List<String>,
    val stateVersion: Long
)
