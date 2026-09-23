package pab.rpg.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pab.rpg.domain.combat.CombatParticipant;

import java.util.List;
import java.util.UUID;

public interface CombatParticipantRepository extends JpaRepository<CombatParticipant, UUID> {

    List<CombatParticipant> findAllByCombatIdOrderByTurnOrderAsc(UUID combatId);
}
