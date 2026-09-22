package pab.rpg.service;

import java.util.List;
import java.util.UUID;

public interface ItemService {

    List<ItemView> getInventory(UUID sessionId);

    List<ItemView> getItemsAtLocation(UUID sessionId, UUID locationId);

    ItemView pickUpItem(UUID sessionId, UUID itemId);

    // Spawns the fixed set of world items a new session starts with (uncollected, lying in their seed locations).
    void seedInitialItems(UUID sessionId);

    record ItemView(
            UUID id,
            String templateCode,
            String name,
            String description,
            int quantity,
            Integer durability
    ) {
    }
}
