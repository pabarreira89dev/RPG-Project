package pab.rpg.service;

import pab.rpg.domain.entity.CombatParticipantStatus;
import pab.rpg.domain.entity.CombatStatus;
import pab.rpg.domain.entity.CombatTeam;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CombatService {

    // Starts a combat between the session's character and the given NPCs (must be ALIVE and at the current location).
    CombatView startCombat(UUID sessionId, List<UUID> npcIds);

    CombatView performAttack(UUID sessionId, UUID combatId, UUID attackerParticipantId, UUID targetParticipantId);

    Optional<CombatView> getActiveCombat(UUID sessionId);

    record CombatView(
            UUID combatId,
            CombatStatus status,
            int roundNumber,
            UUID currentParticipantId,
            List<ParticipantView> participants
    ) {
    }

    record ParticipantView(
            UUID id,
            String name,
            CombatTeam team,
            int initiative,
            int turnOrder,
            int actionsRemaining,
            int healthCurrent,
            int healthMaximum,
            CombatParticipantStatus status
    ) {
    }
}
