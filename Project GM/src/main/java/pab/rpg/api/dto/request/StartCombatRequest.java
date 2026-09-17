package pab.rpg.api.dto.request;

import java.util.List;
import java.util.UUID;

public record StartCombatRequest(List<UUID> npcIds) {
}
