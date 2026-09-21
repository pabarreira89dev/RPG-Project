package pab.rpg.api.dto.request;

import pab.rpg.domain.entity.AttributeSet;
import pab.rpg.domain.entity.HealthState;

import java.time.Instant;
import java.util.UUID;

// playerId is not part of the request body anymore: it always comes from the authenticated identity (TDD §12).
public record CreateSessionRequest(
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
