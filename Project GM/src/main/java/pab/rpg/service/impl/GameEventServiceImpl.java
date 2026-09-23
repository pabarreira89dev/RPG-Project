package pab.rpg.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pab.rpg.domain.session.GameEvent;
import pab.rpg.domain.repository.GameEventRepository;
import pab.rpg.service.GameEventService;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class GameEventServiceImpl implements GameEventService {

    private final GameEventRepository gameEventRepository;
    private final ObjectMapper objectMapper;

    @Override
    public GameEvent append(UUID sessionId, String type, UUID actorId, Object payload, Instant worldTime) {
        Objects.requireNonNull(sessionId, "sessionId must not be null");
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(payload, "payload must not be null");
        Objects.requireNonNull(worldTime, "worldTime must not be null");

        long nextSequence = gameEventRepository.findTopBySessionIdOrderBySequenceDesc(sessionId)
                .map(event -> event.getSequence() + 1)
                .orElse(1L);

        GameEvent event = new GameEvent(
                null,
                sessionId,
                nextSequence,
                type,
                actorId,
                toPayloadMap(payload),
                worldTime,
                Instant.now()
        );

        return gameEventRepository.save(event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameEvent> findAfter(UUID sessionId, long sequence) {
        Objects.requireNonNull(sessionId, "sessionId must not be null");
        return gameEventRepository.findAllBySessionIdAndSequenceGreaterThanOrderBySequenceAsc(sessionId, sequence);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toPayloadMap(Object payload) {
        return objectMapper.convertValue(payload, Map.class);
    }
}
