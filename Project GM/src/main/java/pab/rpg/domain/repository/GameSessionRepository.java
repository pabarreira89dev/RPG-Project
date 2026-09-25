package pab.rpg.domain.repository;

import pab.rpg.domain.session.GameSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GameSessionRepository extends JpaRepository<GameSession, UUID> {

    List<GameSession> findAllByPlayerIdAndDeletedAtIsNullOrderByWorldTimeDesc(UUID playerId);

    Optional<GameSession> findByIdAndPlayerIdAndDeletedAtIsNull(UUID id, UUID playerId);
}
