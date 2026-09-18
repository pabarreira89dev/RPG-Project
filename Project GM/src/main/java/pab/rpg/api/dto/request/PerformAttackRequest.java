package pab.rpg.api.dto.request;

import java.util.UUID;

// attackerParticipantId/targetParticipantId are optional overrides; if targetParticipantId is omitted,
// CombatController derives the attack from free text (attacker is always the current turn's participant).
public record PerformAttackRequest(UUID attackerParticipantId, UUID targetParticipantId, String text) {
}
