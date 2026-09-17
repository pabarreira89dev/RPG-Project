package pab.rpg.domain.rules;

import pab.rpg.domain.entity.GameSession;

import java.util.UUID;

// Context a GameRule needs to decide whether an action is currently allowed. targetNpcId is nullable:
// only social actions aimed at a specific NPC set it.
public record ActionContext(GameSession session, ActionType actionType, UUID targetNpcId) {
}
