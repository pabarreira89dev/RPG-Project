package pab.rpg.domain.rules;

import org.junit.jupiter.api.Test;
import pab.rpg.domain.character.AttributeSet;
import pab.rpg.domain.character.Character;
import pab.rpg.domain.session.GameSession;
import pab.rpg.domain.character.HealthState;
import pab.rpg.domain.session.SessionStatus;
import pab.rpg.exception.ActionNotAllowedException;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ActorAliveRuleTest {

    private final ActorAliveRule rule = new ActorAliveRule();

    private GameSession sessionWithHealth(int currentHealth) {
        Character character = new Character(
                UUID.randomUUID(), "Aren", 1, 0,
                new AttributeSet(10, 10, 10, 10, 10, 10),
                new HealthState(10, currentHealth, 0)
        );
        return new GameSession(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                SessionStatus.ACTIVE, Instant.parse("2026-01-01T08:00:00Z"), 0L, character
        );
    }

    @Test
    void allowsActionWhenCharacterIsAlive() {
        ActionContext context = new ActionContext(sessionWithHealth(5), ActionType.EXPLORATION, null);

        assertDoesNotThrow(() -> rule.check(context));
    }

    @Test
    void rejectsActionWhenCharacterHasNoHealth() {
        ActionContext context = new ActionContext(sessionWithHealth(0), ActionType.EXPLORATION, null);

        assertThrows(ActionNotAllowedException.class, () -> rule.check(context));
    }
}
