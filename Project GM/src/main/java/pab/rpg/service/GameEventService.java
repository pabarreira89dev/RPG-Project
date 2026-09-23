package pab.rpg.service;

import pab.rpg.domain.session.GameEvent;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface GameEventService {

    GameEvent append(UUID sessionId, String type, UUID actorId, Object payload, Instant worldTime);

    List<GameEvent> findAfter(UUID sessionId, long sequence);
}
