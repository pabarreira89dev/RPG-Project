package pab.rpg.api.dto.response;

import java.util.List;

public record InventoryResponse(List<ItemResponse> inventory, List<ItemResponse> atLocation) {
}
