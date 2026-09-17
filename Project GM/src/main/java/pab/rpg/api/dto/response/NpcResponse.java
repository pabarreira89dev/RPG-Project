package pab.rpg.api.dto.response;

import lombok.Builder;
import pab.rpg.domain.entity.Npc;
import pab.rpg.domain.entity.NpcStatus;

import java.util.UUID;

@Builder
public record NpcResponse(
        UUID id,
        String code,
        String name,
        String faction,
        NpcStatus status,
        int relationship
) {

    public static NpcResponse from(Npc npc, int relationship) {
        return NpcResponse.builder()
                .id(npc.getId())
                .code(npc.getCode())
                .name(npc.getName())
                .faction(npc.getFaction())
                .status(npc.getStatus())
                .relationship(relationship)
                .build();
    }
}
