package pab.rpg.service;

import pab.rpg.domain.combat.CombatParticipantStatus;
import pab.rpg.domain.combat.CombatStatus;
import pab.rpg.domain.combat.CombatTeam;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CombatService {

    // Starts a combat between the session's character and the given NPCs (must be ALIVE and at the current location).
    CombatView startCombat(UUID sessionId, List<UUID> npcIds);

    CombatView performAttack(UUID sessionId, UUID combatId, UUID attackerParticipantId, UUID targetParticipantId);

    // Derives the target from free text (attacker is always whoever's turn it currently is); the
    // target text is resolved against the visible ENEMY participants via MasterAdapter.selectCandidate.
    CombatView performAttack(UUID sessionId, UUID combatId, String playerText);

    Optional<CombatView> getActiveCombat(UUID sessionId);

    record CombatView(
            UUID combatId,
            CombatStatus status,
            int roundNumber,
            UUID currentParticipantId,
            List<ParticipantView> participants,
            String narration
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
