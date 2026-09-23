package pab.rpg.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pab.rpg.domain.session.GameEvent;
import pab.rpg.domain.repository.GameEventRepository;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameEventServiceImplTest {

    @Mock
    private GameEventRepository gameEventRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void appendUsesNextSequenceAfterLastEvent() {
        GameEventServiceImpl service = new GameEventServiceImpl(gameEventRepository, objectMapper);
        UUID sessionId = UUID.randomUUID();
        GameEvent lastEvent = new GameEvent(
                UUID.randomUUID(), sessionId, 5L, "ACTION_RESOLVED", null,
                Map.of(), Instant.now(), Instant.now()
        );
        when(gameEventRepository.findTopBySessionIdOrderBySequenceDesc(sessionId))
                .thenReturn(Optional.of(lastEvent));
        when(gameEventRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        GameEvent result = service.append(sessionId, "RELATIONSHIP_CHANGED", null, Map.of("delta", 2), Instant.now());

        assertEquals(6L, result.getSequence());
    }

    @Test
    void appendStartsAtOneWhenNoPreviousEvent() {
        GameEventServiceImpl service = new GameEventServiceImpl(gameEventRepository, objectMapper);
        UUID sessionId = UUID.randomUUID();
        when(gameEventRepository.findTopBySessionIdOrderBySequenceDesc(sessionId))
                .thenReturn(Optional.empty());
        when(gameEventRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        GameEvent result = service.append(sessionId, "SESSION_CREATED", null, Map.of(), Instant.now());

        assertEquals(1L, result.getSequence());
    }
}
