package pab.rpg.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pab.rpg.api.dto.response.InventoryResponse;
import pab.rpg.api.dto.response.ItemResponse;
import pab.rpg.domain.entity.GameSession;
import pab.rpg.security.CurrentPlayer;
import pab.rpg.service.GameSessionService;
import pab.rpg.service.ItemService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sessions/{sessionId}/items")
@RequiredArgsConstructor
public class ItemController {

    private final GameSessionService gameSessionService;
    private final ItemService itemService;

    @GetMapping
    public InventoryResponse getInventory(
            @PathVariable UUID sessionId,
            @CurrentPlayer UUID playerId
    ) {
        GameSession session = gameSessionService.getSession(sessionId, playerId);

        return new InventoryResponse(
                itemService.getInventory(sessionId).stream().map(ItemResponse::from).toList(),
                itemService.getItemsAtLocation(sessionId, session.getCurrentLocationId()).stream().map(ItemResponse::from).toList()
        );
    }

    @PostMapping("/{itemId}/pick-up")
    public ItemResponse pickUpItem(
            @PathVariable UUID sessionId,
            @PathVariable UUID itemId,
            @CurrentPlayer UUID playerId
    ) {
        gameSessionService.getSession(sessionId, playerId);

        return ItemResponse.from(itemService.pickUpItem(sessionId, itemId));
    }
}
