package pab.rpg.api.dto.request;

import pab.rpg.domain.rules.ActionType;

import java.util.UUID;

// actionType is a provisional input until real text interpretation (OpenAI) selects it from free text.
// targetNpcId is only used by SOCIAL actions aimed at a specific NPC.
public record SubmitActionRequest(
        String text,
        ActionType actionType,
        UUID targetNpcId,
        long expectedVersion,
        UUID idempotencyKey
) {
}
