package pab.rpg.domain.repository;

import pab.rpg.domain.session.ProcessedAction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProcessedActionRepository extends JpaRepository<ProcessedAction, UUID> {

    Optional<ProcessedAction> findBySessionIdAndIdempotencyKey(UUID sessionId, UUID idempotencyKey);
}
