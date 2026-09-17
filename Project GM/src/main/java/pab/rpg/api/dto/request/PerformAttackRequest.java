package pab.rpg.api.dto.request;

import java.util.UUID;

public record PerformAttackRequest(UUID attackerParticipantId, UUID targetParticipantId) {
}
