package pab.rpg.service.impl;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pab.rpg.api.dto.CreateGameSessionCommand;
import pab.rpg.domain.entity.Character;
import pab.rpg.domain.entity.GameSession;
import pab.rpg.domain.entity.SessionStatus;
import pab.rpg.domain.repository.GameSessionRepository;
import pab.rpg.exception.SessionNotFoundException;
import pab.rpg.service.GameSessionService;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class GameSessionServiceImpl implements GameSessionService {

    private final GameSessionRepository gameSessionRepository;
    private final MeterRegistry meterRegistry;

    @Override
    public GameSession createSession(CreateGameSessionCommand command) {
        validate(command);

        Character character = new Character(
                null,
                command.characterName().trim(),
                command.characterLevel(),
                command.characterExperience(),
                command.attributes(),
                command.health()
        );

        GameSession session = new GameSession(
                null,
                command.playerId(),
                command.worldId(),
                command.currentLocationId(),
                SessionStatus.ACTIVE,
                command.worldTime() == null ? Instant.now() : command.worldTime(),
                0L,
                character
        );

        session = gameSessionRepository.save(session);
        meterRegistry.counter("pab.rpg.sessions.created").increment();
        return session;
    }

    @Override
    @Transactional(readOnly = true)
    public GameSession getSession(UUID sessionId, UUID playerId) {
        Objects.requireNonNull(sessionId, "sessionId must not be null");
        Objects.requireNonNull(playerId, "playerId must not be null");

        GameSession session = gameSessionRepository.findByIdAndPlayerId(sessionId, playerId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId, playerId));
        meterRegistry.counter("pab.rpg.sessions.retrieved").increment();
        return session;
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameSession> getSessions(UUID playerId) {
        Objects.requireNonNull(playerId, "playerId must not be null");
        return gameSessionRepository.findAllByPlayerIdOrderByWorldTimeDesc(playerId);
    }

    private void validate(CreateGameSessionCommand command) {
        Objects.requireNonNull(command, "command must not be null");
        Objects.requireNonNull(command.playerId(), "playerId must not be null");
        Objects.requireNonNull(command.worldId(), "worldId must not be null");
        Objects.requireNonNull(command.attributes(), "attributes must not be null");
        Objects.requireNonNull(command.health(), "health must not be null");

        if (command.characterName() == null || command.characterName().isBlank()) {
            throw new IllegalArgumentException("characterName must not be blank");
        }
        if (command.characterLevel() < 1) {
            throw new IllegalArgumentException("characterLevel must be at least 1");
        }
        if (command.characterExperience() < 0) {
            throw new IllegalArgumentException("characterExperience must not be negative");
        }
    }
}
