package pab.rpg.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pab.rpg.domain.entity.Combat;
import pab.rpg.domain.entity.CombatStatus;

import java.util.Optional;
import java.util.UUID;

public interface CombatRepository extends JpaRepository<Combat, UUID> {

    Optional<Combat> findBySessionIdAndStatus(UUID sessionId, CombatStatus status);
}
