package pab.rpg.api.dto.response;

import lombok.Builder;
import pab.rpg.service.ItemService;

import java.util.UUID;

@Builder
public record ItemResponse(
        UUID id,
        String templateCode,
        String name,
        String description,
        int quantity,
        Integer durability
) {

    public static ItemResponse from(ItemService.ItemView view) {
        return ItemResponse.builder()
                .id(view.id())
                .templateCode(view.templateCode())
                .name(view.name())
                .description(view.description())
                .quantity(view.quantity())
                .durability(view.durability())
                .build();
    }
}
