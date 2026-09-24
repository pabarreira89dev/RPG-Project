package pab.rpg.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pab.rpg.domain.memory.ConversationTurn;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationTurnRepository extends JpaRepository<ConversationTurn, UUID> {

    Optional<ConversationTurn> findTopBySessionIdOrderBySequenceDesc(UUID sessionId);

    List<ConversationTurn> findTop5BySessionIdOrderBySequenceDesc(UUID sessionId);
}
