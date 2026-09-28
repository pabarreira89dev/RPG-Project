package pab.rpg.android.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class QuestStateResponse(
    val questCode: String,
    val questTitle: String,
    val stageCode: String,
    val stageDescription: String,
    val status: String
)

@Serializable
data class AdvanceQuestRequest(
    val choiceKey: String? = null,
    val text: String? = null
)
