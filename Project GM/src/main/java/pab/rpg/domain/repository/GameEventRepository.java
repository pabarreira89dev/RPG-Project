package pab.rpg.domain.repository;

import pab.rpg.domain.entity.GameEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GameEventRepository extends JpaRepository<GameEvent, UUID> {

    List<GameEvent> findAllBySessionIdAndSequenceGreaterThanOrderBySequenceAsc(UUID sessionId, long sequence);

    Optional<GameEvent> findTopBySessionIdOrderBySequenceDesc(UUID sessionId);
}
