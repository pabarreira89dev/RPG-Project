package pab.rpg.api.dto.request;

import pab.rpg.domain.rules.ActionType;

import java.util.UUID;

// actionType/targetNpcId are optional overrides; if omitted, ActionServiceImpl derives them from the free
// text via MasterAdapter.interpret(). targetNpcId (explicit or interpreted) is only used by SOCIAL actions.
public record SubmitActionRequest(
        String text,
        ActionType actionType,
        UUID targetNpcId,
        long expectedVersion,
        UUID idempotencyKey
) {
}
