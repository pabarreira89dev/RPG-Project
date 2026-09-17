package pab.rpg.domain.repository;

import pab.rpg.domain.entity.GameSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GameSessionRepository extends JpaRepository<GameSession, UUID> {

    List<GameSession> findAllByPlayerIdOrderByWorldTimeDesc(UUID playerId);

    Optional<GameSession> findByIdAndPlayerId(UUID id, UUID playerId);
}
