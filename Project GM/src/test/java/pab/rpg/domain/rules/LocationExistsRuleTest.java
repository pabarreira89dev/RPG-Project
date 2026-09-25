package pab.rpg.domain.rules;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pab.rpg.domain.character.AttributeSet;
import pab.rpg.domain.character.Character;
import pab.rpg.domain.session.GameSession;
import pab.rpg.domain.character.HealthState;
import pab.rpg.domain.session.SessionStatus;
import pab.rpg.domain.repository.LocationRepository;
import pab.rpg.exception.ActionNotAllowedException;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationExistsRuleTest {

    @Mock
    private LocationRepository locationRepository;

    private GameSession sessionAtLocation(UUID locationId) {
        Character character = new Character(
                UUID.randomUUID(), "Aren", 1, 0,
                new AttributeSet(10, 10, 10, 10, 10, 10),
                new HealthState(10, 10, 0)
        );
        return new GameSession(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), locationId,
                SessionStatus.ACTIVE, Instant.parse("2026-01-01T08:00:00Z"), 0L, character, null
        );
    }

    @Test
    void allowsActionWhenLocationExists() {
        UUID locationId = UUID.randomUUID();
        when(locationRepository.existsById(locationId)).thenReturn(true);

        ActionContext context = new ActionContext(sessionAtLocation(locationId), ActionType.EXPLORATION, null);

        assertDoesNotThrow(() -> new LocationExistsRule(locationRepository).check(context));
    }

    @Test
    void rejectsActionWhenLocationIsMissing() {
        ActionContext context = new ActionContext(sessionAtLocation(null), ActionType.EXPLORATION, null);

        assertThrows(ActionNotAllowedException.class, () -> new LocationExistsRule(locationRepository).check(context));
    }

    @Test
    void rejectsActionWhenLocationDoesNotExist() {
        UUID locationId = UUID.randomUUID();
        when(locationRepository.existsById(locationId)).thenReturn(false);

        ActionContext context = new ActionContext(sessionAtLocation(locationId), ActionType.EXPLORATION, null);

        assertThrows(ActionNotAllowedException.class, () -> new LocationExistsRule(locationRepository).check(context));
    }
}
