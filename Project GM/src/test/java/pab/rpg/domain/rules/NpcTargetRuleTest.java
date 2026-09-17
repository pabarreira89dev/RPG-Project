package pab.rpg.domain.rules;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pab.rpg.domain.entity.AttributeSet;
import pab.rpg.domain.entity.Character;
import pab.rpg.domain.entity.GameSession;
import pab.rpg.domain.entity.HealthState;
import pab.rpg.domain.entity.Npc;
import pab.rpg.domain.entity.NpcStatus;
import pab.rpg.domain.entity.SessionStatus;
import pab.rpg.domain.repository.NpcRepository;
import pab.rpg.exception.ActionNotAllowedException;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NpcTargetRuleTest {

    @Mock
    private NpcRepository npcRepository;

    private GameSession sessionAtLocation(UUID locationId) {
        Character character = new Character(
                UUID.randomUUID(), "Aren", 1, 0,
                new AttributeSet(10, 10, 10, 10, 10, 10),
                new HealthState(10, 10, 0)
        );
        return new GameSession(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), locationId,
                SessionStatus.ACTIVE, Instant.parse("2026-01-01T08:00:00Z"), 0L, character
        );
    }

    @Test
    void allowsActionWhenNoNpcIsTargeted() {
        ActionContext context = new ActionContext(sessionAtLocation(UUID.randomUUID()), ActionType.SOCIAL, null);

        assertDoesNotThrow(() -> new NpcTargetRule(npcRepository).check(context));
    }

    @Test
    void allowsActionWhenTargetedNpcIsAtCurrentLocation() {
        UUID locationId = UUID.randomUUID();
        UUID npcId = UUID.randomUUID();
        Npc npc = new Npc(npcId, "aron_tavernkeeper", "Aron", locationId, null, "Tabernero", NpcStatus.ALIVE);
        when(npcRepository.findById(npcId)).thenReturn(Optional.of(npc));

        ActionContext context = new ActionContext(sessionAtLocation(locationId), ActionType.SOCIAL, npcId);

        assertDoesNotThrow(() -> new NpcTargetRule(npcRepository).check(context));
    }

    @Test
    void rejectsActionWhenTargetedNpcDoesNotExist() {
        UUID npcId = UUID.randomUUID();
        when(npcRepository.findById(npcId)).thenReturn(Optional.empty());

        ActionContext context = new ActionContext(sessionAtLocation(UUID.randomUUID()), ActionType.SOCIAL, npcId);

        assertThrows(ActionNotAllowedException.class, () -> new NpcTargetRule(npcRepository).check(context));
    }

    @Test
    void rejectsActionWhenTargetedNpcIsAtAnotherLocation() {
        UUID npcId = UUID.randomUUID();
        Npc npc = new Npc(npcId, "aron_tavernkeeper", "Aron", UUID.randomUUID(), null, "Tabernero", NpcStatus.ALIVE);
        when(npcRepository.findById(npcId)).thenReturn(Optional.of(npc));

        ActionContext context = new ActionContext(sessionAtLocation(UUID.randomUUID()), ActionType.SOCIAL, npcId);

        assertThrows(ActionNotAllowedException.class, () -> new NpcTargetRule(npcRepository).check(context));
    }
}
