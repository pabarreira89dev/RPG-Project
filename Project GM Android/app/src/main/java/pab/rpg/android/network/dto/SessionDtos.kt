package pab.rpg.android.network.dto

import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

@Serializable
data class AttributeSetDto(
    val strength: Int,
    val agility: Int,
    val intellect: Int,
    val willpower: Int,
    val perception: Int,
    val presence: Int
)

@Serializable
data class HealthStateDto(
    val maximum: Int,
    val current: Int,
    val wounds: Int
)

@Serializable
data class CreateSessionRequest(
    val worldId: String,
    val currentLocationId: String,
    val worldTime: String,
    val characterName: String,
    val characterLevel: Int,
    val characterExperience: Int,
    val attributes: AttributeSetDto,
    val health: HealthStateDto
) {
    companion object {
        // village_square seed location (Project GM V3 migration) — same default the E2E suite uses.
        private const val VILLAGE_SQUARE_ID = "11111111-1111-1111-1111-111111111111"

        fun newCharacter(characterName: String = "Aventurero"): CreateSessionRequest = CreateSessionRequest(
            worldId = UUID.randomUUID().toString(),
            currentLocationId = VILLAGE_SQUARE_ID,
            worldTime = Instant.now().toString(),
            characterName = characterName,
            characterLevel = 1,
            characterExperience = 0,
            attributes = AttributeSetDto(strength = 5, agility = 5, intellect = 5, willpower = 5, perception = 5, presence = 5),
            health = HealthStateDto(maximum = 20, current = 20, wounds = 0)
        )
    }
}

@Serializable
data class CharacterResponse(
    val id: String,
    val name: String,
    val level: Int,
    val experience: Int,
    val attributes: AttributeSetDto,
    val health: HealthStateDto
)

@Serializable
data class SessionResponse(
    val sessionId: String,
    val playerId: String,
    val worldId: String,
    val currentLocationId: String,
    val status: String,
    val worldTime: String,
    val version: Long,
    val character: CharacterResponse
)
