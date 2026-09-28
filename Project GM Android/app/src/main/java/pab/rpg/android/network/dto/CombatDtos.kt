package pab.rpg.android.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class CombatParticipantResponse(
    val id: String,
    val name: String,
    val team: String,
    val initiative: Int,
    val turnOrder: Int,
    val actionsRemaining: Int,
    val healthCurrent: Int,
    val healthMaximum: Int,
    val status: String
)

@Serializable
data class CombatResponse(
    val combatId: String,
    val status: String,
    val roundNumber: Int,
    val currentParticipantId: String,
    val participants: List<CombatParticipantResponse>,
    val narration: String? = null
)

@Serializable
data class StartCombatRequest(val npcIds: List<String>)

@Serializable
data class PerformAttackRequest(
    val attackerParticipantId: String? = null,
    val targetParticipantId: String? = null,
    val text: String? = null
)
