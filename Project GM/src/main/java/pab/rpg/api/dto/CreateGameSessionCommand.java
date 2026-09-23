package pab.rpg.api.dto;

import pab.rpg.domain.character.AttributeSet;
import pab.rpg.domain.character.HealthState;

import java.time.Instant;
import java.util.UUID;

public record CreateGameSessionCommand(
        UUID playerId,
        UUID worldId,
        UUID currentLocationId,
        Instant worldTime,
        String characterName,
        int characterLevel,
        int characterExperience,
        AttributeSet attributes,
        HealthState health
) {
}
