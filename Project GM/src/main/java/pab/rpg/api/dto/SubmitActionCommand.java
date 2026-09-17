package pab.rpg.api.dto;

import pab.rpg.domain.rules.ActionType;

import java.util.UUID;

public record SubmitActionCommand(
        UUID sessionId,
        UUID playerId,
        String text,
        ActionType actionType,
        UUID targetNpcId,
        long expectedVersion,
        UUID idempotencyKey
) {
}
