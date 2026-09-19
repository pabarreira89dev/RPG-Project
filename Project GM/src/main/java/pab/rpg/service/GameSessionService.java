package pab.rpg.service;

import pab.rpg.api.dto.CreateGameSessionCommand;
import pab.rpg.domain.entity.GameSession;

import java.util.List;
import java.util.UUID;

public interface GameSessionService {

    GameSession createSession(CreateGameSessionCommand command);

    GameSession getSession(UUID sessionId, UUID playerId);

    List<GameSession> getSessions(UUID playerId);
}
