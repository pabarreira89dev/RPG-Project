package pab.rpg.api.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import pab.rpg.api.dto.CreateGameSessionCommand;
import pab.rpg.api.dto.request.CreateSessionRequest;
import pab.rpg.domain.entity.AttributeSet;
import pab.rpg.domain.entity.HealthState;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class CreateSessionRequestMapperTest {

    private final CreateSessionRequestMapper mapper = Mappers.getMapper(CreateSessionRequestMapper.class);

    @Test
    void mapsRequestToCommand() {
        UUID playerId = UUID.randomUUID();
        UUID worldId = UUID.randomUUID();
        UUID currentLocationId = UUID.randomUUID();
        Instant worldTime = Instant.parse("2026-01-01T00:00:00Z");
        AttributeSet attributes = new AttributeSet(10, 11, 12, 13, 14, 15);
        HealthState health = new HealthState(100, 80, 2);
        CreateSessionRequest request = new CreateSessionRequest(
                playerId,
                worldId,
                currentLocationId,
                worldTime,
                "Aria",
                7,
                950,
                attributes,
                health
        );

        CreateGameSessionCommand command = mapper.toCommand(request);

        assertEquals(playerId, command.playerId());
        assertEquals(worldId, command.worldId());
        assertEquals(currentLocationId, command.currentLocationId());
        assertEquals(worldTime, command.worldTime());
        assertEquals("Aria", command.characterName());
        assertEquals(7, command.characterLevel());
        assertEquals(950, command.characterExperience());
        assertSame(attributes, command.attributes());
        assertSame(health, command.health());
    }
}
