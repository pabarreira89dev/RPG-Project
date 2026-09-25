package pab.rpg.service.impl;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pab.rpg.domain.character.AttributeSet;
import pab.rpg.domain.character.Character;
import pab.rpg.domain.character.HealthState;
import pab.rpg.domain.repository.GameSessionRepository;
import pab.rpg.domain.session.GameSession;
import pab.rpg.domain.session.SessionStatus;
import pab.rpg.exception.SessionNotFoundException;
import pab.rpg.service.ItemService;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameSessionServiceImplTest {

    @Mock
    private GameSessionRepository gameSessionRepository;
    @Mock
    private ItemService itemService;

    private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();

    private GameSession session(UUID id, UUID playerId) {
        Character character = new Character(
                UUID.randomUUID(), "Aren", 1, 0,
                new AttributeSet(10, 10, 10, 10, 10, 10),
                new HealthState(20, 20, 0)
        );
        return new GameSession(
                id, playerId, UUID.randomUUID(), UUID.randomUUID(),
                SessionStatus.ACTIVE, Instant.parse("2026-01-01T08:00:00Z"), 0L, character, null
        );
    }

    @Test
    void deleteSessionSoftDeletesAndPersists() {
        GameSessionServiceImpl service = new GameSessionServiceImpl(gameSessionRepository, meterRegistry, itemService);
        UUID sessionId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();
        GameSession session = session(sessionId, playerId);
        when(gameSessionRepository.findByIdAndPlayerIdAndDeletedAtIsNull(sessionId, playerId))
                .thenReturn(Optional.of(session));
        when(gameSessionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.deleteSession(sessionId, playerId);

        assertNotNull(session.getDeletedAt());
        verify(gameSessionRepository).save(session);
    }

    @Test
    void deleteSessionThrowsWhenSessionMissingOrAlreadyDeleted() {
        GameSessionServiceImpl service = new GameSessionServiceImpl(gameSessionRepository, meterRegistry, itemService);
        UUID sessionId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();
        when(gameSessionRepository.findByIdAndPlayerIdAndDeletedAtIsNull(sessionId, playerId))
                .thenReturn(Optional.empty());

        assertThrows(SessionNotFoundException.class, () -> service.deleteSession(sessionId, playerId));
    }
}
